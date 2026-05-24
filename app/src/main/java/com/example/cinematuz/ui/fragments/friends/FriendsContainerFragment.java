package com.example.cinematuz.ui.fragments.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.cinematuz.ui.fragments.friends.grupy.GroupsListFragment;
import com.example.cinematuz.ui.fragments.friends.znajomi.FriendsListFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class FriendsContainerFragment extends Fragment {

    private ImageButton btnHomeNotifications;
    private TextView notificationBadgeText;
    private MaterialCardView notificationPanel;
    private RecyclerView rvNotifications;
    private RequestAdapter requestAdapter;

    private List<FriendRequest> requestList = new ArrayList<>();
    private List<FriendRequest> friendReqs = new ArrayList<>();
    private List<FriendRequest> groupReqs = new ArrayList<>();
    private List<FriendRequest> removalReqs = new ArrayList<>(); // DODANE: Lista powiadomień o usunięciu

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_container, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnHomeNotifications = view.findViewById(R.id.btn_home_notifications);
        notificationBadgeText = view.findViewById(R.id.notificationBadgeText);
        notificationPanel = view.findViewById(R.id.notificationPanel);
        rvNotifications = view.findViewById(R.id.rvTopNotifications);

        setupRecyclerView();

        if (btnHomeNotifications != null) {
            btnHomeNotifications.setOnClickListener(v -> {
                if (notificationPanel.getVisibility() == View.VISIBLE) {
                    notificationPanel.setVisibility(View.GONE);
                } else if (!requestList.isEmpty()) {
                    notificationPanel.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Brak powiadomień", Toast.LENGTH_SHORT).show();
                }
            });
        }

        setupToggleGroup(view);
        listenForFriendRequests();

        if (savedInstanceState == null) {
            replaceFragment(new FriendsListFragment());
        }

        return view;
    }

    private void setupRecyclerView() {
        requestAdapter = new RequestAdapter(requestList, new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest req) { acceptRequest(req); }
            @Override
            public void onDecline(FriendRequest req) { declineRequest(req); }
        });
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(requestAdapter);
    }

    private void listenForFriendRequests() {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getUid();

        // 1. Nasłuchujemy zaproszeń do znajomych
        db.collection("profiles").document(myUid).collection("friend_requests")
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded()) {
                        friendReqs.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            friendReqs.add(new FriendRequest(doc.getId(), doc.getString("username"), doc.getString("avatarUrl"), "friend"));
                        }
                        updateCombinedNotifications();
                    }
                });

        // 2. Nasłuchujemy zaproszeń do grupy
        db.collection("profiles").document(myUid).collection("group_invites")
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded()) {
                        groupReqs.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            groupReqs.add(new FriendRequest(doc.getId(), doc.getString("groupName"), null, "group"));
                        }
                        updateCombinedNotifications();
                    }
                });

        // 3. DODANE: Nasłuchujemy ogólnych powiadomień (np. o usunięciu z grupy)
        db.collection("profiles").document(myUid).collection("notifications")
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded()) {
                        removalReqs.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            if ("group_removal".equals(doc.getString("type"))) {
                                // Używamy modelu FriendRequest dla wygody - "groupName" trafia pod username
                                removalReqs.add(new FriendRequest(doc.getId(), doc.getString("groupName"), null, "group_removal"));
                            }
                        }
                        updateCombinedNotifications();
                    }
                });
    }

    private void updateCombinedNotifications() {
        requestList.clear();
        requestList.addAll(friendReqs);
        requestList.addAll(groupReqs);
        requestList.addAll(removalReqs); // DODANE: złączamy listy
        requestAdapter.notifyDataSetChanged();

        int count = requestList.size();
        if (count > 0) {
            if (notificationBadgeText != null) {
                notificationBadgeText.setText(String.valueOf(count));
                notificationBadgeText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                notificationBadgeText.setVisibility(View.VISIBLE);
            }
            btnHomeNotifications.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        } else {
            if (notificationBadgeText != null) {
                notificationBadgeText.setVisibility(View.GONE);
            }
            btnHomeNotifications.clearColorFilter();
            notificationPanel.setVisibility(View.GONE);
        }
    }

    private void acceptRequest(FriendRequest request) {
        String myUid = mAuth.getUid();

        // Jeśli to group_removal to przycisk jest schowany, ale dla bezpieczeństwa
        if ("group_removal".equals(request.getType())) return;

        if ("group".equals(request.getType())) {
            db.collection("groups").document(request.getUid())
                    .update("members", FieldValue.arrayUnion(myUid))
                    .addOnSuccessListener(aVoid -> {
                        db.collection("profiles").document(myUid).collection("group_invites").document(request.getUid()).delete();
                        if (isAdded()) Toast.makeText(getContext(), "Dołączyłeś do grupy!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("profiles").document(myUid).get().addOnSuccessListener(doc -> {
                WriteBatch batch = db.batch();

                Friend me = new Friend(myUid, doc.getString("username"), doc.getString("avatar_url"), true);
                me.setStatus("accepted");

                Friend them = new Friend(request.getUid(), request.getUsername(), request.getAvatarUrl(), true);
                them.setStatus("accepted");

                batch.set(db.collection("profiles").document(myUid).collection("friends").document(request.getUid()), them);
                batch.set(db.collection("profiles").document(request.getUid()).collection("friends").document(myUid), me);
                batch.delete(db.collection("profiles").document(myUid).collection("friend_requests").document(request.getUid()));

                batch.commit().addOnSuccessListener(aVoid -> {
                    if (isAdded()) Toast.makeText(getContext(), "Zaakceptowano!", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }

    private void declineRequest(FriendRequest request) {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getUid();

        if ("group".equals(request.getType())) {
            db.collection("profiles").document(myUid).collection("group_invites").document(request.getUid()).delete();
        } else if ("group_removal".equals(request.getType())) {
            // DODANE: Użytkownik zamyka powiadomienie (X) - usuwamy je z bazy
            db.collection("profiles").document(myUid).collection("notifications").document(request.getUid()).delete();
        } else {
            db.collection("profiles").document(myUid).collection("friend_requests").document(request.getUid()).delete();
        }
    }

    private void setupToggleGroup(View view) {
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupFriends);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabFriends) {
                    replaceFragment(new FriendsListFragment());
                } else if (checkedId == R.id.btnTabGroups) {
                    replaceFragment(new GroupsListFragment());
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.child_fragment_container, fragment)
                .commit();
    }
}