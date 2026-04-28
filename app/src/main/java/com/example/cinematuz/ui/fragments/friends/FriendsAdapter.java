package com.example.cinematuz.ui.fragments;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<Friend> friendsList;
    private OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onRemoveFriend(Friend friend, int position);
    }

    public FriendsAdapter(List<Friend> friendsList, OnFriendActionListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        holder.tvFriendName.setText(friend.getName());

        if (friend.isOnline()) {
            holder.tvFriendStatus.setText("Aktywny teraz");
            holder.tvFriendStatus.setTextColor(Color.parseColor("#22C55E"));
            holder.vOnlineStatusDot.setVisibility(View.VISIBLE);
        } else {
            holder.tvFriendStatus.setText("Offline");
            holder.tvFriendStatus.setTextColor(Color.GRAY);
            holder.vOnlineStatusDot.setVisibility(View.GONE);
        }

        holder.btnRemoveFriend.setOnClickListener(v -> listener.onRemoveFriend(friend, position));
    }

    @Override
    public int getItemCount() { return friendsList.size(); }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivFriendAvatar;
        View vOnlineStatusDot;
        TextView tvFriendName, tvFriendStatus;
        ImageButton btnRemoveFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            vOnlineStatusDot = itemView.findViewById(R.id.vOnlineStatusDot);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvFriendStatus = itemView.findViewById(R.id.tvFriendStatus);
            btnRemoveFriend = itemView.findViewById(R.id.btnRemoveFriend);
        }
    }
}