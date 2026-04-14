package com.example.cinematuz.ui.fragments.home.search;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.repositories.MovieRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.text.TextUtils;
import com.example.cinematuz.data.models.FilterCriteria;

public class SearchViewModel extends ViewModel {

    private final MovieRepository repository = new MovieRepository();

    private final MutableLiveData<List<MediaItem>> _searchResults = new MutableLiveData<>();
    public LiveData<List<MediaItem>> searchResults = _searchResults;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public void onSearchTextChanged(String query) {
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        if (query == null || query.trim().isEmpty()) {
            _searchResults.setValue(null);
            _isLoading.setValue(false);
            return;
        }

        // Opóźnienie 500ms (Debounce)
        searchRunnable = () -> performSearch(query.trim());
        handler.postDelayed(searchRunnable, 500);
    }

    private void performSearch(String query) {
        _isLoading.setValue(true);
        fetchMergedSearchResults(query, new Callback<List<MediaItem>>() {
            @Override
            public void onResponse(Call<List<MediaItem>> call, Response<List<MediaItem>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    _searchResults.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<MediaItem>> call, Throwable t) {
                _isLoading.setValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
    }

    public void applyAdvancedFilters(FilterCriteria criteria, String query, String lang) {
        if (query != null && !query.trim().isEmpty()) {
            applyFiltersOnSearchResults(criteria, query.trim(), lang);
            return;
        }

        if (criteria != null && "all".equals(criteria.contentType)) {
            discoverAllContent(criteria, lang);
            return;
        }

        _isLoading.setValue(true);

        String dateFrom = criteria.yearFrom + "-01-01";
        String dateTo = criteria.yearTo + "-12-31";

        // Konwersja listy ID gatunków na string oddzielony przecinkami np. "28,878"
        String genresString = TextUtils.join(",", criteria.genreIds);

        repository.discoverContent(
                criteria.contentType,
                lang,
                criteria.sortBy,
                dateFrom,
                dateTo,
                criteria.minRating,
                genresString,
                new Callback<ApiResponse<MediaItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                        _isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<MediaItem> results = response.body().getResults();
                            if (results != null) {
                                for (MediaItem item : results) {
                                    if (item.getMediaType() == null || item.getMediaType().trim().isEmpty()) {
                                        item.setMediaType(criteria.contentType);
                                    }
                                }
                            }
                            _searchResults.setValue(results);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                        _isLoading.setValue(false);
                    }
                }
        );
    }

    private void discoverAllContent(FilterCriteria criteria, String lang) {
        _isLoading.setValue(true);

        String dateFrom = criteria.yearFrom + "-01-01";
        String dateTo = criteria.yearTo + "-12-31";
        String genresString = TextUtils.join(",", criteria.genreIds);

        List<MediaItem> mergedResults = new ArrayList<>();
        AtomicInteger completedCalls = new AtomicInteger(0);

        Callback<ApiResponse<MediaItem>> movieCallback = new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    List<MediaItem> results = response.body().getResults();
                    for (MediaItem item : results) {
                        if (item.getMediaType() == null || item.getMediaType().trim().isEmpty()) {
                            item.setMediaType("movie");
                        }
                    }
                    mergedResults.addAll(results);
                }
                finishIfReady();
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                finishIfReady();
            }

