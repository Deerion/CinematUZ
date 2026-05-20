package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.SearchResultUser;
import com.example.cinematuz.ui.fragments.friends.znajomi.BluetoothDeviceAdapter;
import com.example.cinematuz.ui.fragments.home.search.SearchResultAdapter;
import com.example.cinematuz.utils.NearbyHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GroupDetailsFragment extends Fragment implements SensorEventListener {

    private String groupId;
    private FirebaseFirestore db;
    private ListenerRegistration groupListener;
    private ListenerRegistration moviesListener;

    private TextView tvGroupName, tvMemberCount;
    private RecyclerView rvMembers, rvMovies;
    private GroupMemberCircleAdapter membersAdapter;
    private AdvancedGroupMoviesAdapter moviesAdapter;

    private List<Friend> membersList = new ArrayList<>();
    private List<MediaItem> movieProposals = new ArrayList<>();
    private Map<Integer, List<String>> votesMap = new HashMap<>();
    private int totalGroupMembers = 1;

    private View btnDeleteGroup, btnLeaveGroup, layoutManagement;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long mShakeTimestamp;
    private NearbyHelper nearbyHelper;
    private List<SearchResultUser> nearbyUsers = new ArrayList<>();
    private BluetoothDeviceAdapter btAdapter;
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) groupId = getArguments().getString("GROUP_ID");
        db = FirebaseFirestore.getInstance();

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_details, container, false);

        tvGroupName = view.findViewById(R.id.tvDetailsGroupName);
        tvMemberCount = view.findViewById(R.id.tvDetailsMemberCount);
        btnDeleteGroup = view.findViewById(R.id.btnDeleteGroup);
        btnLeaveGroup = view.findViewById(R.id.btnLeaveGroup);
        layoutManagement = view.findViewById(R.id.layoutManagement);

        View btnAddMember = view.findViewById(R.id.btnDetailsAddMember);
        View btnBluetooth = view.findViewById(R.id.btnDetailsBluetooth);
        View btnAddMovies = view.findViewById(R.id.btnDetailsAddMovies);

        View btnBack = view.findViewById(R.id.btnBack);
        if(btnBack != null){
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        rvMembers = view.findViewById(R.id.rvGroupMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        membersAdapter = new GroupMemberCircleAdapter(membersList);
        rvMembers.setAdapter(membersAdapter);

        rvMovies = view.findViewById(R.id.rvGroupMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        moviesAdapter = new AdvancedGroupMoviesAdapter();
        rvMovies.setAdapter(moviesAdapter);

        setupClickListeners(btnAddMember, btnBluetooth, btnAddMovies, btnDeleteGroup, btnLeaveGroup);
        listenToGroupChanges();
        listenToMovieProposals();

        return view;
    }

    private void wylosujFilmZGrupy() {
        if (movieProposals == null || movieProposals.isEmpty()) return;
        Random random = new Random();
        MediaItem selected = movieProposals.get(random.nextInt(movieProposals.size()));
        Toast.makeText(getContext(), "Wylosowano: " + selected.getTitle(), Toast.LENGTH_LONG).show();
        Bundle bundle = new Bundle();
        bundle.putSerializable("MEDIA_ITEM", selected);
        Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0] / SensorManager.GRAVITY_EARTH;
            float y = event.values[1] / SensorManager.GRAVITY_EARTH;
            float z = event.values[2] / SensorManager.GRAVITY_EARTH;
            float gForce = (float) Math.sqrt(x * x + y * y + z * z);
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return;
                mShakeTimestamp = now;
                wylosujFilmZGrupy();
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override public void onResume() { super.onResume(); if (sensorManager != null && accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI); }
    @Override public void onPause() { if (sensorManager != null) sensorManager.unregisterListener(this); super.onPause(); }

    private void showRemoveMovieDialog(MediaItem movie) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Usuń film")
                .setMessage("Czy chcesz usunąć film \"" + movie.getTitle() + "\" z listy propozycji?")
                .setPositiveButton("Usuń", (dialog, which) -> removeMovieFromGroup(movie))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void removeMovieFromGroup(MediaItem movie) {
        if (groupId == null) return;
        db.collection("groups").document(groupId).collection("movies").document(String.valueOf(movie.getId())).delete();
    }

    private void toggleVote(MediaItem movie, boolean currentlyVoted) {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null || groupId == null) return;

        List<String> voters = new ArrayList<>(votesMap.getOrDefault(movie.getId(), new ArrayList<>()));
        if (currentlyVoted) voters.remove(myUid);
        else if (!voters.contains(myUid)) voters.add(myUid);

        votesMap.put(movie.getId(), voters);
        sortAndSubmitMovies();

        db.collection("groups").document(groupId).collection("movies").document(String.valueOf(movie.getId()))
                .update("votedBy", currentlyVoted ? FieldValue.arrayRemove(myUid) : FieldValue.arrayUnion(myUid));
    }

    private void listenToMovieProposals() {
        if (groupId == null) return;
        moviesListener = db.collection("groups").document(groupId).collection("movies")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    movieProposals.clear();
                    votesMap.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        MediaItem item = doc.toObject(MediaItem.class);
                        if (doc.contains("tmdbId")) {
                            item.setId(doc.getLong("tmdbId").intValue());
                        }

                        Object rawVoters = doc.get("votedBy");
                        List<String> voters = new ArrayList<>();
                        if (rawVoters instanceof List) {
                            for (Object v : (List<?>) rawVoters) {
                                if (v instanceof String) {
                                    voters.add((String) v);
                                }
                            }
                        }

                        movieProposals.add(item);
                        votesMap.put(item.getId(), voters);
                    }
                    sortAndSubmitMovies();
                });
    }

    private void sortAndSubmitMovies() {
        movieProposals.sort((m1, m2) -> {
            int v1 = votesMap.containsKey(m1.getId()) ? votesMap.get(m1.getId()).size() : 0;
            int v2 = votesMap.containsKey(m2.getId()) ? votesMap.get(m2.getId()).size() : 0;
            return Integer.compare(v2, v1);
        });
        moviesAdapter.setGroupData(movieProposals, votesMap, membersList, totalGroupMembers);
    }

    private void listenToGroupChanges() {
        if (groupId == null) return;
        groupListener = db.collection("groups").document(groupId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    com.example.cinematuz.data.models.Group group = snapshot.toObject(com.example.cinematuz.data.models.Group.class);
                    if (group != null) {
                        tvGroupName.setText(group.getName());
                        List<String> memberUids = group.getMembers();
                        totalGroupMembers = Math.max(1, memberUids.size());
                        tvMemberCount.setText(totalGroupMembers + " członków");

                        String myUid = FirebaseAuth.getInstance().getUid();

                        if (myUid != null && myUid.equals(group.getOwnerId())) {
                            btnDeleteGroup.setVisibility(View.VISIBLE);
                            btnLeaveGroup.setVisibility(View.GONE);
                        } else {
                            btnDeleteGroup.setVisibility(View.GONE);
                            btnLeaveGroup.setVisibility(View.VISIBLE);
                        }

                        layoutManagement.setVisibility(View.VISIBLE);

                        membersAdapter.setOwnerId(group.getOwnerId());
                        fetchMemberProfiles(memberUids);
                    }
                });
    }

    private void fetchMemberProfiles(List<String> uids) {
        membersList.clear();
        for (String uid : uids) {
            db.collection("profiles").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && isAdded()) {
                    // POPRAWKA: Używamy doc.getString("username") oraz doc.getString("avatar_url")
                    membersList.add(new Friend(
                            doc.getId(),
                            doc.getString("username"),
                            doc.getString("avatar_url"),
                            Boolean.TRUE.equals(doc.getBoolean("isOnline"))
                    ));
                    membersAdapter.notifyDataSetChanged();
                    sortAndSubmitMovies();
                }
            });
        }
    }

    private void setupClickListeners(View add, View bt, View movies, View delete, View leave) {
        add.setOnClickListener(v -> showInviteFriendDialog());
        bt.setOnClickListener(v -> checkPermissionsAndStartBluetooth());
        movies.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("TARGET_GROUP_ID", groupId);
            bundle.putBoolean("SELECTION_MODE", true);
            Navigation.findNavController(v).navigate(R.id.searchFragment, bundle);
        });
        delete.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext()).setTitle("Usuń grupę").setMessage("Czy na pewno chcesz usunąć tę grupę?").setPositiveButton("Usuń", (dialog, which) -> deleteGroup()).setNegativeButton("Anuluj", null).show());
        leave.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext()).setTitle("Opuść grupę").setMessage("Czy na pewno chcesz opuścić tę grupę?").setPositiveButton("Opuść", (dialog, which) -> leaveGroup()).setNegativeButton("Anuluj", null).show());
    }

    private void leaveGroup() {
        if (groupId == null) return;
        db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid())).addOnSuccessListener(aVoid -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void removeMemberFromGroup(Friend friend) {
        if (groupId == null) return;
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Usuń z grupy").setMessage("Czy na pewno chcesz wyrzucić " + friend.getName() + "?").setPositiveButton("Wyrzuć", (dialog, which) -> {
            db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(friend.getId()));
        }).setNegativeButton("Anuluj", null).show();
    }

    private void deleteGroup() {
        if (groupId == null) return;
        db.collection("groups").document(groupId).delete().addOnSuccessListener(aVoid -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void showInviteFriendDialog() {
        String myUid = FirebaseAuth.getInstance().getUid();
        // Pobieranie znajomych używa "name" i "avatarUrl" (kolekcja friends) - to było poprawne
        db.collection("profiles").document(myUid).collection("friends").whereEqualTo("status", "accepted").get().addOnSuccessListener(snaps -> {
            List<Friend> friends = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snaps) {
                Friend f = new Friend(doc.getId(), doc.getString("name"), doc.getString("avatarUrl"), true);
                friends.add(f);
                names.add(f.getName());
            }
            new MaterialAlertDialogBuilder(requireContext()).setTitle("Zaproś znajomego").setItems(names.toArray(new String[0]), (dialog, which) -> sendGroupInvite(friends.get(which).getId())).show();
        });
    }

    private void sendGroupInvite(String friendUid) {
        Map<String, Object> invite = new HashMap<>();
        invite.put("groupName", tvGroupName.getText().toString());
        invite.put("type", "group");
        db.collection("profiles").document(friendUid).collection("group_invites").document(groupId).set(invite);
    }

    @Override public void onDestroyView() { super.onDestroyView(); if (groupListener != null) groupListener.remove(); if (moviesListener != null) moviesListener.remove(); if (nearbyHelper != null) nearbyHelper.stopSearching(); }

    private void checkPermissionsAndStartBluetooth() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        List<String> missingPermissions = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(p);
            }
        }

        if (missingPermissions.isEmpty()) {
            startBluetoothSearch();
        } else {
            requestPermissions(missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    private void startBluetoothSearch() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nearbyUsers.clear();
        showBluetoothDiscoveryDialog();

        nearbyHelper = new NearbyHelper(requireContext(), myUid, discoveredUid -> {
            if (discoveredUid.equals(myUid)) return;
            for (SearchResultUser user : nearbyUsers) {
                if (user.getUid().equals(discoveredUid)) return;
            }

            db.collection("profiles").document(discoveredUid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && isAdded()) {
                    // POPRAWKA: Pobieramy username i avatar_url z bazy
                    SearchResultUser user = new SearchResultUser(
                            doc.getId(),
                            doc.getString("username"),
                            doc.getString("avatar_url")
                    );
                    nearbyUsers.add(user);
                    if (btAdapter != null) btAdapter.notifyDataSetChanged();
                }
            });
        });
        nearbyHelper.startSearching();
    }

    private void showBluetoothDiscoveryDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogStyle);
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bluetooth_discovery, null);
        dialog.setContentView(sheetView);

        RecyclerView rv = sheetView.findViewById(R.id.rvBluetoothDevices);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        btAdapter = new BluetoothDeviceAdapter(nearbyUsers, user -> {
            sendGroupInvite(user.getUid());
            Toast.makeText(getContext(), "Wysłano zaproszenie do grupy!", Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(btAdapter);

        sheetView.findViewById(R.id.btnCancelDiscovery).setOnClickListener(v -> {
            if (nearbyHelper != null) nearbyHelper.stopSearching();
            dialog.dismiss();
        });
        dialog.setOnDismissListener(d -> { if (nearbyHelper != null) nearbyHelper.stopSearching(); });
        dialog.show();
    }

    // --- ADAPTER DO KÓŁECZEK ---
    private class GroupMemberCircleAdapter extends RecyclerView.Adapter<GroupMemberCircleAdapter.ViewHolder> {
        private List<Friend> members; private String ownerId = "";
        public GroupMemberCircleAdapter(List<Friend> members) { this.members = members; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; notifyDataSetChanged(); }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member_circle, parent, false)); }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Friend friend = members.get(position);
            holder.tvName.setText(friend.getName());
            holder.ivStar.setVisibility((friend.getId() != null && friend.getId().equals(ownerId)) ? View.VISIBLE : View.GONE);

            if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(friend.getAvatarUrl())
                        .circleCrop() // Dodano circleCrop() dla pewności
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person);
            }

            holder.itemView.setOnClickListener(v -> {
                String myUid = FirebaseAuth.getInstance().getUid();
                if (myUid != null && myUid.equals(ownerId) && !friend.getId().equals(myUid)) removeMemberFromGroup(friend);
            });
        }
        @Override public int getItemCount() { return members.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivStar, ivAvatar; TextView tvName;
            ViewHolder(@NonNull View itemView) { super(itemView); ivStar = itemView.findViewById(R.id.ivOwnerStar); ivAvatar = itemView.findViewById(R.id.ivFriendAvatar); tvName = itemView.findViewById(R.id.tvFriendName); }
        }
    }

    // --- ZAAWANSOWANY ADAPTER KART FILMÓW ---
    private class AdvancedGroupMoviesAdapter extends RecyclerView.Adapter<AdvancedGroupMoviesAdapter.ViewHolder> {
        private List<MediaItem> movies = new ArrayList<>();
        private Map<Integer, List<String>> votes = new HashMap<>();
        private List<Friend> groupMembers = new ArrayList<>();
        private int maxMembers = 1;

        public void setGroupData(List<MediaItem> newMovies, Map<Integer, List<String>> newVotes, List<Friend> members, int maxMembers) {
            this.movies = new ArrayList<>(newMovies);
            this.votes = new HashMap<>(newVotes);
            this.groupMembers = members;
            this.maxMembers = maxMembers;
            notifyDataSetChanged();
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_movie, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MediaItem movie = movies.get(position);
            String myUid = FirebaseAuth.getInstance().getUid();
            List<String> voters = votes.getOrDefault(movie.getId(), new ArrayList<>());
            boolean iVoted = voters.contains(myUid);

            holder.tvTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Brak tytułu");

            String date = movie.getReleaseDate();
            if (date != null && date.length() >= 4) {
                holder.tvYear.setText(date.substring(0, 4));
            } else {
                holder.tvYear.setText("");
            }

            // --- POPRAWIONA LOGIKA: Tylko gatunek, bez fallbacku do typu filmu/serialu ---
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                holder.tvGenre.setText("• " + movie.getGenres().get(0).getName());
            } else {
                String mainGenre = SearchResultAdapter.getFirstGenreName(movie.getGenreIds(), holder.itemView.getContext());
                holder.tvGenre.setText(mainGenre.isEmpty() ? "" : "• " + mainGenre);
            }

            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load("https://image.tmdb.org/t/p/w200" + movie.getPosterPath())
                        .into(holder.ivPoster);
            }

            holder.tvVoteCount.setText(voters.size() + " głosów");
            holder.progressBar.setMax(maxMembers);
            holder.progressBar.setProgress(voters.size(), true);

            // --- IKONA CZERWONEGO SERCA ---
            holder.btnVote.setImageResource(iVoted ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline);
            holder.btnVote.setColorFilter(iVoted ? 0xFFFF0000 : 0xFF757575);
            holder.btnVote.setOnClickListener(v -> toggleVote(movie, iVoted));

            holder.btnDelete.setOnClickListener(v -> GroupDetailsFragment.this.showRemoveMovieDialog(movie));
            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("MEDIA_ITEM", movie);
                Navigation.findNavController(v).navigate(R.id.detailsFragment, bundle);
            });

            holder.ivVoter1.setVisibility(View.GONE);
            holder.ivVoter2.setVisibility(View.GONE);
            holder.ivVoter3.setVisibility(View.GONE);
            holder.tvExtraVoters.setVisibility(View.GONE);

            ImageView[] avatarViews = {holder.ivVoter1, holder.ivVoter2, holder.ivVoter3};
            int drawnAvatars = 0;

            for (String uid : voters) {
                if (drawnAvatars >= 3) break;
                String avatarUrl = null;
                for (Friend f : groupMembers) {
                    if (f.getId().equals(uid)) {
                        avatarUrl = f.getAvatarUrl(); break;
                    }
                }
                avatarViews[drawnAvatars].setVisibility(View.VISIBLE);
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                            .load(avatarUrl)
                            .circleCrop()
                            .into(avatarViews[drawnAvatars]);
                } else {
                    avatarViews[drawnAvatars].setImageResource(R.drawable.ic_person);
                }
                drawnAvatars++;
            }

            if (voters.size() > 3) {
                holder.tvExtraVoters.setVisibility(View.VISIBLE);
                holder.tvExtraVoters.setText("+" + (voters.size() - 3));
            }
        }

        @Override public int getItemCount() { return movies.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPoster, ivVoter1, ivVoter2, ivVoter3, btnVote, btnDelete;
            TextView tvTitle, tvYear, tvGenre, tvVoteCount, tvExtraVoters;
            LinearProgressIndicator progressBar;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPoster = itemView.findViewById(R.id.ivMoviePoster);
                tvTitle = itemView.findViewById(R.id.tvMovieTitle);
                tvYear = itemView.findViewById(R.id.tvMovieYear);
                tvGenre = itemView.findViewById(R.id.tvMovieGenre);
                tvVoteCount = itemView.findViewById(R.id.tvVoteCount);
                tvExtraVoters = itemView.findViewById(R.id.tvExtraVoters);
                ivVoter1 = itemView.findViewById(R.id.ivVoter1);
                ivVoter2 = itemView.findViewById(R.id.ivVoter2);
                ivVoter3 = itemView.findViewById(R.id.ivVoter3);
                progressBar = itemView.findViewById(R.id.progressVotes);
                btnVote = itemView.findViewById(R.id.btnVote);
                btnDelete = itemView.findViewById(R.id.btnOptions);
            }
        }
    }

}
