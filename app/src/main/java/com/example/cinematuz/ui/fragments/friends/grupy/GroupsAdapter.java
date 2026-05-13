package com.example.cinematuz.ui.fragments.friends.grupy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Group;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    // 1. Definiujemy interfejs dla kliknięć
    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private final List<Group> groups;
    private final OnGroupClickListener listener;

    // 2. Aktualizujemy konstruktor, aby przyjmował listener
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

        int membersSize = group.getMembers() != null ? group.getMembers().size() : 0;
        holder.tvCount.setText(membersSize + " członków");

        // 3. Obsługujemy kliknięcie w cały element listy
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGroupName);
            tvCount = itemView.findViewById(R.id.tvGroupMembersCount);
        }
    }
}