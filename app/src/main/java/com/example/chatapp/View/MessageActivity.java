package com.example.chatapp.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Adapter.MessageAdapter;
import com.example.chatapp.Model.Message;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.example.chatapp.Util.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 1;
    private static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private static final String TAG = "MessageActivity";

    private EditText messageEditText;
    private ImageButton photoPickerButton, sendButton;
    private RecyclerView messageRecycleView;
    private MessageAdapter messageAdapter;
    private TextView toolbarUsername;
    private CircleImageView toolbarCircleImage;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private String otherUserId;
    private String otherUserNotificationKey;
    private String otherUsername;
    private FirebaseUser firebaseUser;
    private List<Message> messageList;
    private LinearLayout linearLayout;


    private void init() {
        toolbar = findViewById(R.id.toolbar);
        toolbarUsername = findViewById(R.id.toolbar_title_text_view);
        toolbarCircleImage = findViewById(R.id.toolbar_image_view);
        messageEditText = findViewById(R.id.message_edit_text);
        photoPickerButton = findViewById(R.id.photo_picker_button);
        sendButton = findViewById(R.id.send_button);
        messageRecycleView = findViewById(R.id.messages_recycle_view);
        progressBar = findViewById(R.id.progress_bar);
        firebaseUser = Utils.getUser(this);
        linearLayout = findViewById(R.id.linear_layout);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // get the user id who you chat with.
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("userId");

        init();
        setActionBar();
        setAdapter();
        readMessages(otherUserId);


        // set send message button click listener.
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete Action using"), RC_PHOTO_PICKER);
            }
        });

        // set text change listener for the message edit text.
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setClickable(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // set send message button click listener.
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("");
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

        // set user data to the toolbar
        DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final User user = snapshot.getValue(User.class);
                Log.d(TAG, "userDatabaseReference: onDataChange: " + user);
                // set up toolbar.
                assert user != null;
                toolbarUsername.setText(user.getUser_name());
                otherUserNotificationKey = user.getNotification_key();
                otherUsername = user.getUser_name();
                Glide.with(getApplicationContext())
                        .load(Uri.parse(user.getProfile_image()))
                        .placeholder(R.drawable.ic_person)
                        .into(toolbarCircleImage);

                toolbarCircleImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
                        intent.putExtra("userId", user.getUser_id());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setAdapter() {
        messageAdapter = new MessageAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MessageActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecycleView.setLayoutManager(linearLayoutManager);
        messageRecycleView.setAdapter(messageAdapter);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RC_PHOTO_PICKER) {
            assert data != null;
            Uri photoSelectedUri = data.getData();
            progressBar.setVisibility(View.VISIBLE);
            compressImage(photoSelectedUri);
        }
    }


    private void compressImage(final Uri photoSelectedUri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final byte[] bytes = Utils.ImageUtils.compressImage(MessageActivity.this, Utils.ImageUtils.getBitmap(photoSelectedUri, getContentResolver()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadMessageImage(bytes, photoSelectedUri.getLastPathSegment());
                    }
                });
            }
        }).start();
    }

    private void readMessages(final String userid) {
        messageList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats")
                .child(Utils.getChatId(firebaseUser.getUid(), userid));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals("background_color")) {
                        setBackgroundColor(Utils.Colors.valueOf(snapshot.getValue(String.class)));
                        continue;
                    }
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        if (message.getReceiver().equals(userid) && message.getSender().equals(firebaseUser.getUid())
                                || message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userid)) {
                            messageList.add(message);
                        }
                    }
                }
                messageAdapter.submitList(messageList);
                Log.d(TAG, "chatsDatabaseReference: OnDataChange: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_activity_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("change color theme"))
            return false;
        FirebaseDatabase.getInstance().getReference("chats")
                .child(Utils.getChatId(firebaseUser.getUid(),otherUserId))
                .child("background_color").setValue(item.getTitle().toString().toUpperCase());
        return true;
    }

    private void setBackgroundColor(Utils.Colors value) {
        switch (value) {
            case AMBER:
            toolbar.setBackgroundResource(R.drawable.drawable_amber_gradient);
            messageRecycleView.setBackgroundResource(R.drawable.drawable_amber_gradient);
            linearLayout.setBackgroundResource(R.drawable.drawable_amber_gradient);
            break;
            case RED:
                toolbar.setBackgroundResource(R.drawable.drawable_red_gradient);
                messageRecycleView.setBackgroundResource(R.drawable.drawable_red_gradient);
                linearLayout.setBackgroundResource(R.drawable.drawable_red_gradient);
                break;
            case PURPLE:
                toolbar.setBackgroundResource(R.drawable.drawable_purple_gradient);
                messageRecycleView.setBackgroundResource(R.drawable.drawable_purple_gradient);
                linearLayout.setBackgroundResource(R.drawable.drawable_purple_gradient);
                break;
            default:
                toolbar.setBackgroundResource(R.drawable.drawable_green_gradient);
                messageRecycleView.setBackgroundResource(R.drawable.drawable_green_gradient);
                linearLayout.setBackgroundResource(R.drawable.drawable_green_gradient);
        }
    }

    private void uploadMessageImage(byte[] compressedImage, final String pathSegment) {
        final StorageReference photoReference = FirebaseStorage.getInstance().getReference().child("images/chats")
                .child(Utils.getChatId(firebaseUser.getUid(), otherUserId)).child(pathSegment);
        UploadTask uploadTask = photoReference.putBytes(compressedImage);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "uploadImage: failure", e.getCause());
                Toast.makeText(MessageActivity.this, "Photo couldn't be uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "uploadImage: success");
                photoReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressBar.setVisibility(View.INVISIBLE);
                        sendMessage(uri.toString());
                    }
                });
            }
        });
    }

    private void sendMessage(String profileUri) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats").child(Utils.getChatId(otherUserId, firebaseUser.getUid())).push();
        String messageId = reference.getKey();
        Message message = new Message(messageEditText.getText().toString(), profileUri, firebaseUser.getUid(), otherUserId, System.currentTimeMillis(), messageId);
        reference.setValue(message);
        Utils.sendNotification("Chat App: New Message", otherUsername+ ": "+ (profileUri.isEmpty()?message.getText():"send photo"),otherUserNotificationKey,message.getSender());
        // update exist chat flag to both users.
        FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid()).child("friends").child(otherUserId).setValue(Utils.FriendState.WITH_CHAT);
        FirebaseDatabase.getInstance().getReference("users").child(otherUserId).child("friends").child(firebaseUser.getUid()).setValue(Utils.FriendState.WITH_CHAT);

        messageEditText.setText("");
        messageRecycleView.scrollToPosition(messageList.size());
    }

}




