package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Group;
import com.example.cinematuz.data.models.SearchResultUser;
import com.example.cinematuz.ui.fragments.friends.znajomi.BluetoothDeviceAdapter;
import com.example.cinematuz.utils.DialogHelper;
import com.example.cinematuz.utils.NearbyHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

public class GroupDetailsFragment extends Fragment {

    private String groupId;
    private FirebaseFirestore db;
    private ListenerRegistration groupListener;
    private ListenerRegistration moviesListener;

    private TextView tvGroupName, tvMemberCount;
    private RecyclerView rvMembers, rvMovies;
    private GroupMemberCircleAdapter membersAdapter;
    private AdvancedGroupMoviesAdapter moviesAdapter;

    public static String lastSeenWinnerId = null; // Zmienione na publiczne statyczne
    private List<Friend> membersList = new ArrayList<>();
    private List<MediaItem> movieProposals = new ArrayList<>();
    private Map<Integer, List<String>> votesMap = new HashMap<>();
    private int totalGroupMembers = 1;
    private int memberProfilesRequestToken = 0;
    private boolean hasProcessedInitialGroupSnapshot = false;
    private boolean currentUserIsAdmin = false;

    private View btnGroupMenu;
    private View layoutManagement;
    private ExtendedFloatingActionButton fabFinishVoting;

