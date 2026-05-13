package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.ui.fragments.friends.znajomi.FriendsAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
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
    private FriendsAdapter membersAdapter;
    private GroupMoviesAdapter moviesAdapter;

    private List<Friend> membersList = new ArrayList<>();
    private List<MediaItem> movieProposals = new ArrayList<>();
    private Map<Integer, List<String>> votesMap = new HashMap<>();

    private MaterialButton btnDeleteGroup;
    private MaterialButton btnLeaveGroup;
    private View layoutManagement;

    // Zmienne dla akcelerometru
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F; // Siła potrząśnięcia
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long mShakeTimestamp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString("GROUP_ID");
        }
        db = FirebaseFirestore.getInstance();

        // Inicjalizacja sensora
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

        MaterialButton btnAddMember = view.findViewById(R.id.btnDetailsAddMember);
        MaterialButton btnBluetooth = view.findViewById(R.id.btnDetailsBluetooth);
        MaterialButton btnAddMovies = view.findViewById(R.id.btnDetailsAddMovies);

        rvMembers = view.findViewById(R.id.rvGroupMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        membersAdapter = new FriendsAdapter(membersList, (friend, position) -> removeMemberFromGroup(friend));
        rvMembers.setAdapter(membersAdapter);

        rvMovies = view.findViewById(R.id.rvGroupMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        moviesAdapter = new GroupMoviesAdapter(
                item -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("MEDIA_ITEM", item);
                    Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
                },
                (item, isVoted) -> toggleVote(item, isVoted),
                this::showRemoveMovieDialog
        );
        rvMovies.setAdapter(moviesAdapter);

        setupClickListeners(btnAddMember, btnBluetooth, btnAddMovies, btnDeleteGroup, btnLeaveGroup);
        listenToGroupChanges();
        listenToMovieProposals();

        return view;
    }

    private void wylosujFilmZGrupy() {
        if (movieProposals == null || movieProposals.isEmpty()) {
            Toast.makeText(getContext(), "Brak filmów do wylosowania!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Losowanie
        Random random = new Random();
        int index = random.nextInt(movieProposals.size());
        MediaItem selected = movieProposals.get(index);

        Toast.makeText(getContext(), "Wylosowano: " + selected.getTitle(), Toast.LENGTH_LONG).show();

        // Przejście do szczegółów wylosowanego filmu
        Bundle bundle = new Bundle();
        bundle.putSerializable("MEDIA_ITEM", selected);
        Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
    }

    // --- Metody Sensora ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce będzie bliskie 1, gdy telefon leży spokojnie
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // Ignoruj wstrząsy zbyt blisko siebie
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }
                mShakeTimestamp = now;
                wylosujFilmZGrupy();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Niepotrzebne w tym przypadku
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        super.onPause();
    }
    // -----------------------

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
        db.collection("groups").document(groupId)
                .collection("movies").document(String.valueOf(movie.getId()))
                .delete();
    }

    private void toggleVote(MediaItem movie, boolean currentlyVoted) {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null || groupId == null) return;

        // --- 1. OPTYMISTYCZNA AKTUALIZACJA UI (Natychmiastowa reakcja) ---
        // Pobieramy aktualną listę głosujących dla tego filmu
        List<String> voters = votesMap.get(movie.getId());
        if (voters == null) {
            voters = new ArrayList<>();
        } else {
            // Tworzymy nową instancję listy, by adapter wykrył zmianę
            voters = new ArrayList<>(voters);
        }

        if (currentlyVoted) {
            voters.remove(myUid); // Cofamy głos lokalnie
        } else {
            if (!voters.contains(myUid)) {
                voters.add(myUid); // Dodajemy głos lokalnie
            }
        }

        // Nadpisujemy mapę nowymi danymi
        votesMap.put(movie.getId(), voters);

        // Natychmiast odświeżamy adapter (tworzymy nową mapę, żeby DiffUtil zauważył różnicę)
        moviesAdapter.submitList(new ArrayList<>(movieProposals), new HashMap<>(votesMap));

        // --- 2. WYSYŁKA DO FIREBASE W TLE ---
        db.collection("groups").document(groupId)
                .collection("movies").document(String.valueOf(movie.getId()))
                .update("votedBy", currentlyVoted ?
                        FieldValue.arrayRemove(myUid) :
                        FieldValue.arrayUnion(myUid))
                .addOnFailureListener(e -> {
                    // Jeśli coś pójdzie nie tak (np. brak internetu), Firebase SnapshotListener
                    // i tak za chwilę nadpisze nasz "optymistyczny" widok poprawnym stanem z serwera.
                });
    }

    private void listenToMovieProposals() {
        if (groupId == null) return;

        moviesListener = db.collection("groups").document(groupId)
                .collection("movies")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    movieProposals.clear();
                    votesMap.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        MediaItem item = new MediaItem();
                        if (doc.contains("tmdbId")) {
                            item.setId(doc.getLong("tmdbId").intValue());
                        }
                        item.setTitle(doc.getString("title"));
                        item.setPosterPath(doc.getString("posterPath"));
                        item.setMediaType(doc.getString("mediaType"));
                        if (doc.contains("voteAverage")) {
                            item.setVoteAverage(doc.getDouble("voteAverage"));
                        }

                        List<String> voters = (List<String>) doc.get("votedBy");
                        if (voters == null) voters = new ArrayList<>();

                        movieProposals.add(item);
                        votesMap.put(item.getId(), voters);
                    }
                    moviesAdapter.submitList(new ArrayList<>(movieProposals), votesMap);
                });
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
                        tvMemberCount.setText(memberUids.size() + " członków");
                        String myUid = FirebaseAuth.getInstance().getUid();
                        if (myUid != null && myUid.equals(group.getOwnerId())) {
                            btnDeleteGroup.setVisibility(View.VISIBLE);
                            btnLeaveGroup.setVisibility(View.GONE);
                            layoutManagement.setVisibility(View.VISIBLE);
                        } else {
                            btnDeleteGroup.setVisibility(View.GONE);
                            btnLeaveGroup.setVisibility(View.VISIBLE);
                            layoutManagement.setVisibility(View.GONE);
                        }
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
                    Friend member = new Friend(doc.getId(), doc.getString("username"), doc.getString("avatar_url"), Boolean.TRUE.equals(doc.getBoolean("isOnline")));
                    membersList.add(member);
                    membersAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setupClickListeners(View add, View bt, View movies, View delete, View leave) {
        add.setOnClickListener(v -> showInviteFriendDialog());
        bt.setOnClickListener(v -> Toast.makeText(getContext(), "Potrząśnij telefonem, aby wylosować film!", Toast.LENGTH_SHORT).show());
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
        String myUid = FirebaseAuth.getInstance().getUid();
        db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(myUid)).addOnSuccessListener(aVoid -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupListener != null) groupListener.remove();
        if (moviesListener != null) moviesListener.remove();
    }
}