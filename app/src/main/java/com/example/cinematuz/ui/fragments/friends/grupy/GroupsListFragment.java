package com.example.cinematuz.ui.fragments.friends.grupy;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Group;
import com.example.cinematuz.utils.DialogHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupsListFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GroupsAdapter adapter;
    private GroupsAdapter requestsAdapter;
    private List<Group> groupsList = new ArrayList<>();
    private List<Group> groupRequestsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        RecyclerView rvGroups = view.findViewById(R.id.rvGroups);
        RecyclerView rvGroupRequests = view.findViewById(R.id.rvGroupRequests);

        // Adapter dla moich grup
        adapter = new GroupsAdapter(groupsList, group -> {
            Bundle args = new Bundle();
            args.putString("GROUP_ID", group.getId());
            Navigation.findNavController(view).navigate(R.id.groupDetailsFragment, args);
        });
        rvGroups.setAdapter(adapter);

        // Adapter dla zaproszeń do grup
        requestsAdapter = new GroupsAdapter(groupRequestsList, group -> {
            showAcceptInviteDialog(group);
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

    private void listenForGroupInvites() {
        if (mAuth.getCurrentUser() == null) return;
        String myUid = mAuth.getCurrentUser().getUid();

        // Słuchamy na zaproszenia w profilu użytkownika
        db.collection("profiles").document(myUid).collection("group_invites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        groupRequestsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Group g = new Group();
                            g.setId(doc.getId());
                            g.setName(doc.getString("groupName"));
                            groupRequestsList.add(g);
                        }
                        if (requestsAdapter != null) requestsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAcceptInviteDialog(Group group) {
        DialogHelper.showConfirmDialog(
                requireContext(),
                "Zaproszenie do grupy",
                "Czy chcesz dołączyć do grupy \"" + group.getName() + "\"?",
                "Dołącz",
                "Odrzuć",
                () -> joinGroup(group)
        );
    }

    private void joinGroup(Group group) {
        String myUid = mAuth.getCurrentUser().getUid();
        db.collection("groups").document(group.getId())
                .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(myUid))
                .addOnSuccessListener(aVoid -> rejectInvite(group));
    }

    private void rejectInvite(Group group) {
        String myUid = mAuth.getCurrentUser().getUid();
        db.collection("profiles").document(myUid).collection("group_invites").document(group.getId()).delete();
    }

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

    private void showCreateGroupDialog() {
        DialogHelper.showInputDialog(
                requireContext(),
                "Nowa grupa",
                "Wprowadź nazwę dla swojej nowej grupy filmowej.",
                "Nazwa grupy",
                "Stwórz",
                "Anuluj",
                name -> {
                    if (!TextUtils.isEmpty(name)) {
                        createGroupInFirebase(name);
                    } else {
                        Toast.makeText(getContext(), "Nazwa nie może być pusta", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void createGroupInFirebase(String groupName) {
        String myUid = mAuth.getCurrentUser().getUid();
        List<String> members = new ArrayList<>();
        members.add(myUid);

        Group newGroup = new Group(groupName, myUid, members);
        db.collection("groups").add(newGroup)
                .addOnSuccessListener(docRef -> Toast.makeText(getContext(), "Utworzono grupę!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Błąd tworzenia grupy", Toast.LENGTH_SHORT).show());
    }
}