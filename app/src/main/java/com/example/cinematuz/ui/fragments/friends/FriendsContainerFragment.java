package com.example.cinematuz.ui.fragments.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cinematuz.R;
import com.example.cinematuz.ui.fragments.friends.znajomi.FriendsListFragment;
import com.example.cinematuz.ui.fragments.friends.grupy.GroupFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class FriendsContainerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_container, container, false);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupFriends);

        // Domyślnie ładujemy listę znajomych przy pierwszym uruchomieniu
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.child_fragment_container, new FriendsListFragment())
                    .commit();
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                Fragment selectedFragment;
                if (checkedId == R.id.btnTabFriends) {
                    selectedFragment = new FriendsListFragment();
                } else {
                    selectedFragment = new GroupFragment();
                }

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.child_fragment_container, selectedFragment)
                        .commit();
            }
        });

        return view;
    }
}