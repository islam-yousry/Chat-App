package com.example.chatapp.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatapp.Model.User;
import com.example.chatapp.Util.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<List<User>> friends = new MutableLiveData<>();
    private final MutableLiveData<List<User>> chatsUsers = new MutableLiveData<>();
    private final List<String> friendsListIds = new ArrayList<>();
    private final List<String> friendsChatListIds = new ArrayList<>();

    public MainViewModel() {
        readFriends();
    }

    private void readFriends() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference friendsDatabaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(firebaseUser.getUid())
                .child("friends");

        friendsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsListIds.clear();
                friendsChatListIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String friendId = snapshot.getKey();
                    String friendState = snapshot.getValue(String.class);
                    assert friendState != null;
                    if (friendState.equals(Utils.FriendState.WITHOUT_CHAT.toString()) || friendState.equals(Utils.FriendState.WITH_CHAT.toString()))
                        friendsListIds.add(friendId);
                    if (friendState.equals(Utils.FriendState.WITH_CHAT.toString())) friendsChatListIds.add(friendId);
                }

                updateFriendsChatList(friendsChatListIds);
                updateFriendsList(friendsListIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void updateFriendsList(List<String> friendsListIds) {
        final List<User> friendsList = new ArrayList<>();
        for (String userId : friendsListIds) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        User user = snapshot.getValue(User.class);
                        friendsList.add(user);
                        friends.setValue(friendsList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateFriendsChatList(List<String> friendsChatListIds) {
        final List<User> friendsChatList = new ArrayList<>();
        for (final String userId : friendsChatListIds) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        User user = snapshot.getValue(User.class);
                        friendsChatList.add(user);
                        chatsUsers.setValue(friendsChatList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        // in case the chat list is empty.
        chatsUsers.setValue(friendsChatList);
    }


    public MutableLiveData<List<User>> getFriends() {
        return friends;
    }

    public MutableLiveData<List<User>> getChatsUsers() {
        return chatsUsers;
    }
}
