package com.example.cinematuz.ui.fragments.friends.znajomi;

import android.graphics.Color;
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
// DODANY IMPORT DLA FIREBASE AUTH
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    public interface OnFriendActionListener {
        void onRemoveFriend(Friend friend, int position);
    }

    private final List<Friend> friendsList;
    private final OnFriendActionListener listener;
    private String ownerId;

    public FriendsAdapter(List<Friend> friendsList, OnFriendActionListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        notifyDataSetChanged();
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

        android.content.Context context = holder.itemView.getContext();

        String myUid = FirebaseAuth.getInstance().getUid();

        // Weryfikacja czy aktualny wpis to właściciel grupy (aby pokazać gwiazdkę)
        boolean isCurrentItemOwner = (ownerId != null && ownerId.equals(friend.getId()));

        // Weryfikacja czy aktualny wpis to JA (zalogowany użytkownik)
        boolean isMe = (myUid != null && myUid.equals(friend.getId()));

        if (isCurrentItemOwner) {
            holder.ivOwnerStar.setVisibility(View.VISIBLE);
        } else {
            holder.ivOwnerStar.setVisibility(View.GONE);
        }

        // LOGIKA PRZYCISKU USUWANIA (X)
        if (ownerId != null) {
            // Jesteśmy w trybie GRUPY
            boolean amIOwner = (myUid != null && myUid.equals(ownerId));

            // Pokazuj 'X' TYLKO jeśli ja jestem właścicielem i ta pozycja na liście TO NIE JESTEM JA
            if (amIOwner && !isMe) {
                holder.btnRemoveFriend.setVisibility(View.VISIBLE);
            } else {
                holder.btnRemoveFriend.setVisibility(View.GONE);
            }
        } else {
            // Jesteśmy w zwykłej liście znajomych (ownerId jest null), pokazujemy 'X' wszystkim oprócz siebie (na wszelki wypadek)
            if (!isMe) {
                holder.btnRemoveFriend.setVisibility(View.VISIBLE);
            } else {
                holder.btnRemoveFriend.setVisibility(View.GONE);
            }
        }

        // Awatar
        String avatarUrl = friend.getAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .circleCrop()
                    .into(holder.ivFriendAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.ivFriendAvatar);
        }

        // Status
        if ("pending".equals(friend.getStatus())) {
            holder.tvFriendStatus.setText(context.getString(R.string.status_pending_acceptance));
            holder.tvFriendStatus.setTextColor(Color.parseColor("#F59E0B"));
            if (holder.vOnlineStatusDot != null) holder.vOnlineStatusDot.setVisibility(View.GONE);
        } else if (friend.isOnline()) {
            holder.tvFriendStatus.setText(context.getString(R.string.status_active_now));
            holder.tvFriendStatus.setTextColor(Color.parseColor("#22C55E"));
            if (holder.vOnlineStatusDot != null) holder.vOnlineStatusDot.setVisibility(View.VISIBLE);
        } else {
            holder.tvFriendStatus.setText(context.getString(R.string.status_offline));
            holder.tvFriendStatus.setTextColor(Color.GRAY);
            if (holder.vOnlineStatusDot != null) holder.vOnlineStatusDot.setVisibility(View.GONE);
        }

        // Kliknięcie usunięcia
        if (holder.btnRemoveFriend != null) {
            holder.btnRemoveFriend.setOnClickListener(v -> listener.onRemoveFriend(friend, position));
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        TextView tvFriendStatus;
        ImageView ivFriendAvatar;
        ImageView ivOwnerStar;
        View btnRemoveFriend;
        View vOnlineStatusDot;

        FriendViewHolder(View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            tvFriendStatus = itemView.findViewById(R.id.tvFriendStatus);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            ivOwnerStar = itemView.findViewById(R.id.ivOwnerStar);
            btnRemoveFriend = itemView.findViewById(R.id.btnRemoveFriend);
            vOnlineStatusDot = itemView.findViewById(R.id.vOnlineStatusDot);
        }
    }
}