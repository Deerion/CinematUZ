package com.example.cinematuz.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// UWAGA: Dodano implements FriendsAdapter.OnFriendActionListener
public class FriendsFragment extends Fragment implements FriendsAdapter.OnFriendActionListener {

    private RecyclerView recyclerViewFriends;
    private FriendsAdapter friendsAdapter;
    private TextView tvFriendsCount;
    private TextView tvInvitationsTitle;
    private TextView tvGroupsPlaceholder;
    private View layoutFriendRequest;
    private List<Friend> mockFriends;
    private List<String> searchableUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        // Inicjalizacja widoków
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);
        tvFriendsCount = view.findViewById(R.id.tvFriendsCount);
        tvInvitationsTitle = view.findViewById(R.id.tvInvitationsTitle);
        tvGroupsPlaceholder = view.findViewById(R.id.tvGroupsPlaceholder);
        layoutFriendRequest = view.findViewById(R.id.layoutFriendRequest);
        FloatingActionButton fab = view.findViewById(R.id.fabAddFriend);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupFriends);
        View btnScanQr = view.findViewById(R.id.btnScanQr);
        View btnMyQr = view.findViewById(R.id.btnMyQr);
        View layoutBluetoothNearby = view.findViewById(R.id.layoutBluetoothNearby);

        searchableUsers = getSearchableUsers();

        if (fab != null) {
            fab.setOnClickListener(v -> showSearchFriendsDialog());
        }

        if (btnScanQr != null) {
            btnScanQr.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.friends_scan_qr_soon), Toast.LENGTH_SHORT).show());
        }

        if (btnMyQr != null) {
            btnMyQr.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.friends_my_qr_soon), Toast.LENGTH_SHORT).show());
        }

        if (layoutBluetoothNearby != null) {
            layoutBluetoothNearby.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.friends_bluetooth_soon), Toast.LENGTH_SHORT).show());
        }

        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    updateTabState(checkedId == R.id.btnTabFriends);
                }
            });
        }

        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        mockFriends = getMockFriends();
        updateFriendsCount();

        friendsAdapter = new FriendsAdapter(mockFriends, this);
        recyclerViewFriends.setAdapter(friendsAdapter);
        updateTabState(true);

        return view;
    }

    @Override
    public void onRemoveFriend(Friend friend, int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.friends_remove_title)
                .setMessage(getString(R.string.friends_remove_message, friend.getName()))
                .setNegativeButton(R.string.profile_cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.friends_remove_confirm, (dialog, which) -> {
                    mockFriends.remove(position);
                    friendsAdapter.notifyItemRemoved(position);
                    updateFriendsCount();
                    Toast.makeText(getContext(), getString(R.string.friends_removed_toast), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void updateTabState(boolean isFriendsTab) {
        if (tvInvitationsTitle != null) {
            tvInvitationsTitle.setVisibility(isFriendsTab ? View.VISIBLE : View.GONE);
        }
        if (layoutFriendRequest != null) {
            layoutFriendRequest.setVisibility(isFriendsTab ? View.VISIBLE : View.GONE);
        }
        if (recyclerViewFriends != null) {
            recyclerViewFriends.setVisibility(isFriendsTab ? View.VISIBLE : View.GONE);
        }
        if (tvFriendsCount != null) {
            tvFriendsCount.setVisibility(isFriendsTab ? View.VISIBLE : View.GONE);
        }
        if (tvGroupsPlaceholder != null) {
            tvGroupsPlaceholder.setVisibility(isFriendsTab ? View.GONE : View.VISIBLE);
        }
    }

    private void updateFriendsCount() {
        if (tvFriendsCount != null) {
            tvFriendsCount.setText(getString(R.string.friends_active_count_dynamic, mockFriends.size()));
        }
    }

    private void showSearchFriendsDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_friends, null);
        EditText etSearch = dialogView.findViewById(R.id.etFriendSearch);
        RecyclerView rvSuggestions = dialogView.findViewById(R.id.rvFriendSuggestions);
        TextView tvNoResults = dialogView.findViewById(R.id.tvNoResults);

        List<String> filtered = new ArrayList<>(searchableUsers);
        SuggestionAdapter suggestionAdapter = new SuggestionAdapter(filtered, username -> {
            if (isAlreadyFriend(username)) {
                Toast.makeText(getContext(), getString(R.string.friends_already_added, username), Toast.LENGTH_SHORT).show();
                return;
            }

            mockFriends.add(0, new Friend(username, false));
            friendsAdapter.notifyItemInserted(0);
            if (recyclerViewFriends != null) {
                recyclerViewFriends.scrollToPosition(0);
            }
            updateFriendsCount();
            Toast.makeText(getContext(), getString(R.string.friends_added_toast, username), Toast.LENGTH_SHORT).show();
        });

        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(suggestionAdapter);

        tvNoResults.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggestions = filterUsers(s == null ? "" : s.toString());
                suggestionAdapter.submitList(suggestions);
                tvNoResults.setVisibility(suggestions.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.friends_search_dialog_title)
                .setView(dialogView)
                .setNegativeButton(R.string.profile_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean isAlreadyFriend(String username) {
        for (Friend friend : mockFriends) {
            if (friend.getName().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    private List<String> filterUsers(String query) {
        String normalized = query.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return new ArrayList<>(searchableUsers);
        }

        List<String> suggestions = new ArrayList<>();
        for (String user : searchableUsers) {
            String username = user.toLowerCase();
            if (username.contains(normalized)) {
                suggestions.add(user);
            }
        }

        Collections.sort(suggestions, Comparator.comparingInt(name -> {
            String lowercase = name.toLowerCase();
            if (lowercase.startsWith(normalized)) {
                return 0;
            }
            return 1;
        }));
        return suggestions;
    }

    private List<Friend> getMockFriends() {
        List<Friend> list = new ArrayList<>();
        list.add(new Friend("Alex_Cinema", true));
        list.add(new Friend("Sarah_Noir", false));
        list.add(new Friend("MovieBuff_99", true));
        list.add(new Friend("Cinephile_Uz", false));
        list.add(new Friend("Jan_Kowalski", false));
        list.add(new Friend("Anna_Nowak", true));
        return list;
    }

    private List<String> getSearchableUsers() {
        List<String> users = new ArrayList<>();
        users.add("Alex_Cinema");
        users.add("Sarah_Noir");
        users.add("MovieBuff_99");
        users.add("Cinephile_Uz");
        users.add("Jan_Kowalski");
        users.add("Anna_Nowak");
        users.add("KinoRider");
        users.add("NoirWave");
        users.add("RetroReel");
        users.add("FrameHunter");
        users.add("Popcorn_Guru");
        users.add("SilentCredits");
        return users;
    }

    private static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

        interface OnSuggestionClickListener {
            void onSuggestionClick(String username);
        }

        private final List<String> items = new ArrayList<>();
        private final OnSuggestionClickListener onSuggestionClickListener;

        SuggestionAdapter(List<String> initialItems, OnSuggestionClickListener onSuggestionClickListener) {
            items.addAll(initialItems);
            this.onSuggestionClickListener = onSuggestionClickListener;
        }

        void submitList(List<String> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_suggestion, parent, false);
            return new SuggestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
            String username = items.get(position);
            holder.tvName.setText(username);
            holder.itemView.setOnClickListener(v -> onSuggestionClickListener.onSuggestionClick(username));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class SuggestionViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvName;

            SuggestionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvSuggestionName);
            }
        }
    }
}