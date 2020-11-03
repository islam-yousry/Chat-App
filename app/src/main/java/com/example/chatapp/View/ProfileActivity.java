package com.example.chatapp.View;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
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
import com.onesignal.OneSignal;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {


    private static final String TAG = "ProfileActivity";
    private TextView profileName, profileEmail, profilePhone;
    private CircleImageView profileImageView;
    private FirebaseUser firebaseUser;
    private static Boolean SELF_PROFILE_VIEW;
    private DatabaseReference usersReference;

    // self user profile mode.
    private LinearLayout selfUserLinearLayout, settingsLinearLayout, shareAppLinearLayout, blockedListLinearLayout, logoutLinearLayout;


    // other user profile mode.
    private RelativeLayout relativeLayout;
    private ImageButton rightImageButton, blockImageButton;
    private TextView rightTextView;
    private String userNotificationKey;
    private LinearLayout blockLinearLayout, rightLinearLayout;


    private void init() {
        profileName = findViewById(R.id.profile_username);
        profileEmail = findViewById(R.id.profile_user_email_address);
        profilePhone = findViewById(R.id.profile_phone_text_view);
        profileImageView = findViewById(R.id.profile_image_view);
        selfUserLinearLayout = findViewById(R.id.self_user_linear_layout);
        settingsLinearLayout = findViewById(R.id.settings_linear_layout);
        shareAppLinearLayout = findViewById(R.id.share_app_linear_layout);
        blockedListLinearLayout = findViewById(R.id.blocked_list_linear_layout);
        logoutLinearLayout = findViewById(R.id.logout_linear_layout);

        firebaseUser = Utils.getUser(this);
        usersReference = FirebaseDatabase.getInstance().getReference("users");

        relativeLayout = findViewById(R.id.other_user_relative_layout);
        rightImageButton = findViewById(R.id.right_image_button);
        rightTextView = findViewById(R.id.right_text_view);
        rightLinearLayout = findViewById(R.id.right_linear_layout);
        blockImageButton = findViewById(R.id.block_image_button);
        blockLinearLayout = findViewById(R.id.block_linear_layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

        final String userId = getIntent().getStringExtra("userId");
        if (userId.equals(firebaseUser.getUid())) {
            setSelfProfileView();
        } else {
            setOtherUserProfileView(userId);
        }
        setActionBar();


        // get profile data.
        usersReference
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final User user = snapshot.getValue(User.class);
                        assert user != null;
                        profileName.setText(user.getUser_name());
                        profileEmail.setText(user.getUser_email());
                        userNotificationKey = user.getNotification_key();
                        Glide.with(getApplicationContext())
                                .load(user.getProfile_image())
                                .placeholder(R.drawable.ic_person)
                                .into(profileImageView);
                        if(user.getPhone().isEmpty()){
                            profilePhone.setVisibility(View.GONE);
                        }
                        else {
                            profilePhone.setVisibility(View.VISIBLE);
                            profilePhone.setText(user.getPhone());
                        }

                        profileImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Utils.ImageUtils.showPhoto(ProfileActivity.this, Uri.parse(user.getProfile_image()));
                            }
                        });
                        Log.d(TAG, "OnDataChange: " + snapshot.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }


    private void setSelfProfileView() {
        SELF_PROFILE_VIEW = true;

        selfUserLinearLayout.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.GONE);

        // set share app click listener.
        shareAppLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.shareApplication(ProfileActivity.this);
            }
        });


        // set settings click listener.
        settingsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
        });

        // set blocked list click listener.
        blockedListLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,BlockedListActivity.class);
                startActivity(intent);
            }
        });

        // set logout click listener.
        logoutLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneSignal.setSubscription(false);
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }


    private void setOtherUserProfileView(final String userId) {
        SELF_PROFILE_VIEW = false;

        relativeLayout.setVisibility(View.VISIBLE);
        selfUserLinearLayout.setVisibility(View.GONE);
        // check if the other user is a friend or not.
        usersReference
                .child(firebaseUser.getUid())
                .child("friends").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null)
                            setRightButtonView("add", R.drawable.ic_add_person);
                        else if (snapshot.getValue().toString().equals(Utils.FriendState.PENDING.toString()))
                            setRightButtonView("accept", R.drawable.ic_accept);
                        else if (snapshot.getValue().toString().equals(Utils.FriendState.SENT.toString()))
                            setRightButtonView("cancel", R.drawable.ic_remove_person);
                        else if (snapshot.getValue().toString().equals(Utils.FriendState.WITHOUT_CHAT.toString())
                                || snapshot.getValue().toString().equals(Utils.FriendState.WITH_CHAT.toString()))
                            setRightButtonView("remove", R.drawable.ic_remove_person);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // set right button click listener.
        rightLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rightTextView.getText().toString()) {
                    case "add":
                        setRightButtonView("cancel", R.drawable.ic_remove_person);
                        setAddAction(userId);
                        break;
                    case "accept":
                        setRightButtonView("remove", R.drawable.ic_remove_person);
                        setAcceptAction(userId);
                        break;
                    default: // the case of remove friend or cancel request.
                        setRightButtonView("add", R.drawable.ic_add_person);
                        setRemoveAction(userId);
                }
            }
        });


        // set block button click listener.
        blockLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersReference.child(firebaseUser.getUid())
                        .child("friends")
                        .child(userId).setValue(Utils.FriendState.BLOCK);
                usersReference.child(userId)
                        .child("friends")
                        .child(firebaseUser.getUid())
                        .setValue(Utils.FriendState.BLOCKED);
                finish();
            }
        });
    }


    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        CircleImageView toolbarImageView = findViewById(R.id.toolbar_image_view);
        toolbarImageView.setVisibility(View.GONE);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text_view);
        if (SELF_PROFILE_VIEW) toolbarTitle.setText(R.string.selfProfile_activity_title);
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

    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAuthenticationState(ProfileActivity.this);
    }


    private void setRightButtonView(String rightText, int rightImage) {
        rightImageButton.setBackgroundResource(rightImage);
        rightTextView.setText(rightText);
    }

    private void setAddAction(String userId) {
        Utils.sendNotification("Chat App: Friend Request",profileName.getText().toString(),userNotificationKey,"");
        usersReference.child(firebaseUser.getUid()).child("friends").child(userId).setValue(Utils.FriendState.SENT);
        usersReference.child(userId).child("friends").child(firebaseUser.getUid()).setValue(Utils.FriendState.PENDING);
    }

    private void setAcceptAction(final String userId) {
        FirebaseDatabase.getInstance().getReference("chats")
                .child(Utils.getChatId(userId,firebaseUser.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersReference.child(firebaseUser.getUid()).child("friends").child(userId)
                                .setValue(snapshot.getValue()==null?Utils.FriendState.WITHOUT_CHAT.toString()
                                        :Utils.FriendState.WITH_CHAT.toString());
                        usersReference.child(userId).child("friends").child(firebaseUser.getUid())
                                .setValue(snapshot.getValue()==null?Utils.FriendState.WITHOUT_CHAT.toString()
                                        :Utils.FriendState.WITH_CHAT.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setRemoveAction(String userId) {
        usersReference.child(firebaseUser.getUid()).child("friends").child(userId).removeValue();
        usersReference.child(userId).child("friends").child(firebaseUser.getUid()).removeValue();
    }

}