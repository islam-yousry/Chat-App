package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.example.chatapp.Util.Utils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private DatabaseReference friendsListener;


    private void init() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.invitations_recycle_view);
        FirebaseUser firebaseUser = Utils.getUser(this);
        assert firebaseUser != null;
        friendsListener = FirebaseDatabase.getInstance().getReference("users")
                .child(firebaseUser.getUid())
                .child("friends");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        init();
        setAdapter();
        setUpToolBarView();
        readRequests();

    }

    private void setAdapter() {
        adapter = new UserAdapter(RequestsActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
        recyclerView.setAdapter(adapter);
    }

    private void setUpToolBarView() {
        CircleImageView toolbarImageView = findViewById(R.id.toolbar_image_view);
        toolbarImageView.setVisibility(View.GONE);
        TextView titleTextView = findViewById(R.id.toolbar_title_text_view);
        titleTextView.setText(R.string.requests);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void readRequests() {
        final List<String> usersIdList = new ArrayList<>();

        friendsListener.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersIdList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    String userState = snapshot.getValue(String.class);
                    assert userState != null;
                    if (userState.equals(Utils.FriendState.PENDING.toString())) {
                        usersIdList.add(userId);
                    }
                }
                getUsers(usersIdList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUsers(List<String> usersIdList) {
        final List<User> usersList = new ArrayList<>();
        for (final String userId : usersIdList) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    usersList.add(user);
                    adapter.submitList(usersList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAuthenticationState(RequestsActivity.this);
    }
}