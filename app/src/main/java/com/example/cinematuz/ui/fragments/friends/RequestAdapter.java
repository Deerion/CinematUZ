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

        if ("group_removal".equals(req.getType())) {
            // ZWYKŁA INFORMACJA O WYRZUCENIU: Ukrywamy awatar, przycisk akceptacji ORAZ podtytuł
            holder.ivAvatar.setVisibility(View.GONE);
            holder.tvName.setText(req.getUsername()); // Tu jest np. "Usunięto z grupy: Matrix"
            holder.tvRequestInfo.setVisibility(View.GONE); // UKRYWAMY TEKST POMOCNICZY
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.VISIBLE);

        } else if ("group".equals(req.getType())) {
            // ZAPROSZENIE DO GRUPY: Ukrywamy awatar, pokazujemy przyciski i zmieniamy tekst
            holder.ivAvatar.setVisibility(View.GONE);
            holder.tvName.setText(req.getUsername()); // Tu jest nazwa grupy
            holder.tvRequestInfo.setVisibility(View.VISIBLE);
            holder.tvRequestInfo.setText("Zaprasza Cię do grupy"); // ODPOWIEDNI TEKST
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);

        } else {
            // ZAPROSZENIE DO ZNAJOMYCH: Pokazujemy wszystko i ustawiamy standardowy tekst
            holder.ivAvatar.setVisibility(View.VISIBLE);
            holder.tvName.setText(req.getUsername());
            holder.tvRequestInfo.setVisibility(View.VISIBLE);
            holder.tvRequestInfo.setText("Chce dodać Cię do znajomych"); // STANDARDOWY TEKST
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);

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
        TextView tvRequestInfo; // DODANO ZMIENNĄ DLA TEKSTU POMOCNICZEGO
        ImageView ivAvatar;
        View btnAccept, btnDecline;

        RequestViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRequestName);
            tvRequestInfo = v.findViewById(R.id.tvRequestInfo); // PODPIĘCIE WIDOKU
            ivAvatar = v.findViewById(R.id.ivRequestAvatar);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline = v.findViewById(R.id.btnDecline);
        }
    }
}