package com.example.cinematuz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Cast;

import java.util.ArrayList;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private List<Cast> castList = new ArrayList<>();

    public void setCastList(List<Cast> list) {
        this.castList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast member = castList.get(position);
        holder.tvName.setText(member.getName());

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w185" + member.getProfilePath())
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return castList != null ? castList.size() : 0;
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName;

        CastViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_cast_photo);
            tvName = itemView.findViewById(R.id.tv_cast_name);
        }
    }
}