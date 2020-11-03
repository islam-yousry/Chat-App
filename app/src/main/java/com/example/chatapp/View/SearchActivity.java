package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.chatapp.Adapter.UserAdapter;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.example.chatapp.Util.Utils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> usersList;

    private static final String TAG = "SearchActivity";

    private void init() {
        searchEditText = findViewById(R.id.search_edit_text);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycle_view);
        usersList = new ArrayList<>();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();
        setAdapter();
        setActionBar();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usersList.clear();
                String string = s.toString().trim();
                if (!string.isEmpty()) {
                    Log.d(TAG, "searchEditText: onTextChanged " + s.toString());
                    searchUsers(s.toString());
                } else adapter.submitList(usersList);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void setActionBar() {
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

    private void setAdapter() {
        adapter = new UserAdapter(SearchActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }


    private void searchUsers(String s) {
        final Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("search_name").startAt(s).endAt(s + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                FirebaseUser firebaseUser = Utils.getUser(SearchActivity.this);
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final User user = snapshot.getValue(User.class);
                    if (user != null) {
                        DataSnapshot reference = dataSnapshot.child("friends").child(firebaseUser.getUid());
                        if (!reference.exists() || (!reference.getValue().equals(Utils.FriendState.BLOCK.toString())
                                && !reference.getValue().equals(Utils.FriendState.BLOCKED.toString()))) {
                            usersList.add(user);
                            Log.d(TAG, "friend: change " + user);

                        }
                    }
                }

                adapter.submitList(usersList);
                Log.d(TAG, "searchUsers: onDataChange " + dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAuthenticationState(SearchActivity.this);
    }
}