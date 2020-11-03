package com.example.chatapp.View.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.Adapter.ChatsAdapter;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.example.chatapp.ViewModel.MainViewModel;

import java.util.List;

public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";
    private RecyclerView recyclerView;
    private ChatsAdapter chatsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.chats_recycle_view);
        setAdapter();

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getChatsUsers().observe(getActivity(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                chatsAdapter.submitList(users);
                Log.d(TAG, "viewModelChatsUsersList OnDataChange: " + users);
            }
        });
        return view;
    }

    private void setAdapter() {
        chatsAdapter = new ChatsAdapter(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatsAdapter);
    }

}