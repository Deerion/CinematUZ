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
import com.example.cinematuz.ui.fragments.friends.znajomi.NotificationsBottomSheet;
import com.example.cinematuz.ui.fragments.friends.znajomi.RequestAdapter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
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
                    Toast.makeText(getContext(), "Brak nowych zaproszeń", Toast.LENGTH_SHORT).show();
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

        db.collection("profiles").document(mAuth.getUid()).collection("friend_requests")
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded()) {
                        requestList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            requestList.add(new FriendRequest(doc.getId(), doc.getString("username"), doc.getString("avatarUrl")));
                        }
                        requestAdapter.notifyDataSetChanged();

                        int count = requestList.size();

                        if (count > 0) {
                            if (notificationBadgeText != null) {
                                // Ustawiamy tekst i upewniamy się, że kolor jest biały
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
                });
    }

    private void acceptRequest(FriendRequest request) {
        String myUid = mAuth.getUid();
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

    private void declineRequest(FriendRequest request) {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("profiles").document(mAuth.getUid()).collection("friend_requests").document(request.getUid())
                .delete();
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