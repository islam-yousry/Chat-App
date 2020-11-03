package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.Util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private EditText mailEditText;
    private TextInputEditText passwordTextInputEditText;
    private Button signInButton;
    private ImageButton backImageButton;
    private TextView forgottenPassword, resendVerificationEmail;
    private ProgressBar progressBar;
    private CheckBox keepMeSignedInCheckBox;

    private void init() {
        mailEditText = findViewById(R.id.email_edit_text);
        passwordTextInputEditText = findViewById(R.id.password_text_input_edit_text);
        progressBar = findViewById(R.id.progress_bar);
        signInButton = findViewById(R.id.sign_in_action);
        backImageButton = findViewById(R.id.back_image_button);
        forgottenPassword = findViewById(R.id.forgotten_password_text_view);
        keepMeSignedInCheckBox = findViewById(R.id.keep_me_signedIn_checked_box);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();

        // add sign in button listener.
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mailEditText.getText().toString();
                String password = passwordTextInputEditText.getText().toString();
                signIn(email, password);
            }
        });


        // add back button listener.
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // add forgotten password click listener.
        forgottenPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.resendPassword(SignInActivity.this);
            }
        });

        // add resend verification email click listener.
        resendVerificationEmail = findViewById(R.id.resend_verification_email_text_view);
        resendVerificationEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = Utils.getUser(SignInActivity.this);
                if (user != null) {
                    user.sendEmailVerification();
                    Toast.makeText(SignInActivity.this, "sent Email Verification", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "resent Email Verification");
                }
            }
        });

    }


    // validate input data.
    private boolean valid(String email, String password) {
        boolean res = true;
        if (TextUtils.isEmpty(email.trim()) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignInActivity.this, "All Fields are required!", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "empty input");
            res = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mailEditText.setError("please Enter a valid E-mail Address");
            Log.w(TAG, "invalid mail input " + email);
            res = false;
        }
        return res;
    }


    // sign in with firebase.
    private void signIn(String email, String password) {
        if (valid(email, password)) {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = Utils.getUser(SignInActivity.this);
                                if (user.isEmailVerified()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("keepMeSigned", keepMeSignedInCheckBox.isChecked());
                                    startActivity(intent);
                                    finish();
                                } else {
                                    resendVerificationEmail.setVisibility(View.VISIBLE);
                                    Toast.makeText(SignInActivity.this, "Check your Email Inbox for a Verification Link", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "email isn't verified");
                                }
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                if (task.getException() instanceof FirebaseAuthInvalidUserException)
                                    Toast.makeText(getApplicationContext(), "This Email Address does not exist", Toast.LENGTH_SHORT).show();
                                else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                    Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(SignInActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }


}
