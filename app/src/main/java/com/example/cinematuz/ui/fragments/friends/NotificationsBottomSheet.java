package com.example.cinematuz.ui.fragments.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.FriendRequest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment typu BottomSheet wyświetlający listę powiadomień (zaproszeń do znajomych).
 * Pozwala na akceptowanie lub odrzucanie zaproszeń bezpośrednio z panelu dolnego.
 */
public class NotificationsBottomSheet extends BottomSheetDialogFragment {
    private RequestAdapter adapter;
    private List<FriendRequest> requests = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Inicjalizuje widok BottomSheet, konfiguruje RecyclerView oraz nasłuchuje zaproszeń w Firestore.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_notifications, container, false);
        RecyclerView rv = v.findViewById(R.id.rvNotifications);
        TextView tvEmpty = v.findViewById(R.id.tvEmptyNotifications);

        adapter = new RequestAdapter(requests, new RequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest req) { accept(req); }
            @Override
            public void onDecline(FriendRequest req) { decline(req); }
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        db.collection("profiles").document(mAuth.getUid()).collection("friend_requests")
                .addSnapshotListener((value, e) -> {
                    if (value != null) {
                        requests.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            requests.add(new FriendRequest(doc.getId(), doc.getString("username"), doc.getString("avatarUrl"), "friend"));
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
        return v;
    }

    /**
     * Akceptuje zaproszenie do znajomych. Tworzy relację obustronną w Firestore.
     * 
     * @param req Obiekt zaproszenia do zaakceptowania.
     */
    private void accept(FriendRequest req) {
        String myUid = mAuth.getUid();
        db.collection("profiles").document(myUid).get().addOnSuccessListener(doc -> {
            WriteBatch batch = db.batch();
            batch.set(db.collection("profiles").document(myUid).collection("friends").document(req.getUid()),
                    new Friend(req.getUid(), req.getUsername(), req.getAvatarUrl(), true));
            batch.delete(db.collection("profiles").document(myUid).collection("friend_requests").document(req.getUid()));
            batch.commit().addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Dodano znajomego!", Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Odrzuca zaproszenie do znajomych, usuwając je z bazy danych.
     * 
     * @param req Obiekt zaproszenia do odrzucenia.
     */
    private void decline(FriendRequest req) {
        db.collection("profiles").document(mAuth.getUid()).collection("friend_requests").document(req.getUid()).delete();
    }

    /**
     * Zwraca styl motywu dla BottomSheet.
     * 
     * @return Identyfikator zasobu stylu.
     */
    @Override
    public int getTheme() { return R.style.TransparentBottomSheetDialog; }
}