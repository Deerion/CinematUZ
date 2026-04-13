package com.example.cinematuz.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private MovieGridAdapter adapter;
    private View rootView; // Caching widoku powraca!

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Wracamy do Twojego patentu. Generujemy ciężki widok tylko za pierwszym wejściem.
        if (rootView == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();

            // Te rzeczy też ustalamy tylko raz na całe życie aplikacji!
            setupRecyclerView();
            setupInitialState();
            setupListeners();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Obserwatorów odpalamy za każdym razem (ponieważ LifecycleOwner się odświeża po powrocie),
        // ale dzięki zamrożonemu rootView, dane wstrzykną się w ułamek milisekundy.
        setupObservers();

        // Strzał do API tylko za pierwszym razem
        if (viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty()) {
            String lang = getResources().getConfiguration().locale.getLanguage().equals("pl") ? "pl-PL" : "en-US";
            viewModel.fetchTrending(lang);
        }
    }

    private void setupRecyclerView() {
        adapter = new MovieGridAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("MEDIA_ITEM", item);
            Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
        });

        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        binding.rvTrending.setHasFixedSize(true);
        binding.rvTrending.setItemViewCacheSize(20);
        binding.rvTrending.setDrawingCacheEnabled(true);
        binding.rvTrending.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        binding.rvTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvTrending.setAdapter(adapter);
    }

    private void setupInitialState() {
        binding.rvTrending.setVisibility(View.GONE);
    }

    private void hideSkeletonsInstantly() {
        if (binding == null) return;
        binding.layoutSkeletonHero.getRoot().setVisibility(View.GONE);
        binding.layoutSkeletonTrending.getRoot().setVisibility(View.GONE);
        binding.layoutHeroMovie.getRoot().setVisibility(View.VISIBLE);
        binding.rvTrending.setVisibility(View.VISIBLE);
    }

    private void setupObservers() {
        viewModel.heroItem.observe(getViewLifecycleOwner(), this::updateHeroUi);

        viewModel.trendingList.observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                adapter.submitList(list);
                hideSkeletonsInstantly();
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading && (viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty())) {
                binding.layoutSkeletonHero.getRoot().setVisibility(View.VISIBLE);
                binding.layoutSkeletonTrending.getRoot().setVisibility(View.VISIBLE);
            } else if (!loading) {
                hideSkeletonsInstantly();
            }
        });
    }

    private void setupListeners() {
        binding.btnFilterAll.setOnClickListener(v -> viewModel.applyFilter("all"));
        binding.btnFilterMovies.setOnClickListener(v -> viewModel.applyFilter("movie"));
        binding.btnFilterTv.setOnClickListener(v -> viewModel.applyFilter("tv"));

        binding.layoutHeroMovie.btnDetails.setOnClickListener(v -> {
            MediaItem hero = viewModel.heroItem.getValue();
            if (hero != null) {
                Bundle b = new Bundle();
                b.putSerializable("MEDIA_ITEM", hero);
                Navigation.findNavController(v).navigate(R.id.detailsFragment, b);
            }
        });
    }

    private void updateHeroUi(MediaItem item) {
        if (item == null || binding == null) return;
        binding.layoutHeroMovie.tvHeroTitle.setText(item.getTitle());
        binding.layoutHeroMovie.tvHeroRating.setText(String.format("%.1f", item.getVoteAverage()));

        Glide.with(this)
                .load("https://image.tmdb.org/t/p/w780" + item.getPosterPath())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate() // Wyłączona animacja dla błyskawicznego powrotu
                .placeholder(R.drawable.hero_cinema)
                .into(binding.layoutHeroMovie.ivHeroPoster);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // CELOWO PUSTE: Zostawiamy referencje do rootView.
        // Odcięcie ich tutaj wymusiłoby generowanie widoku od nowa (i wywołało te sekundowe lagi).
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // BEZPIECZEŃSTWO: Niszczymy referencje w momencie niszczenia całego Fragmentu (np. przy zamknięciu aplikacji lub wycieku Activity).
        // Dzięki temu osiągamy natychmiastowe ładowanie, unikając równocześnie groźnych wycieków pamięci!
        binding = null;
        rootView = null;
    }
}