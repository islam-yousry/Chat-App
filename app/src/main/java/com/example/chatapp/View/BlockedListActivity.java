package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedListActivity extends AppCompatActivity {
    private static final String TAG = "BlockedListActivity";

    private RecyclerView recyclerView;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_list);

        recyclerView = findViewById(R.id.blocked_list_recycle_view);
        setAdapter();
        setActionBar();
        readUsers();
    }

    private void setAdapter() {
        adapter = new UserAdapter(BlockedListActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(BlockedListActivity.this));
        recyclerView.setAdapter(adapter);
    }


    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        CircleImageView toolbarImageView = findViewById(R.id.toolbar_image_view);
        toolbarImageView.setVisibility(View.GONE);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text_view);
        toolbarTitle.setText(R.string.blocked_list);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void readUsers() {
        FirebaseUser firebaseUser = Utils.getUser(BlockedListActivity.this);
        final DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");
        usersReference
                .child(firebaseUser.getUid()).child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<User> userList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String friendId = snapshot.getKey();
                            String friendState = snapshot.getValue(String.class);
                            if (friendState.equals("block")) {
                                usersReference.child(friendId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                User user = userSnapshot.getValue(User.class);
                                                userList.add(user);
                                                adapter.submitList(userList);
                                                Log.d(TAG,"user has block: "+ user);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAuthenticationState(BlockedListActivity.this);
    }

}