package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class GroupMemberCircleAdapter extends RecyclerView.Adapter<GroupMemberCircleAdapter.ViewHolder> {
    private List<Friend> members;
    private String ownerId = "";
    private final OnMemberInteractionListener listener;

    // Interfejs do komunikacji z Fragmentem
    public interface OnMemberInteractionListener {
        void onMemberLongClick(Friend friend);
    }

    public GroupMemberCircleAdapter(List<Friend> members, OnMemberInteractionListener listener) {
        this.members = members;
        this.listener = listener;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member_circle, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = members.get(position);
        holder.tvName.setText(friend.getName());
        holder.ivStar.setVisibility((friend.getId() != null && friend.getId().equals(ownerId)) ? View.VISIBLE : View.GONE);

        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(friend.getAvatarUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }

        String myUid = FirebaseAuth.getInstance().getUid();
        boolean isMe = friend.getId() != null && friend.getId().equals(myUid);
        boolean isAdmin = myUid != null && myUid.equals(ownerId);

        holder.ivRemove.setVisibility(View.GONE);
        holder.flAvatarContainer.clearAnimation();
        holder.itemView.setOnClickListener(null);

        // Wywołanie interfejsu zamiast bezpośredniej metody z Fragmentu
        holder.itemView.setOnLongClickListener(v -> {
            if (isAdmin && !isMe) {
                if (listener != null) {
                    listener.onMemberLongClick(friend);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStar, ivAvatar, ivRemove;
        TextView tvName;
        View flAvatarContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStar = itemView.findViewById(R.id.ivOwnerStar);
            ivAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            tvName = itemView.findViewById(R.id.tvFriendName);
            ivRemove = itemView.findViewById(R.id.ivRemoveMember);
            flAvatarContainer = itemView.findViewById(R.id.flAvatarContainer);
        }
    }
}