package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.Group;
import com.example.cinematuz.ui.fragments.friends.znajomi.FriendsAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupDetailsFragment extends Fragment {

    private String groupId;
    private FirebaseFirestore db;
    private ListenerRegistration groupListener;

    private TextView tvGroupName, tvMemberCount;
    private RecyclerView rvMembers;
    private FriendsAdapter membersAdapter;
    private List<Friend> membersList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString("GROUP_ID");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_details, container, false);

        tvGroupName = view.findViewById(R.id.tvDetailsGroupName);
        tvMemberCount = view.findViewById(R.id.tvDetailsMemberCount);
        rvMembers = view.findViewById(R.id.rvGroupMembers);

        MaterialButton btnAddMember = view.findViewById(R.id.btnDetailsAddMember);
        MaterialButton btnBluetooth = view.findViewById(R.id.btnDetailsBluetooth);
        MaterialButton btnAddMovies = view.findViewById(R.id.btnDetailsAddMovies);

        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        membersAdapter = new FriendsAdapter(membersList, (friend, position) -> {
            // Tu możesz dodać logikę usuwania z grupy
        });
        rvMembers.setAdapter(membersAdapter);

        setupClickListeners(btnAddMember, btnBluetooth, btnAddMovies);
        listenToGroupChanges();

        return view;
    }

    private void listenToGroupChanges() {
        if (groupId == null) return;

        groupListener = db.collection("groups").document(groupId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    Group group = snapshot.toObject(Group.class);
                    if (group != null) {
                        tvGroupName.setText(group.getName());
                        List<String> memberUids = group.getMembers();
                        tvMemberCount.setText(memberUids.size() + " członków");

                        // Przekazanie ID właściciela do adaptera
                        membersAdapter.setOwnerId(group.getOwnerId());

                        fetchMemberProfiles(memberUids);
                    }
                });
    }

    private void fetchMemberProfiles(List<String> uids) {
        membersList.clear();
        for (String uid : uids) {
            db.collection("profiles").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && isAdded()) {
                    // Wykorzystanie konstruktora Friend(id, name, avatarUrl, isOnline)
                    Friend member = new Friend(
                            doc.getId(),
                            doc.getString("username"),
                            doc.getString("avatar_url"),
                            Boolean.TRUE.equals(doc.getBoolean("isOnline"))
                    );
                    membersList.add(member);
                    membersAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setupClickListeners(View add, View bt, View movies) {
        add.setOnClickListener(v -> showInviteFriendDialog());
        bt.setOnClickListener(v -> Toast.makeText(getContext(), "Szukanie urządzeń...", Toast.LENGTH_SHORT).show());
        movies.setOnClickListener(v -> Toast.makeText(getContext(), "Przejdź do bazy filmów", Toast.LENGTH_SHORT).show());
    }

    private void showInviteFriendDialog() {
        String myUid = FirebaseAuth.getInstance().getUid();

        // Pobieramy Twoich znajomych z bazy
        db.collection("profiles").document(myUid).collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Friend> friends = new ArrayList<>();
                    List<String> names = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Friend f = new Friend(doc.getId(), doc.getString("name"), doc.getString("avatarUrl"), true);
                        friends.add(f);
                        names.add(f.getName());
                    }

                    if (friends.isEmpty()) {
                        Toast.makeText(getContext(), "Nie masz jeszcze znajomych do zaproszenia", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Prosty Alert Dialog z możliwością wyboru znajomego
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Zaproś znajomego")
                            .setItems(names.toArray(new String[0]), (dialog, which) -> {
                                Friend selectedFriend = friends.get(which);
                                sendGroupInvite(selectedFriend.getId());
                            })
                            .show();
                });
    }

    private void sendGroupInvite(String friendUid) {
        // Przygotowujemy powiadomienie
        Map<String, Object> invite = new HashMap<>();
        invite.put("groupName", tvGroupName.getText().toString());
        invite.put("type", "group");

        // Wrzucamy zaproszenie do nowej kolekcji "group_invites" zapraszanego użytkownika
        db.collection("profiles").document(friendUid).collection("group_invites")
                .document(groupId)
                .set(invite)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Zaproszenie zostało wysłane!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupListener != null) groupListener.remove();
    }
}