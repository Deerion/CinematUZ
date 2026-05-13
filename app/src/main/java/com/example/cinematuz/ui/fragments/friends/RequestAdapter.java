package com.example.cinematuz.ui.fragments.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.FriendRequest;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    private final List<FriendRequest> items;
    private final OnRequestActionListener listener;

    public RequestAdapter(List<FriendRequest> items, OnRequestActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest req = items.get(position);

        if ("group".equals(req.getType())) {
            holder.tvName.setText("Zaproszenie do grupy: " + req.getUsername());
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_group_add)
                    .centerCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.tvName.setText(req.getUsername());
            if (req.getAvatarUrl() != null && !req.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(req.getAvatarUrl())
                        .centerCrop()
                        .into(holder.ivAvatar);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.ic_person)
                        .centerCrop()
                        .into(holder.ivAvatar);
            }
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(req));
        holder.btnDecline.setOnClickListener(v -> listener.onDecline(req));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar;
        View btnAccept, btnDecline;

        RequestViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRequestName);
            ivAvatar = v.findViewById(R.id.ivRequestAvatar);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline = v.findViewById(R.id.btnDecline);
        }
    }
}