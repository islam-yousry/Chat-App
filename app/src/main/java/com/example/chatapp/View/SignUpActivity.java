package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {


    private static final String TAG = "SignUpActivity";


    private CircleImageView profileCircleImageView;
    private EditText usernameEditText, mailEditText;
    private TextInputEditText passwordTextInputEditText, confirmPasswordTextInputEditText;
    private Button signUp;
    private ImageButton backImageButton;


    private void init() {
        backImageButton = findViewById(R.id.back_image_button);
        profileCircleImageView = findViewById(R.id.toolbar_image_view);
        usernameEditText = findViewById(R.id.username_edit_text);
        mailEditText = findViewById(R.id.email_edit_text);
        passwordTextInputEditText = findViewById(R.id.password_text_input_edit_text);
        confirmPasswordTextInputEditText = findViewById(R.id.confirm_password_text_input_edit_text);
        signUp = findViewById(R.id.sign_up_action);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();

        // add profile image click listener.
        profileCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ImageUtils.pickAPhoto(SignUpActivity.this);
            }
        });

        // add sign up click listener.
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String mail = mailEditText.getText().toString();
                String password = Objects.requireNonNull(passwordTextInputEditText.getText()).toString();
                String confirmPassword = Objects.requireNonNull(confirmPasswordTextInputEditText.getText()).toString();
                signUp(username, mail, password, confirmPassword);
            }
        });

        // add back button click listener.
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // validate input data.
    private boolean valid(String username, String email, String password, String confirmPassword) {
        boolean res = true;
        if (TextUtils.isEmpty(username.trim()) || TextUtils.isEmpty(email.trim())
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword.trim())) {
            Toast.makeText(SignUpActivity.this, "All Fields are required", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "valid: empty input");
            res = false;
            if (TextUtils.isEmpty(username.trim()))
                usernameEditText.setError("this Field is required");
            if (TextUtils.isEmpty(email.trim()))
                mailEditText.setError("this Field is required");
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mailEditText.setError("please Enter a valid Email Address");
                res = false;
                Log.w(TAG, "valid: invalid email address: " + email);
            }
            if (password.length() < 6) {
                Toast.makeText(SignUpActivity.this, "Password must be at least 6 digits", Toast.LENGTH_SHORT).show();
                res = false;
                Log.w(TAG, "valid: Password is less than 6 digits: " + password);
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "valid: Passwords don't match: " + password + " " + confirmPassword);
                res = false;
            }
        }
        return res;
    }

    // sign up with firebase.
    private void signUp(final String username, final String email, String password, String confirmPassword) {
        if (valid(username, email, password, confirmPassword)) {

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                // insert user data to firebase database.
                                User user = new User();
                                user.setUser_email(email);
                                user.setSearch_name(username.toLowerCase());
                                user.setProfile_image("");
                                user.setPhone("");
                                user.setUser_id(FirebaseAuth.getInstance().getUid());
                                user.setUser_name(username);
                                user.setStatus("connected");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("users")
                                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "insertUserDataIntoDatabase: Success");
                                        sendVerificationEmail();
                                        Intent intent = new Intent(SignUpActivity.this,SignInActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "insertUserDataIntoDatabase: failure", e.getCause());
                                        Toast.makeText(SignUpActivity.this, "failed inserting the user to the database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                // compress the profile image and upload it to storage
                                compressImage(((BitmapDrawable) profileCircleImageView.getDrawable()).getBitmap());
                            } else {
                                Log.w(TAG, "createUserWithEmail:failed", task.getException());
                                Toast.makeText(SignUpActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


            // set user personal data.
            FirebaseUser firebaseUser = Utils.getUser(SignUpActivity.this);
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();
            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: User profile Updated.");
                    }
                }
            });


        }
    }


    private void sendVerificationEmail() {
        FirebaseUser user = Utils.getUser(SignUpActivity.this);
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "sendVerificationEmail: task is successful");
                                Toast.makeText(SignUpActivity.this, "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "sendVerificationEmail: task is unsuccessful", task.getException());
                                Toast.makeText(SignUpActivity.this, "Couldn't Send Verification Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get the image selected for profile image.
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "OnActivityResult: RESULT_OK");
            if (requestCode == Utils.ImageUtils.RC_PHOTO_PICKER || requestCode == Utils.ImageUtils.REQUEST_IMAGE_CAPTURE) {
                Utils.ImageUtils.performCrop(Objects.requireNonNull(data).getData(), getCacheDir(), SignUpActivity.this);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                Log.d(TAG, "OnActivityResult: UCrop.REQUEST_CROP : " + UCrop.getOutput(Objects.requireNonNull(data)));
                profileCircleImageView.setImageBitmap(Utils.ImageUtils.getBitmap(UCrop.getOutput(data), getContentResolver()));
            } else if (requestCode == UCrop.RESULT_ERROR) {
                Log.w(TAG, "OnActivityResult: UCrop.RESULT_ERROR", UCrop.getError(Objects.requireNonNull(data)));
            }
        }
    }


    private void compressImage(final Bitmap profileImage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final byte[] bytes = Utils.ImageUtils.compressImage(SignUpActivity.this, profileImage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.ImageUtils.uploadProfileImage(SignUpActivity.this,bytes);
                    }
                });
            }
        }).start();
    }

}
