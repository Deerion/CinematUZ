package com.example.cinematuz.ui.fragments.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.databinding.FragmentDetailsBinding;
import com.google.android.material.chip.Chip;

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private DetailsViewModel viewModel;
    private MediaItem mediaItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mediaItem = (MediaItem) getArguments().getSerializable("MEDIA_ITEM");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DetailsViewModel.class);

        setupObservers();

        if (mediaItem != null) {
            bindBasicInfo();
            String lang = getResources().getConfiguration().locale.getLanguage().equals("pl") ? "pl-PL" : "en-US";
            viewModel.loadData(mediaItem.getId(), mediaItem.getMediaType(), lang);
        }

        binding.btnBackCustom.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.fabPlay.setOnClickListener(v -> viewModel.fetchTrailer(mediaItem.getId(), mediaItem.getMediaType()));
    }

    private void setupObservers() {
        viewModel.cast.observe(getViewLifecycleOwner(), list -> {
            CastAdapter adapter = new CastAdapter();
            binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvCast.setAdapter(adapter);
            adapter.setCastList(list);
        });

        viewModel.fullDetails.observe(getViewLifecycleOwner(), details -> {
            if (details.getGenres() != null) {
                binding.cgGenres.removeAllViews();
                for (MediaItem.Genre g : details.getGenres()) {
                    Chip chip = new Chip(getContext());
                    chip.setText(g.getName());
                    binding.cgGenres.addView(chip);
                }
            }
        });

        viewModel.trailerKey.observe(getViewLifecycleOwner(), this::openYoutube);
    }

    private void bindBasicInfo() {
        binding.tvDetailsTitle.setText(mediaItem.getTitle());
        binding.tvDetailsOverview.setText(mediaItem.getOverview());
        Glide.with(this).load("https://image.tmdb.org/t/p/w1280" + mediaItem.getBackdropPath()).into(binding.ivBackdrop);
    }

    private void openYoutube(String key) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + key)));
    }
}