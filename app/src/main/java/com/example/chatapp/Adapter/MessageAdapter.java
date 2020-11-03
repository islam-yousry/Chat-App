package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.Message;
import com.example.chatapp.R;
import com.example.chatapp.Util.Utils;
import com.example.chatapp.View.ProfileActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public final static int MSG_TYPE_RIGHT = 0;
    public final static int MSG_TYPE_LEFT = 1;
    private static final String TAG = "MessageAdapter";

    private final Context context;
    private List<Message> messageList = new ArrayList<>();

    public MessageAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageViewHolder holder = null;
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false);
            holder = new MessageViewHolder(view, MSG_TYPE_RIGHT);
        } else if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false);
            holder = new MessageViewHolder(view, MSG_TYPE_LEFT);
        }

        assert holder != null;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void submitList(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    public void add(Message message) {
        messageList.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;
        private final ImageView messageImageView;
        private final TextView messageTimeTextView;
        private CircleImageView messageProfileImage = null, messageRightStatusImage = null;

        public MessageViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_item_text_view);
            messageImageView = itemView.findViewById(R.id.message_item_image_view);
            messageTimeTextView = itemView.findViewById(R.id.message_item_time);
            if (itemViewType == MSG_TYPE_LEFT)
                messageProfileImage = itemView.findViewById(R.id.message_item_profile_image);
            else
                messageRightStatusImage = itemView.findViewById(R.id.message_right_status);

        }

        public void bind(final Message message) {
            // the message is plain text.
            if (!message.getPhoto_url().equals("")) {
                messageTextView.setVisibility(View.GONE);
                messageImageView.setVisibility(View.VISIBLE);
                Glide.with(context.getApplicationContext())
                        .load(message.getPhoto_url())
                        .into(messageImageView);


                messageImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.ImageUtils.showPhoto(context, Uri.parse(message.getPhoto_url()));
                    }
                });
            } else {// the message is an image.
                messageTextView.setVisibility(View.VISIBLE);
                messageImageView.setVisibility(View.GONE);
                messageTextView.setText(message.getText());
            }
            messageTimeTextView.setText(Utils.convertTime(message.getTime()));

            // the message is MessageLeftType (received).
            if (messageProfileImage != null) {
                // get the profile image.
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                        .child(message.getSender()).child("profile_image");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "reference: " + snapshot.getValue(String.class));
                        String profileUrl = snapshot.getValue(String.class);
                        Glide.with(context.getApplicationContext())
                                .load(profileUrl)
                                .placeholder(R.drawable.ic_person)
                                .into(messageProfileImage);

                        // set click listener.
                        messageProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ProfileActivity.class);
                                intent.putExtra("userId", message.getSender());
                                context.startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // set the message status to seen.
                FirebaseDatabase.getInstance().getReference("chats")
                        .child(Utils.getChatId(message.getReceiver(), message.getSender()))
                        .child(message.getMessage_id()).child("seen").setValue(true);
            }

            // the message is sent
            else if (messageRightStatusImage != null) {
                // check the message seen listener
                FirebaseDatabase.getInstance().getReference("chats")
                        .child(Utils.getChatId(message.getReceiver(), message.getSender()))
                        .child(message.getMessage_id()).child("seen")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue(Boolean.class)) {
                                    messageRightStatusImage.setBackgroundResource(R.drawable.ic_seen);
                                } else {
                                    messageRightStatusImage.setBackgroundResource(R.drawable.ic_notseen);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            // set show time when the card view is clicked.
            messageTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messageTimeTextView.getVisibility() == View.VISIBLE)
                        messageTimeTextView.setVisibility(View.GONE);
                    else
                        messageTimeTextView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser firebaseUser = Utils.getUser(context);
        if (messageList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else
            return MSG_TYPE_LEFT;
    }


}