package com.example.chatapp.View.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.example.chatapp.View.RequestsActivity;
import com.example.chatapp.View.SearchActivity;
import com.example.chatapp.ViewModel.MainViewModel;

import java.util.List;

public class PeopleFragment extends Fragment {
    private static final String TAG = "PeopleFragment";
    private RecyclerView userRecyclerView;
    private UserAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        userRecyclerView = view.findViewById(R.id.user_recycle_view);
        setAdapter();
        setHasOptionsMenu(true);

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getFriends().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                adapter.submitList(users);
                Log.d(TAG, "viewModelFriendsList onDataChange: " + users);
            }
        });


        CardView requestsCardView = view.findViewById(R.id.requests_card_view);
        requestsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), RequestsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    private void setAdapter() {
        adapter = new UserAdapter(getContext());
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.people_fragment_menu, menu);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_person_menu_item) {
            goToSearchActivity();
            return true;
        }
        return false;
    }

    private void goToSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivity(intent);
    }

}