    private NearbyHelper nearbyHelper;
    private List<SearchResultUser> nearbyUsers = new ArrayList<>();
    private BluetoothDeviceAdapter btAdapter;
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) groupId = getArguments().getString("GROUP_ID");
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_details, container, false);

        tvGroupName = view.findViewById(R.id.tvDetailsGroupName);
        tvMemberCount = view.findViewById(R.id.tvDetailsMemberCount);
        btnGroupMenu = view.findViewById(R.id.btnGroupMenu);
        layoutManagement = view.findViewById(R.id.layoutManagement);
        fabFinishVoting = view.findViewById(R.id.fabFinishVoting);
        fabFinishVoting.setOnClickListener(v -> showVotingDecisionBottomSheet());

        View btnAddMember = view.findViewById(R.id.btnDetailsAddMember);
        View btnBluetooth = view.findViewById(R.id.btnDetailsBluetooth);
        View btnAddMovies = view.findViewById(R.id.btnDetailsAddMovies);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
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

        setupClickListeners(btnAddMember, btnBluetooth, btnAddMovies, btnGroupMenu);
        listenToGroupChanges();
        listenToMovieProposals();

        return view;
    }

    private void showVotingDecisionBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogStyle);
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_voting_decision, null);
        dialog.setContentView(sheetView);

        // Wyłuskujemy tylko filmy, które mają przynajmniej 1 głos
        ArrayList<MediaItem> eligibleMovies = new ArrayList<>();
        for (MediaItem m : movieProposals) {
            if (votesMap.containsKey(m.getId()) && !votesMap.get(m.getId()).isEmpty()) {
                eligibleMovies.add(m);
            }
        }

        if (eligibleMovies.isEmpty()) {
            Toast.makeText(getContext(), "Nikt jeszcze nie oddał głosu na żadną propozycję!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- OPCJA: Głos ludu ---
        sheetView.findViewById(R.id.btnVotePopular).setOnClickListener(v -> {
            dialog.dismiss();
            int maxVotes = 0;
            List<MediaItem> topMovies = new ArrayList<>();
            for (MediaItem m : eligibleMovies) {
                int vCount = votesMap.get(m.getId()).size();
                if (vCount > maxVotes) {
                    maxVotes = vCount;
                    topMovies.clear();
                    topMovies.add(m);
                } else if (vCount == maxVotes) {
                    topMovies.add(m);
                }
            }
            // W razie remisu losujemy z tych o max liczbie głosów
            MediaItem winner = topMovies.get(new Random().nextInt(topMovies.size()));

            updateGroupWinner(winner, "Głos ludu (" + maxVotes + " głosów)");
        });

        // --- OPCJA: Decyzja losu ---
        sheetView.findViewById(R.id.btnVoteRandom).setOnClickListener(v -> {
            dialog.dismiss();
            Bundle b = new Bundle();
            b.putSerializable("ELIGIBLE_MOVIES", eligibleMovies);
            b.putString("GROUP_ID", groupId); // Przekazujemy ID grupy, żeby ShakeFragment wiedział gdzie zapisać wynik
            Navigation.findNavController(requireView()).navigate(R.id.shakeFragment, b);
        });

        dialog.show();
    }

    private void updateGroupWinner(MediaItem winner, String reason) {
        if (groupId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("winnerId", String.valueOf(winner.getId()));
        updates.put("winnerReason", reason);

        db.collection("groups").document(groupId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Zamiast tylko Toast, wywołaj nawigację bezpośrednio:
                    Bundle b = new Bundle();
                    b.putString("GROUP_ID", groupId);
                    b.putString("WINNER_ID", String.valueOf(winner.getId()));
                    b.putString("WINNER_REASON", reason);
                    b.putBoolean("IS_ADMIN", true);
                    b.putSerializable("WINNER_MOVIE", winner); // Przekazujemy od razu obiekt!

                    Navigation.findNavController(requireView()).navigate(R.id.winnerFragment, b);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Błąd: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void showRemoveMovieDialog(MediaItem movie) {
        DialogHelper.showConfirmDialog(
                requireContext(),
                "Usuń propozycję",
                "Czy chcesz usunąć film \"" + movie.getTitle() + "\" z listy głosowania?",
                "Usuń",
                "Anuluj",
                () -> removeMovieFromGroup(movie)
        );
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

                        List<String> voters = (List<String>) doc.get("votedBy");
                        if (voters == null) voters = new ArrayList<>();

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
                    if (e != null) {
                        android.util.Log.e("GroupDetailsFragment", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Group group = snapshot.toObject(Group.class);
                        if (group != null) {
                            tvGroupName.setText(group.getName());
                            membersAdapter.setOwnerId(group.getOwnerId());

                            // Aktualizacja liczby członków dla obliczeń paska postępu w adapterze
                            if (group.getMembers() != null && !group.getMembers().isEmpty()) {
                                totalGroupMembers = group.getMembers().size();
                                fetchMemberProfiles(group.getMembers());
                            } else {
                                totalGroupMembers = 1;
                                membersList.clear();
                                membersAdapter.notifyDataSetChanged();
                                sortAndSubmitMovies();
                            }

                            // Widoczność przycisku zakończenia tylko dla właściciela grupy
                            String myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    : null;
                            boolean isAdmin = myUid != null && myUid.equals(group.getOwnerId());
                            currentUserIsAdmin = isAdmin;
                            if (isAdmin) {
                                fabFinishVoting.setVisibility(View.VISIBLE);
                            } else {
                                fabFinishVoting.setVisibility(View.GONE);
                            }

                            // KROK KRYTYCZNY: Reakcja na pojawienie się zwycięzcy w Firebase
                            String currentWinnerId = group.getWinnerId();
                            boolean isInitialSnapshot = !hasProcessedInitialGroupSnapshot;
                            hasProcessedInitialGroupSnapshot = true;

                            if (currentWinnerId != null && !currentWinnerId.isEmpty()) {
                                if (isInitialSnapshot) {
                                    lastSeenWinnerId = currentWinnerId;
                                } else {
                                    navigateToWinnerIfFound(currentWinnerId, group.getWinnerReason(), isAdmin);
                                }
                            } else {
                                lastSeenWinnerId = null; // Resetujemy, aby umożliwić ponowną nawigację po nowym losowaniu
                            }
                        }
                    }
                });
    }

    private void showGroupPopupMenu(View view, boolean isAdmin) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), view);
        if (isAdmin) {
            popup.getMenu().add("Usuń grupę");
        } else {
            popup.getMenu().add("Opuść grupę");
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Usuń grupę")) {
                showDeleteGroupDialog();
            } else if (item.getTitle().equals("Opuść grupę")) {
                showLeaveGroupDialog();
            }
            return true;
        });
        popup.show();
    }

    private void showDeleteGroupDialog() {
        DialogHelper.showConfirmDialog(
                requireContext(),
                "Usuń grupę",
                "Czy na pewno chcesz bezpowrotnie usunąć tę grupę? Wszyscy członkowie zostaną usunięci, a propozycje filmowe przepadną.",
                "Usuń grupę",
                "Anuluj",
                this::deleteGroup
        );
    }

    private void showLeaveGroupDialog() {
        DialogHelper.showConfirmDialog(
                requireContext(),
                "Opuść grupę",
                "Czy na pewno chcesz opuścić tę grupę? Nie będziesz już mógł brać udziału w głosowaniu.",
                "Opuść",
                "Anuluj",
                this::leaveGroup
        );
    }

    private void navigateToWinnerIfFound(String winnerId, String winnerReason, boolean isAdmin) {
        if (winnerId == null || winnerId.trim().isEmpty() || winnerId.equals(lastSeenWinnerId)) {
            return;
        }

        lastSeenWinnerId = winnerId;

        if (isAdded() && getView() != null) {
            NavController navController = Navigation.findNavController(getView());

            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.winnerFragment) {
                return;
            }

            Bundle b = new Bundle();
            b.putString("GROUP_ID", groupId);
            b.putString("WINNER_ID", winnerId);
            b.putString("WINNER_REASON", winnerReason != null ? winnerReason : "Głosowanie grupowe");
            b.putBoolean("IS_ADMIN", isAdmin);
            b.putString("media_type", "movie"); // Dodaj to, aby WinnerFragment wiedział co ładować

            // Znajdź pełny obiekt MediaItem, jeśli jest dostępny
            MediaItem winnerMovie = null;
            if (movieProposals != null) {
                for (MediaItem m : movieProposals) {
                    if (String.valueOf(m.getId()).equals(winnerId)) {
                        winnerMovie = m;
                        break;
                    }
                }
            }

            if (winnerMovie != null) {
                b.putSerializable("WINNER_MOVIE", winnerMovie);
            }

            try {
                // JEDNO wywołanie nawigacji z kompletnym Bundle
                navController.navigate(R.id.winnerFragment, b);
            } catch (Exception e) {
                android.util.Log.e("GroupDetailsFragment", "Nawigacja nieudana: " + e.getMessage());
                lastSeenWinnerId = null;
            }
        }
    }


    private void fetchMemberProfiles(List<String> uids) {
        final int requestToken = ++memberProfilesRequestToken;
        membersList.clear();
        membersAdapter.notifyDataSetChanged();

        if (uids == null || uids.isEmpty()) {
            sortAndSubmitMovies();
            return;
        }

        final int expectedResponses = uids.size();
        final int[] receivedResponses = {0};

        for (String uid : uids) {
            db.collection("profiles").document(uid).get().addOnSuccessListener(doc -> {
                if (!isAdded() || requestToken != memberProfilesRequestToken) {
                    return;
                }

                if (doc.exists()) {
                    boolean alreadyExists = false;
                    for (Friend friend : membersList) {
                        if (friend.getId().equals(doc.getId())) {
                            alreadyExists = true;
                            break;
                        }
                    }

                    if (!alreadyExists) {
                        membersList.add(new Friend(
                                doc.getId(),
                                doc.getString("username"),
                                doc.getString("avatar_url"),
                                Boolean.TRUE.equals(doc.getBoolean("isOnline"))
                        ));
                    }
                }

                receivedResponses[0]++;
                if (receivedResponses[0] >= expectedResponses) {
                    membersAdapter.notifyDataSetChanged();
                    sortAndSubmitMovies();
                }
            });
        }
    }

    private void setupClickListeners(View add, View bt, View movies, View menu) {
        add.setOnClickListener(v -> showInviteFriendDialog());
        bt.setOnClickListener(v -> checkPermissionsAndStartBluetooth());
        movies.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("TARGET_GROUP_ID", groupId);
            bundle.putBoolean("SELECTION_MODE", true);
            Navigation.findNavController(v).navigate(R.id.searchFragment, bundle);
        });

        if (menu != null) {
            menu.setOnClickListener(v -> showGroupPopupMenu(v, currentUserIsAdmin));
        }

        // Pływający przycisk dla admina - odpala BottomSheet
        fabFinishVoting.setOnClickListener(v -> showVotingDecisionBottomSheet());
    }

    private void leaveGroup() {
        if (groupId == null) return;
        db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(FirebaseAuth.getInstance().getUid())).addOnSuccessListener(aVoid -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void removeMemberFromGroup(Friend friend) {
        String myUid = FirebaseAuth.getInstance().getUid();
        if (groupId == null || myUid == null || friend.getId() == null) return;

        DialogHelper.showConfirmDialog(
                requireContext(),
                "Usuń użytkownika",
                getString(R.string.friends_remove_message, friend.getName()),
                "Usuń",
                "Anuluj",
                () -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    // 1. Usuń z grupy
                    batch.update(db.collection("groups").document(groupId),
                            "members", FieldValue.arrayRemove(friend.getId()));

                    // 2. Usuń z moich znajomych
                    batch.delete(db.collection("profiles").document(myUid)
                            .collection("friends").document(friend.getId()));

                    // 3. Usuń mnie ze znajomych u niego
                    batch.delete(db.collection("profiles").document(friend.getId())
                            .collection("friends").document(myUid));

                    batch.commit().addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Użytkownik usunięty z grupy i znajomych", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Błąd: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );
    }

    private void deleteGroup() {
        if (groupId == null) return;
        db.collection("groups").document(groupId).delete().addOnSuccessListener(aVoid -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void showInviteFriendDialog() {
        String myUid = FirebaseAuth.getInstance().getUid();
        db.collection("profiles").document(myUid).collection("friends")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Friend> friends = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snaps) {
                        Friend f = new Friend(doc.getId(), doc.getString("name"), doc.getString("avatarUrl"), true);
                        friends.add(f);
                        names.add(f.getName());
                    }

                    if (friends.isEmpty()) {
                        Toast.makeText(getContext(), "Nie masz jeszcze znajomych do zaproszenia", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DialogHelper.showItemsDialog(
                            requireContext(),
                            "Zaproś do grupy",
                            names.toArray(new String[0]),
                            (dialog, which) -> {
                                sendGroupInvite(friends.get(which).getId());
                                Toast.makeText(getContext(), "Wysłano zaproszenie do " + names.get(which), Toast.LENGTH_SHORT).show();
                            }
                    );
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
        if (nearbyHelper != null) nearbyHelper.stopSearching();
    }

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
        dialog.setOnDismissListener(d -> {
            if (nearbyHelper != null) nearbyHelper.stopSearching();
        });
        dialog.show();
    }

    private class GroupMemberCircleAdapter extends RecyclerView.Adapter<GroupMemberCircleAdapter.ViewHolder> {
        private List<Friend> members;
        private String ownerId = "";
        private boolean isEditMode = false;

        public GroupMemberCircleAdapter(List<Friend> members) {
            this.members = members;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
            notifyDataSetChanged();
        }

        public void setEditMode(boolean editMode) {
            this.isEditMode = editMode;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member_circle, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Friend friend = members.get(position);
            holder.tvName.setText(friend.getName());
            holder.ivStar.setVisibility((friend.getId() != null && friend.getId().equals(ownerId)) ? View.VISIBLE : View.GONE);

            if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(friend.getAvatarUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person);
            }

            // Obsługa trybu usuwania (wiggle + X)
            String myUid = FirebaseAuth.getInstance().getUid();
            boolean isMe = friend.getId() != null && friend.getId().equals(myUid);
            boolean isAdmin = myUid != null && myUid.equals(ownerId);

            if (isEditMode && isAdmin && !isMe) {
                holder.ivRemove.setVisibility(View.VISIBLE);
                android.view.animation.Animation shake = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.shake_side_to_side);
                holder.flAvatarContainer.startAnimation(shake);
            } else {
                holder.ivRemove.setVisibility(View.GONE);
                holder.flAvatarContainer.clearAnimation();
            }

            holder.ivRemove.setOnClickListener(v -> {
                removeMemberFromGroup(friend);
            });

            holder.itemView.setOnClickListener(v -> {
                if (isEditMode) {
                    setEditMode(false);
                } else if (isAdmin && !isMe) {
                    removeMemberFromGroup(friend);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (isAdmin) {
                    setEditMode(true);
                    return true;
                }
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivStar, ivAvatar, ivRemove;
            TextView tvName;
            View flAvatarContainer;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivStar = itemView.findViewById(R.id.ivOwnerStar);
                ivAvatar = itemView.findViewById(R.id.ivFriendAvatar);
                tvName = itemView.findViewById(R.id.tvFriendName);
                ivRemove = itemView.findViewById(R.id.ivRemoveMember);
                flAvatarContainer = itemView.findViewById(R.id.flAvatarContainer);
            }
        }
    }

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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_movie, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                holder.tvGenre.setText("• " + movie.getGenres().get(0).getName());
            } else {
                holder.tvGenre.setText("");
            }

            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load("https://image.tmdb.org/t/p/w200" + movie.getPosterPath())
                        .into(holder.ivPoster);
            }

            holder.tvVoteCount.setText(voters.size() + " głosów");
            holder.progressBar.setMax(maxMembers);
            holder.progressBar.setProgress(voters.size(), true);

            holder.btnVote.setImageResource(iVoted ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline);
            holder.btnVote.setColorFilter(iVoted ? 0xFFFF0000 : 0xFF757575);
            holder.btnVote.setOnClickListener(v -> toggleVote(movie, iVoted));

            holder.btnDelete.setOnClickListener(v -> GroupDetailsFragment.this.showRemoveMovieDialog(movie));

            // Wyróżnienie oddanego głosu czerwoną ramką
            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) holder.itemView;
            if (iVoted) {
                card.setStrokeColor(android.graphics.Color.RED);
                card.setStrokeWidth(4);
            } else {
                card.setStrokeWidth(0);
            }

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
                        avatarUrl = f.getAvatarUrl();
                        break;
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

        @Override
        public int getItemCount() {
            return movies.size();
        }

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