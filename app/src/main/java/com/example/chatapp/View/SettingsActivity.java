package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.example.chatapp.Util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yalantis.ucrop.UCrop;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private TextInputEditText passwordEditText;
    private EditText usernameEditText, phoneEditText;
    private CircleImageView profileImageView;
    private TextView forgottenPasswordText;
    private Button saveChanges;
    private FirebaseUser firebaseUser;

    private boolean isProfileImageChanged;

    private static final String TAG = "SettingsActivity";

    private void init() {
        passwordEditText = findViewById(R.id.password_text_input_edit_text);
        usernameEditText = findViewById(R.id.username_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        profileImageView = findViewById(R.id.profile_image_view);
        forgottenPasswordText = findViewById(R.id.forgotten_password_text_view);
        saveChanges = findViewById(R.id.settings_save_changes_button);
        firebaseUser = Utils.getUser(this);
        isProfileImageChanged = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
        setActionBar();
        DisplayUserData();

        // set profile image click listener.
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.ImageUtils.pickAPhoto(SettingsActivity.this);
            }
        });

        // set forgotten password click listener.
        forgottenPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.resendPassword(SettingsActivity.this);
            }
        });

        // set save button click listener.
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valid()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(firebaseUser.getEmail(), passwordEditText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG,"profileData changed");
                                        Toast.makeText(SettingsActivity.this,"Profile data updated",Toast.LENGTH_SHORT).show();
                                        if (isProfileImageChanged) {
                                            compressImage(((BitmapDrawable) profileImageView.getDrawable()).getBitmap());
                                            Log.d(TAG, "profile Image Change: successful");
                                        }
                                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users")
                                                .child(firebaseUser.getUid());
                                        userReference.child("user_name").setValue(usernameEditText.getText().toString());
                                        userReference.child("search_name").setValue(usernameEditText.getText().toString().toLowerCase());
                                        userReference.child("phone").setValue(phoneEditText.getText().toString());
                                    } else {
                                        Toast.makeText(SettingsActivity.this,"you entered a wrong password. try again!",Toast.LENGTH_SHORT).show();
                                        Log.w(TAG,"profileData change: failure",task.getException().getCause());
                                    }
                                }
                            });
                }
            }
        });

    }

    private boolean valid() {
        boolean res = false;
        if (usernameEditText.getText().toString().isEmpty()) {
            usernameEditText.setError("this field must be not empty");
            return false;
        } else if (passwordEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Enter your password to save changes", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void DisplayUserData() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            Glide.with(getApplicationContext())
                                    .load(user.getProfile_image())
                                    .placeholder(R.drawable.ic_person)
                                    .into(profileImageView);

                            usernameEditText.setText(user.getUser_name());
                            phoneEditText.setText(user.getPhone());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        CircleImageView toolbarImageView = findViewById(R.id.toolbar_image_view);
        toolbarImageView.setVisibility(View.GONE);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text_view);
        toolbarTitle.setText(R.string.settings);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get the image selected for profile image.
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "OnActivityResult: RESULT_OK");
            if (requestCode == Utils.ImageUtils.RC_PHOTO_PICKER || requestCode == Utils.ImageUtils.REQUEST_IMAGE_CAPTURE) {
                isProfileImageChanged = true;
                Utils.ImageUtils.performCrop(Objects.requireNonNull(data).getData(), getCacheDir(), SettingsActivity.this);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                Log.d(TAG, "OnActivityResult: UCrop.REQUEST_CROP : " + UCrop.getOutput(Objects.requireNonNull(data)));
                profileImageView.setImageBitmap(Utils.ImageUtils.getBitmap(UCrop.getOutput(data), getContentResolver()));
            } else if (requestCode == UCrop.RESULT_ERROR) {
                Log.w(TAG, "OnActivityResult: UCrop.RESULT_ERROR", UCrop.getError(Objects.requireNonNull(data)));
            }
        }
    }


    private void compressImage(final Bitmap profileImage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final byte[] bytes = Utils.ImageUtils.compressImage(SettingsActivity.this, profileImage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.ImageUtils.uploadProfileImage(SettingsActivity.this, bytes);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.checkAuthenticationState(SettingsActivity.this);
    }
}