            private void finishIfReady() {
                if (completedCalls.incrementAndGet() < 2) return;

                sortByCriteria(mergedResults, criteria.sortBy);
                _isLoading.setValue(false);
                _searchResults.setValue(aggregateByTitle(mergedResults));
            }
        };

        Callback<ApiResponse<MediaItem>> tvCallback = new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    List<MediaItem> results = response.body().getResults();
                    for (MediaItem item : results) {
                        if (item.getMediaType() == null || item.getMediaType().trim().isEmpty()) {
                            item.setMediaType("tv");
                        }
                    }
                    mergedResults.addAll(results);
                }
                if (completedCalls.incrementAndGet() < 2) return;

                sortByCriteria(mergedResults, criteria.sortBy);
                _isLoading.setValue(false);
                _searchResults.setValue(aggregateByTitle(mergedResults));
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                if (completedCalls.incrementAndGet() < 2) return;

                sortByCriteria(mergedResults, criteria.sortBy);
                _isLoading.setValue(false);
                _searchResults.setValue(aggregateByTitle(mergedResults));
            }
        };

        repository.discoverContent("movie", lang, mapSortForDiscover("movie", criteria.sortBy), dateFrom, dateTo,
                criteria.minRating, genresString, movieCallback);
        repository.discoverContent("tv", lang, mapSortForDiscover("tv", criteria.sortBy), dateFrom, dateTo,
                criteria.minRating, genresString, tvCallback);
    }

    private String mapSortForDiscover(String contentType, String sortBy) {
        if ("tv".equals(contentType) && "primary_release_date.desc".equals(sortBy)) {
            return "first_air_date.desc";
        }
        if ("movie".equals(contentType) && "first_air_date.desc".equals(sortBy)) {
            return "primary_release_date.desc";
        }
        return sortBy;
    }


    private void applyFiltersOnSearchResults(FilterCriteria criteria, String query, String lang) {
        _isLoading.setValue(true);
        fetchMergedSearchResults(query, new Callback<List<MediaItem>>() {
            @Override
            public void onResponse(Call<List<MediaItem>> call, Response<List<MediaItem>> response) {
                _isLoading.setValue(false);
                if (!response.isSuccessful() || response.body() == null) {
                    _searchResults.setValue(Collections.emptyList());
                    return;
                }

                List<MediaItem> source = response.body();
                List<MediaItem> filtered = applyCriteria(source, criteria, query);
                _searchResults.setValue(aggregateByTitle(filtered));
            }

            @Override
            public void onFailure(Call<List<MediaItem>> call, Throwable t) {
                _isLoading.setValue(false);
            }
        });
    }

    private void fetchMergedSearchResults(String query, Callback<List<MediaItem>> callback) {
        repository.searchMulti(query, "pl-PL", 1, new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> plResponse) {
                if (!plResponse.isSuccessful() || plResponse.body() == null) {
                    callback.onFailure(null, new RuntimeException("PL search failed"));
                    return;
                }

                repository.searchMulti(query, "en-US", 1, new Callback<ApiResponse<MediaItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> enResponse) {
                        if (!enResponse.isSuccessful() || enResponse.body() == null) {
                            callback.onFailure(null, new RuntimeException("EN search failed"));
                            return;
                        }

                        List<MediaItem> merged = mergeByMediaId(plResponse.body().getResults(), enResponse.body().getResults(), query);
                        callback.onResponse(null, Response.success(merged));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                        callback.onFailure(null, t);
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                callback.onFailure(null, t);
            }
        });
    }

    private List<MediaItem> mergeByMediaId(List<MediaItem> plItems, List<MediaItem> enItems, String query) {
        Map<String, MediaItem> merged = new LinkedHashMap<>();
        putItemsWithPriority(merged, plItems, query);
        putItemsWithPriority(merged, enItems, query);
        return new ArrayList<>(merged.values());
    }

    private void putItemsWithPriority(Map<String, MediaItem> target, List<MediaItem> items, String query) {
        if (items == null) return;

        for (MediaItem item : items) {
            if (item == null || item.getId() <= 0) continue;
            String type = item.getMediaType();
            if (!"movie".equals(type) && !"tv".equals(type)) continue;

            String key = type + "_" + item.getId();
            MediaItem current = target.get(key);
            if (current == null || isBetterForQuery(item, current, query)) {
                target.put(key, item);
            }
        }
    }

    private boolean isBetterForQuery(MediaItem first, MediaItem second, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String firstTitle = first.getTitle() == null ? "" : first.getTitle().toLowerCase(Locale.ROOT);
        String secondTitle = second.getTitle() == null ? "" : second.getTitle().toLowerCase(Locale.ROOT);

        boolean firstMatches = !normalizedQuery.isEmpty() && firstTitle.contains(normalizedQuery);
        boolean secondMatches = !normalizedQuery.isEmpty() && secondTitle.contains(normalizedQuery);

        if (firstMatches != secondMatches) {
            return firstMatches;
        }

        if (first.getVoteAverage() != second.getVoteAverage()) {
            return first.getVoteAverage() > second.getVoteAverage();
        }

        return extractYear(first.getReleaseDate()) > extractYear(second.getReleaseDate());
    }

    private List<MediaItem> applyCriteria(List<MediaItem> source, FilterCriteria criteria, String query) {
        List<MediaItem> result = new ArrayList<>();
        if (source == null) return result;

        String normalizedQuery = query == null ? "" : query.toLowerCase(Locale.ROOT);

        for (MediaItem item : source) {
            if (item == null) continue;

            String mediaType = item.getMediaType();
            if (!"movie".equals(mediaType) && !"tv".equals(mediaType)) continue;

            String title = item.getTitle();
            if (title == null || title.trim().isEmpty()) continue;
            if (!normalizedQuery.isEmpty() && !title.toLowerCase(Locale.ROOT).contains(normalizedQuery)) continue;

            if (criteria != null
                    && !TextUtils.isEmpty(criteria.contentType)
                    && !"all".equals(criteria.contentType)
                    && !criteria.contentType.equals(mediaType)) {
                continue;
            }

            int year = extractYear(item.getReleaseDate());
            if (criteria != null && year > 0 && (year < criteria.yearFrom || year > criteria.yearTo)) continue;

            if (criteria != null && item.getVoteAverage() < criteria.minRating) continue;

            if (criteria != null && criteria.genreIds != null && !criteria.genreIds.isEmpty()) {
                List<Integer> itemGenres = item.getGenreIds();
                if (itemGenres == null || itemGenres.isEmpty()) continue;

                boolean anyMatch = false;
                for (Integer selectedGenre : criteria.genreIds) {
                    if (itemGenres.contains(selectedGenre)) {
                        anyMatch = true;
                        break;
                    }
                }
                if (!anyMatch) continue;
            }

            result.add(item);
        }

        if (criteria != null) {
            sortByCriteria(result, criteria.sortBy);
        }

        return result;
    }

    private void sortByCriteria(List<MediaItem> items, String sortBy) {
        if (items == null || items.size() < 2 || sortBy == null) return;

        if ("vote_average.desc".equals(sortBy)) {
            items.sort((a, b) -> Double.compare(b.getVoteAverage(), a.getVoteAverage()));
            return;
        }

        if ("primary_release_date.desc".equals(sortBy) || "first_air_date.desc".equals(sortBy)) {
            items.sort(Comparator.comparingInt((MediaItem item) -> extractYear(item.getReleaseDate())).reversed());
        }
    }

    private List<MediaItem> aggregateByTitle(List<MediaItem> items) {
        Map<String, MediaItem> byTitle = new LinkedHashMap<>();
        if (items == null) return new ArrayList<>();

        for (MediaItem item : items) {
            String type = item.getMediaType() == null ? "" : item.getMediaType().trim().toLowerCase(Locale.ROOT);
            String titleKey = item.getTitle() == null ? "" : item.getTitle().trim().toLowerCase(Locale.ROOT);
            String key = type + "#" + titleKey;
            if (titleKey.isEmpty()) continue;

            MediaItem current = byTitle.get(key);
            if (current == null || isBetterCandidate(item, current)) {
                byTitle.put(key, item);
            }
        }

        return new ArrayList<>(byTitle.values());
    }

    private boolean isBetterCandidate(MediaItem first, MediaItem second) {
        if (first.getVoteAverage() != second.getVoteAverage()) {
            return first.getVoteAverage() > second.getVoteAverage();
        }
        return extractYear(first.getReleaseDate()) > extractYear(second.getReleaseDate());
    }

    private int extractYear(String date) {
        if (date == null || date.length() < 4) return -1;
        try {
            return Integer.parseInt(date.substring(0, 4));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}