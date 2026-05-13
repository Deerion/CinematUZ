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
import androidx.navigation.Navigation; // Dodano import
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Group;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GroupsAdapter adapter;
    private List<Group> groupsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        RecyclerView rvGroups = view.findViewById(R.id.rvGroups);

        // POPRAWKA: Przekazujemy groupsList ORAZ listener kliknięcia
        adapter = new GroupsAdapter(groupsList, group -> {
            Bundle args = new Bundle();
            args.putString("GROUP_ID", group.getId());
            // Nawigacja do fragmentu szczegółów
            Navigation.findNavController(view).navigate(R.id.groupDetailsFragment, args);
        });

        rvGroups.setAdapter(adapter);

        FloatingActionButton btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        if (btnCreateGroup != null) {
            btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        }

        listenForGroups();

        return view;
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
        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 0);
        EditText input = new EditText(requireContext());
        input.setHint("Nazwa grupy");
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nowa grupa")
                .setView(container)
                .setPositiveButton("Stwórz", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(name)) createGroupInFirebase(name);
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void createGroupInFirebase(String groupName) {
        if (mAuth.getCurrentUser() == null) return;

        String myUid = mAuth.getCurrentUser().getUid();
        List<String> members = new ArrayList<>();
        members.add(myUid);

        Group newGroup = new Group(groupName, myUid, members);

        db.collection("groups").add(newGroup)
                .addOnSuccessListener(ref -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Stworzono grupę!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Błąd: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}