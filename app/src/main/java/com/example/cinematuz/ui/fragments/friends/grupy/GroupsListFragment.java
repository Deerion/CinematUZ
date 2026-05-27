package com.example.cinematuz.ui.fragments.friends.grupy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Group;
import com.example.cinematuz.data.models.FriendRequest;
import com.example.cinematuz.ui.fragments.friends.RequestAdapter;
import com.example.cinematuz.utils.DialogHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment wyświetlający listę grup użytkownika oraz oczekujące zaproszenia do grup.
 * Obsługuje dołączanie do grup, odrzucanie zaproszeń oraz tworzenie nowych grup.
 */
public class GroupsListFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private GroupsAdapter adapter;
    private RequestAdapter requestsAdapter;

    private List<Group> groupsList = new ArrayList<>();
    private List<FriendRequest> groupRequestsList = new ArrayList<>();

    /**
     * Inicjalizuje widok fragmentu, Firebase oraz dwa adaptery: dla list grup i zaproszeń.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        RecyclerView rvGroups = view.findViewById(R.id.rvGroups);
        RecyclerView rvGroupRequests = view.findViewById(R.id.rvGroupRequests);

        // Adapter dla list grup
        adapter = new GroupsAdapter(groupsList, group -> {
            Bundle args = new Bundle();
            args.putString("GROUP_ID", group.getId());
            Navigation.findNavController(view).navigate(R.id.groupDetailsFragment, args);
        });
        rvGroups.setAdapter(adapter);

        // Adapter dla zaproszeń do grup
        requestsAdapter = new RequestAdapter(groupRequestsList, new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                Group group = new Group();
                group.setId(request.getUid());
                joinGroup(group);
            }

            @Override
            public void onDecline(FriendRequest request) {
                Group group = new Group();
                group.setId(request.getUid());
                rejectInvite(group);
            }
        });

        if (rvGroupRequests != null) {
            rvGroupRequests.setAdapter(requestsAdapter);
        }

        View btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        if (btnCreateGroup != null) {
            btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        }

        listenForGroups();
        listenForGroupInvites();

        return view;
    }

    /**
     * Nasłuchuje przychodzących zaproszeń do grup w Firestore.
     */
    private void listenForGroupInvites() {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("profiles").document(myUid).collection("group_invites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        groupRequestsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            FriendRequest req = new FriendRequest();
                            req.setUid(doc.getId());
                            req.setUsername(doc.getString("groupName"));
                            req.setType("group");

                            groupRequestsList.add(req);
                        }
                        if (requestsAdapter != null) requestsAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Dodaje bieżącego użytkownika do wybranej grupy.
     * 
     * @param group Obiekt grupy z ustawionym ID.
     */
    private void joinGroup(Group group) {
        String myUid = mAuth.getCurrentUser().getUid();
        db.collection("groups").document(group.getId())
                .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(myUid))
                .addOnSuccessListener(aVoid -> rejectInvite(group));
    }

    /**
     * Usuwa zaproszenie do grupy z profilu użytkownika.
     * 
     * @param group Obiekt grupy z ustawionym ID.
     */
    private void rejectInvite(Group group) {
        String myUid = mAuth.getCurrentUser().getUid();
        db.collection("profiles").document(myUid).collection("group_invites").document(group.getId()).delete();
    }

    /**
     * Nasłuchuje zmian w listach grup, do których należy użytkownik.
     */
    private void listenForGroups() {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("groups")
                .whereArrayContains("members", myUid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        groupsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Group group = doc.toObject(Group.class);
                            group.setId(doc.getId());
                            groupsList.add(group);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Wyświetla okno dialogowe do wpisania nazwy nowej grupy.
     */
    private void showCreateGroupDialog() {
        DialogHelper.showInputDialog(
                requireContext(),
                getString(R.string.dialog_new_group_title),
                getString(R.string.dialog_new_group_desc),
                getString(R.string.group_name_hint),
                getString(R.string.dialog_create),
                getString(R.string.dialog_cancel),
                name -> {
                    if (!TextUtils.isEmpty(name)) {
                        createGroupInFirebase(name);
                    } else {
                        Toast.makeText(getContext(), R.string.error_name_empty, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Tworzy nowy dokument grupy w Firestore.
     * 
     * @param groupName Nazwa grupy.
     */
    private void createGroupInFirebase(String groupName) {
        String myUid = mAuth.getCurrentUser().getUid();
        List<String> members = new ArrayList<>();
        members.add(myUid);

        Group newGroup = new Group(groupName, myUid, members);
        db.collection("groups").add(newGroup)
                .addOnSuccessListener(docRef -> Toast.makeText(getContext(), R.string.toast_group_created_alt, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.error_creating_group, Toast.LENGTH_SHORT).show());
    }
}