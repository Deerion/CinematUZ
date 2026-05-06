package com.example.cinematuz.ui.fragments.friends;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.FriendRequest;
import com.example.cinematuz.data.models.SearchResultUser;
import com.example.cinematuz.utils.BluetoothHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment implements FriendsAdapter.OnFriendActionListener {

    private static final String TAG = "FriendsFragment";
    private static final int PERMISSION_REQUEST_CODE = 101;

    private RecyclerView recyclerViewFriends;
    private RecyclerView rvFriendRequests;
    private FriendsAdapter friendsAdapter;
    private RequestAdapter requestAdapter;

    private TextView tvFriendsCount;
    private TextView tvInvitationsTitle;
    private TextView tvGroupsPlaceholder;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Bluetooth
    private BluetoothHelper bluetoothHelper;
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private BluetoothDeviceAdapter btAdapter;

    private List<Friend> friendsList = new ArrayList<>();
    private List<FriendRequest> pendingRequests = new ArrayList<>();
    private Map<String, ListenerRegistration> profileListeners = new HashMap<>();
    private boolean isFriendsTabActive = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);
        rvFriendRequests = view.findViewById(R.id.rvFriendRequests);
        tvFriendsCount = view.findViewById(R.id.tvFriendsCount);
        tvInvitationsTitle = view.findViewById(R.id.tvInvitationsTitle);
        tvGroupsPlaceholder = view.findViewById(R.id.tvGroupsPlaceholder);

        FloatingActionButton fab = view.findViewById(R.id.fabAddFriend);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupFriends);
        View bluetoothCard = view.findViewById(R.id.layoutBluetoothNearby);

        if (tvInvitationsTitle != null) tvInvitationsTitle.setVisibility(View.GONE);

        if (fab != null) fab.setOnClickListener(v -> showSearchFriendsDialog());

        if (bluetoothCard != null) {
            bluetoothCard.setOnClickListener(v -> checkPermissionsAndStartBluetooth());
        }

        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    isFriendsTabActive = (checkedId == R.id.btnTabFriends);
                    updateUIVisibility();
                }
            });
        }

        // Setup głównej listy znajomych
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new FriendsAdapter(friendsList, this);
        recyclerViewFriends.setAdapter(friendsAdapter);

        // Setup listy zaproszeń
        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        requestAdapter = new RequestAdapter(pendingRequests, new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) { acceptRequest(request); }
            @Override
            public void onDecline(FriendRequest request) { declineRequest(request); }
        });
        rvFriendRequests.setAdapter(requestAdapter);

        listenForFriends();
        listenForFriendRequests();

        return view;
    }

    // --- LOGIKA BLUETOOTH ---

    private void checkPermissionsAndStartBluetooth() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(p);
            }
        }

        if (missingPermissions.isEmpty()) {
            startBluetoothSearch();
        } else {
            requestPermissions(missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothSearch() {
        discoveredDevices.clear();
        bluetoothHelper = new BluetoothHelper(requireContext(), new BluetoothHelper.BluetoothDiscoveryListener() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                if (!discoveredDevices.contains(device)) {
                    discoveredDevices.add(device);
                    if (btAdapter != null) {
                        btAdapter.notifyDataSetChanged();
                    }
                    Log.d(TAG, "Bluetooth znaleziono: " + device.getName());
                }
            }

            @Override
            public void onDiscoveryFinished() {
                Log.d(TAG, "Bluetooth: Skanowanie zakończone");
            }
        });

        if (bluetoothHelper.isBluetoothEnabled()) {
            showBluetoothDiscoveryDialog();
            bluetoothHelper.startDiscovery();
        } else {
            Toast.makeText(getContext(), "Włącz Bluetooth w ustawieniach!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBluetoothDiscoveryDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogStyle);
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bluetooth_discovery, null);
        dialog.setContentView(sheetView);

        RecyclerView rv = sheetView.findViewById(R.id.rvBluetoothDevices);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        btAdapter = new BluetoothDeviceAdapter(discoveredDevices);
        rv.setAdapter(btAdapter);

        sheetView.findViewById(R.id.btnCancelDiscovery).setOnClickListener(v -> {
            if (bluetoothHelper != null) bluetoothHelper.stopDiscovery();
            dialog.dismiss();
        });

        dialog.setOnDismissListener(d -> {
            if (bluetoothHelper != null) bluetoothHelper.stopDiscovery();
        });

        dialog.show();
    }

    // --- LOGIKA FIREBASE (ZNAJOMI) ---

    private void listenForFriends() {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("profiles").document(myUid).collection("friends")
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;

                    if (value != null) {
                        List<String> currentFriendIds = new ArrayList<>();
                        friendsList.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            Friend friend = doc.toObject(Friend.class);
                            friend.setId(doc.getId());
                            friendsList.add(friend);
                            currentFriendIds.add(friend.getId());

                            if (!profileListeners.containsKey(friend.getId())) {
                                String targetId = friend.getId();
                                ListenerRegistration reg = db.collection("profiles").document(targetId)
                                        .addSnapshotListener((profDoc, profErr) -> {
                                            if (profDoc != null && profDoc.exists() && isAdded()) {
                                                Boolean isOnline = profDoc.getBoolean("isOnline");
                                                String avatarUrl = profDoc.getString("avatar_url");

                                                for (int i = 0; i < friendsList.size(); i++) {
                                                    if (friendsList.get(i).getId().equals(targetId)) {
                                                        friendsList.get(i).setOnline(isOnline != null ? isOnline : false);
                                                        if (avatarUrl != null) friendsList.get(i).setAvatarUrl(avatarUrl);
                                                        friendsAdapter.notifyItemChanged(i);
                                                        break;
                                                    }
                                                }
                                            }
                                        });
                                profileListeners.put(targetId, reg);
                            }
                        }

                        List<String> keysToRemove = new ArrayList<>();
                        for (String id : profileListeners.keySet()) {
                            if (!currentFriendIds.contains(id)) {
                                profileListeners.get(id).remove();
                                keysToRemove.add(id);
                            }
                        }
                        for (String id : keysToRemove) {
                            profileListeners.remove(id);
                        }

                        friendsAdapter.notifyDataSetChanged();
                        if (tvFriendsCount != null) {
                            tvFriendsCount.setText(getString(R.string.friends_active_count_dynamic, friendsList.size()));
                        }
                    }
                });
    }

    private void listenForFriendRequests() {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("profiles").document(myUid).collection("friend_requests")
                .addSnapshotListener((value, error) -> {
                    if (error != null || !isAdded()) return;

                    if (value != null) {
                        pendingRequests.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String senderUid = doc.getId();
                            String senderUsername = doc.getString("username");
                            String senderAvatar = doc.getString("avatarUrl");

                            if (senderUsername != null) {
                                pendingRequests.add(new FriendRequest(senderUid, senderUsername, senderAvatar));
                            }
                        }
                        requestAdapter.notifyDataSetChanged();
                        updateUIVisibility();
                    }
                });
    }

    private void updateUIVisibility() {
        boolean hasRequests = !pendingRequests.isEmpty();

        if (tvInvitationsTitle != null) {
            tvInvitationsTitle.setVisibility(isFriendsTabActive && hasRequests ? View.VISIBLE : View.GONE);
            tvInvitationsTitle.setText("Zaproszenia (" + pendingRequests.size() + ")");
        }

        if (rvFriendRequests != null) {
            rvFriendRequests.setVisibility(isFriendsTabActive && hasRequests ? View.VISIBLE : View.GONE);
        }

        if (recyclerViewFriends != null) recyclerViewFriends.setVisibility(isFriendsTabActive ? View.VISIBLE : View.GONE);
        if (tvFriendsCount != null) tvFriendsCount.setVisibility(isFriendsTabActive ? View.VISIBLE : View.GONE);
        if (tvGroupsPlaceholder != null) tvGroupsPlaceholder.setVisibility(isFriendsTabActive ? View.GONE : View.VISIBLE);
    }

    private void showSearchFriendsDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogStyle);
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_search_friends, null);
        bottomSheet.setContentView(sheetView);

        EditText etSearch = sheetView.findViewById(R.id.etFriendSearch);
        RecyclerView rvSuggestions = sheetView.findViewById(R.id.rvFriendSuggestions);
        LinearLayout layoutNoResults = sheetView.findViewById(R.id.layoutNoResults);

        SuggestionAdapter suggestionAdapter = new SuggestionAdapter(new ArrayList<>(), clickedUser -> {
            if (mAuth.getCurrentUser() == null) return;
            String myUid = mAuth.getCurrentUser().getUid();

            for (Friend f : friendsList) {
                if (f.getId().equals(clickedUser.getUid())) {
                    Toast.makeText(getContext(), "Użytkownik jest już na Twojej liście!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            db.collection("profiles").document(myUid).get().addOnSuccessListener(documentSnapshot -> {
                String myUsername = documentSnapshot.getString("username");
                String myAvatarUrl = documentSnapshot.getString("avatar_url");

                WriteBatch batch = db.batch();
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("username", myUsername != null ? myUsername : "Użytkownik");
                requestData.put("avatarUrl", myAvatarUrl != null ? myAvatarUrl : "");
                requestData.put("status", "pending");

                DocumentReference requestRef = db.collection("profiles").document(clickedUser.getUid())
                        .collection("friend_requests").document(myUid);
                batch.set(requestRef, requestData);

                DocumentReference myPendingRef = db.collection("profiles").document(myUid)
                        .collection("friends").document(clickedUser.getUid());
                Friend pendingFriend = new Friend(clickedUser.getUid(), clickedUser.getUsername(), clickedUser.getAvatarUrl(), false);
                pendingFriend.setStatus("pending");
                batch.set(myPendingRef, pendingFriend);

                batch.commit().addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Wysłano zaproszenie!", Toast.LENGTH_SHORT).show();
                    bottomSheet.dismiss();
                });
            });
        });

        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(suggestionAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (query.isEmpty()) {
                    suggestionAdapter.submitList(new ArrayList<>());
                    layoutNoResults.setVisibility(View.GONE);
                    return;
                }

                db.collection("profiles")
                        .whereGreaterThanOrEqualTo("username", query)
                        .whereLessThanOrEqualTo("username", query + "\uf8ff")
                        .limit(10).get()
                        .addOnSuccessListener(snapshots -> {
                            List<SearchResultUser> results = new ArrayList<>();
                            String mUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
                            for (QueryDocumentSnapshot doc : snapshots) {
                                if (!doc.getId().equals(mUid)) {
                                    results.add(new SearchResultUser(doc.getId(), doc.getString("username"), doc.getString("avatar_url")));
                                }
                            }
                            suggestionAdapter.submitList(results);
                            layoutNoResults.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                        });
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        bottomSheet.show();
    }

    private void acceptRequest(FriendRequest request) {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("profiles").document(myUid).get().addOnSuccessListener(documentSnapshot -> {
            WriteBatch batch = db.batch();
            Friend friendForMe = new Friend(request.getUid(), request.getUsername(), request.getAvatarUrl(), true);
            friendForMe.setStatus("accepted");
            batch.set(db.collection("profiles").document(myUid).collection("friends").document(request.getUid()), friendForMe);

            Friend friendForSender = new Friend(myUid, documentSnapshot.getString("username"), documentSnapshot.getString("avatar_url"), true);
            friendForSender.setStatus("accepted");
            batch.set(db.collection("profiles").document(request.getUid()).collection("friends").document(myUid), friendForSender);

            batch.delete(db.collection("profiles").document(myUid).collection("friend_requests").document(request.getUid()));
            batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Zaakceptowano!", Toast.LENGTH_SHORT).show());
        });
    }

    private void declineRequest(FriendRequest request) {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        WriteBatch batch = db.batch();
        batch.delete(db.collection("profiles").document(myUid).collection("friend_requests").document(request.getUid()));
        batch.delete(db.collection("profiles").document(request.getUid()).collection("friends").document(myUid));
        batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Odrzucono", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRemoveFriend(Friend friend, int position) {
        if ("pending".equals(friend.getStatus())) {
            cancelSentRequest(friend);
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.friends_remove_title)
                .setMessage(getString(R.string.friends_remove_message, friend.getName()))
                .setPositiveButton(R.string.friends_remove_confirm, (dialog, which) -> {
                    String myUid = mAuth.getCurrentUser().getUid();
                    WriteBatch batch = db.batch();
                    batch.delete(db.collection("profiles").document(myUid).collection("friends").document(friend.getId()));
                    batch.delete(db.collection("profiles").document(friend.getId()).collection("friends").document(myUid));
                    batch.commit();
                })
                .setNegativeButton(R.string.profile_cancel, null).show();
    }

    private void cancelSentRequest(Friend friend) {
        String myUid = mAuth.getCurrentUser().getUid();
        WriteBatch batch = db.batch();
        batch.delete(db.collection("profiles").document(myUid).collection("friends").document(friend.getId()));
        batch.delete(db.collection("profiles").document(friend.getId()).collection("friend_requests").document(myUid));
        batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Anulowano zaproszenie", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) if (res != PackageManager.PERMISSION_GRANTED) allGranted = false;
            if (allGranted) startBluetoothSearch();
            else Toast.makeText(getContext(), "Wymagane uprawnienia Bluetooth!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (ListenerRegistration reg : profileListeners.values()) reg.remove();
        profileListeners.clear();
        if (bluetoothHelper != null) bluetoothHelper.stopDiscovery();
    }
}