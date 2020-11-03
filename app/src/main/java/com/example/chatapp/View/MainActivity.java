package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Util.NotificationOpenedHandler;
import com.example.chatapp.Util.Utils;
import com.example.chatapp.View.Fragments.PeopleFragment;
import com.example.chatapp.R;
import com.example.chatapp.View.Fragments.ChatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageView profileImageView;
    private FirebaseUser firebaseUser;
    private BottomNavigationView bottomNavigationView;
    private Fragment chatsFragment, peopleFragment;
    private TextView titleTextView;


    private void init() {
        chatsFragment = new ChatsFragment();
        peopleFragment = new PeopleFragment();
        firebaseUser = Utils.getUser(this);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setUpToolBarView();
        setCurrentFragment(chatsFragment);
        setUpOneSignal();

        // set profile image to toolbar
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("profile_image");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profile_url = snapshot.getValue(String.class);
                Glide.with(getApplicationContext())
                        .load(profile_url)
                        .placeholder(R.drawable.ic_person)
                        .into(profileImageView);
                Log.d(TAG, "ProfileImage OnDataChange: " + profile_url);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        // set bottom navigation click listener.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chats_menu_item:
                        setCurrentFragment(chatsFragment);
                        return true;
                    case R.id.people_menu_item:
                        setCurrentFragment(peopleFragment);
                        return true;
                }
                return false;
            }
        });
    }

    private void setUpOneSignal() {
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new NotificationOpenedHandler(this))
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid())
                        .child("notification_key").setValue(userId);
                Log.d(TAG,"notification key change: "+userId);
            }
        });
    }

    private void setUpToolBarView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.toolbar_image_view);
        titleTextView = findViewById(R.id.toolbar_title_text_view);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("userId", firebaseUser.getUid());
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAuthenticationState(MainActivity.this);
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.frame_layout, fragment).
                commit();
        if (fragment instanceof ChatsFragment)
            titleTextView.setText(R.string.Chats);
        else if (fragment instanceof PeopleFragment)
            titleTextView.setText(R.string.People);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!getIntent().getBooleanExtra("keepMeSigned", false)) {
            FirebaseAuth.getInstance().signOut();
            Utils.checkAuthenticationState(MainActivity.this);
        }
    }
}
