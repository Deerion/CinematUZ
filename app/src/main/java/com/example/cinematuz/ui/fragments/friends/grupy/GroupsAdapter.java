package com.example.cinematuz.ui.fragments.friends.grupy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Group;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private final List<Group> groups;
    private final OnGroupClickListener listener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Cache awatarów, by nie pobierać ich ponownie przy scrollowaniu listy
    private final Map<String, String> avatarCache = new HashMap<>();

    public GroupsAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.tvName.setText(group.getName());

        List<String> members = group.getMembers();
        int membersSize = members != null ? members.size() : 0;
        holder.tvCount.setText(holder.itemView.getContext().getString(R.string.group_members_format, membersSize));

        // Ustawienie kliknięcia na całą kartę grupy
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });

        // Wypełnianie okrągłych miniaturek uczestników
        holder.bindParticipants(members, avatarCache, db);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount, tvExtraParticipants;
        ImageView ivParticipant1, ivParticipant2, ivParticipant3;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGroupName);
            tvCount = itemView.findViewById(R.id.tvGroupMembersCount);
            // Poprawione ID!
            tvExtraParticipants = itemView.findViewById(R.id.tvExtraParticipants);
            ivParticipant1 = itemView.findViewById(R.id.ivParticipant1);
            ivParticipant2 = itemView.findViewById(R.id.ivParticipant2);
            ivParticipant3 = itemView.findViewById(R.id.ivParticipant3);
        }

        // Metoda, której brakowało!
        void bindParticipants(List<String> members, Map<String, String> cache, FirebaseFirestore db) {
            // Najpierw ukrywamy wszystkie awatary
            ivParticipant1.setVisibility(View.GONE);
            ivParticipant2.setVisibility(View.GONE);
            ivParticipant3.setVisibility(View.GONE);
            tvExtraParticipants.setVisibility(View.GONE);

            if (members == null || members.isEmpty()) return;

            ImageView[] avatarViews = {ivParticipant1, ivParticipant2, ivParticipant3};
            int maxToDraw = Math.min(members.size(), 3);

            for (int i = 0; i < maxToDraw; i++) {
                String uid = members.get(i);
                ImageView currentView = avatarViews[i];
                currentView.setVisibility(View.VISIBLE);

                // Jeśli mamy ten awatar już w pamięci, załaduj od razu
                if (cache.containsKey(uid)) {
                    loadAvatar(cache.get(uid), currentView);
                } else {
                    // W przeciwnym razie pobierz profil z Firebase
                    db.collection("profiles").document(uid).get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String url = doc.getString("avatar_url"); // zgodnie z kluczami bazy z poprzednich etapów
                            cache.put(uid, url);
                            loadAvatar(url, currentView);
                        } else {
                            currentView.setImageResource(R.drawable.ic_person);
                        }
                    }).addOnFailureListener(e -> currentView.setImageResource(R.drawable.ic_person));
                }
            }

            // Jeśli jest więcej niż 3 członków, pokaż "+X"
            if (members.size() > 3) {
                tvExtraParticipants.setVisibility(View.VISIBLE);
                tvExtraParticipants.setText("+" + (members.size() - 3));
            }
        }

        private void loadAvatar(String url, ImageView imageView) {
            if (url != null && !url.isEmpty()) {
                Glide.with(imageView.getContext())
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_person);
            }
        }
    }
}