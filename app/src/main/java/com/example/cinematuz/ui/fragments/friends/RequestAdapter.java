package com.example.cinematuz.ui.fragments.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

        holder.btnAccept.setVisibility(View.VISIBLE);
        holder.tvInfo.setVisibility(View.VISIBLE);
        if (holder.btnDecline instanceof ImageButton) {
            ((ImageButton) holder.btnDecline).setImageResource(R.drawable.ic_close);
        }

        if ("group".equals(req.getType())) {
            holder.tvName.setText(req.getUsername());
            holder.tvInfo.setText("Zaproszenie do grupy");
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_group_add)
                    .placeholder(R.drawable.ic_people)
                    .into(holder.ivAvatar);
        } else if ("accepted".equals(req.getType())) {
            holder.tvName.setText(req.getUsername());
            holder.tvInfo.setText("Zaakceptował Twoje zaproszenie!");
            holder.btnAccept.setVisibility(View.GONE);
            if (req.getAvatarUrl() != null && !req.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(req.getAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person);
            }
        } else {
            // Friend request
            holder.tvName.setText(req.getUsername());
            holder.tvInfo.setText("Chce dodać Cię do znajomych");
            if (holder.btnAccept instanceof ImageButton) {
                ((ImageButton) holder.btnAccept).setImageResource(R.drawable.ic_check_circle);
            }
            
            if (req.getAvatarUrl() != null && !req.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(req.getAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person);
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
        TextView tvName, tvInfo;
        ImageView ivAvatar;
        View btnAccept, btnDecline;

        RequestViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRequestName);
            tvInfo = v.findViewById(R.id.tvRequestInfo);
            ivAvatar = v.findViewById(R.id.ivRequestAvatar);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline = v.findViewById(R.id.btnDecline);
        }
    }
}
