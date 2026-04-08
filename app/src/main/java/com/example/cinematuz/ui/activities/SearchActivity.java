package com.example.cinematuz.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.remote.RetrofitClient;
import com.example.cinematuz.data.remote.TmdbApi;
import com.example.cinematuz.ui.adapters.SearchResultsAdapter;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

	public static final String EXTRA_QUERY = "SEARCH_QUERY";

	private final List<MediaItem> searchResults = new ArrayList<>();
	private SearchResultsAdapter adapter;
	private Call<ApiResponse<MediaItem>> activeCall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeHelper.applyTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		ImageButton btnBack = findViewById(R.id.btnBack);
		ImageButton btnFilter = findViewById(R.id.btnFilter);
		EditText etSearch = findViewById(R.id.etSearch);
		RecyclerView rvSearchResults = findViewById(R.id.rvSearchResults);

		adapter = new SearchResultsAdapter(new SearchResultsAdapter.OnResultActionListener() {
			@Override
			public void onItemClick(MediaItem item) {
				Intent intent = new Intent(SearchActivity.this, DetailsActivity.class);
				intent.putExtra("MEDIA_ITEM", item);
				startActivity(intent);
			}

			@Override
			public void onAddClick(MediaItem item) {
				Toast.makeText(SearchActivity.this, getString(R.string.cd_add_result), Toast.LENGTH_SHORT).show();
			}
		});

		rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
		rvSearchResults.setAdapter(adapter);

		btnBack.setOnClickListener(v -> finish());
		btnFilter.setOnClickListener(v -> Toast.makeText(this, getString(R.string.cd_filter_results), Toast.LENGTH_SHORT).show());

		etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				performSearch(etSearch.getText() != null ? etSearch.getText().toString().trim() : "");
				return true;
			}
			return false;
		});
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				performSearch(s.toString().trim());
			}
			@Override public void afterTextChanged(Editable s) { }
		});

		String initialQuery = getIntent().getStringExtra(EXTRA_QUERY);
		if (initialQuery != null && !initialQuery.isEmpty()) {
			etSearch.setText(initialQuery);
			etSearch.setSelection(initialQuery.length());
			performSearch(initialQuery);
		}
	}

	private void performSearch(String query) {
		if (activeCall != null) {
			activeCall.cancel();
			activeCall = null;
		}

		if (query == null || query.trim().length() < 2) {
			searchResults.clear();
			adapter.submitList(searchResults);
			return;
		}

		TmdbApi api = RetrofitClient.getClient().create(TmdbApi.class);
		String currentLang = getResources().getConfiguration().getLocales().get(0).getLanguage();
		String apiLang = "pl".equals(currentLang) ? "pl-PL" : "en-US";

		activeCall = api.searchMulti(query.trim(), apiLang, 1);
		activeCall.enqueue(new Callback<ApiResponse<MediaItem>>() {
			@Override
			public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
				if (call.isCanceled()) return;

				if (response.isSuccessful() && response.body() != null) {
					List<MediaItem> filtered = new ArrayList<>();
					for (MediaItem item : response.body().getResults()) {
						if (item == null) continue;
						String mediaType = item.getMediaType();
						if (mediaType == null || "movie".equals(mediaType) || "tv".equals(mediaType)) {
							filtered.add(item);
						}
					}
					searchResults.clear();
					searchResults.addAll(filtered);
					adapter.submitList(searchResults);

					if (filtered.isEmpty()) {
						Toast.makeText(SearchActivity.this, "Brak wyników dla podanej frazy.", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(SearchActivity.this, "Nie udało się pobrać wyników wyszukiwania.", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
				if (!call.isCanceled()) {
					Toast.makeText(SearchActivity.this, "Błąd wyszukiwania: " + t.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (activeCall != null) {
			activeCall.cancel();
		}
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(LocaleHelper.onAttach(newBase));
	}
}

