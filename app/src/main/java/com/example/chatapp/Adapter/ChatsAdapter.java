package com.example.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.View.MessageActivity;
import com.example.chatapp.Model.Message;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {
    private List<User> chatsUsersList;
    private final Context context;

    public ChatsAdapter(Context context) {
        this.chatsUsersList = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ChatsAdapter.ChatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.ChatsViewHolder holder, int position) {
        holder.bind(chatsUsersList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatsUsersList.size();
    }

    public void submitList(List<User> chatsUsersList) {
        this.chatsUsersList = chatsUsersList;
        notifyDataSetChanged();
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName, lastMessage, chatTime;
        private final CircleImageView userProfileImage;
        private final View view;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            userName = itemView.findViewById(R.id.chat_item_profile_name);
            lastMessage = itemView.findViewById(R.id.chat_item_last_message);
            chatTime = itemView.findViewById(R.id.chat_item_time);
            userProfileImage = itemView.findViewById(R.id.chat_item_profile_image);
        }


        public void bind(final User user) {
            userName.setText(user.getUser_name());
            final FirebaseUser firebaseUser = Utils.getUser(context);
            assert firebaseUser != null;
            Query query = FirebaseDatabase.getInstance().getReference("chats")
                    .child(Utils.getChatId(user.getUser_id(), firebaseUser.getUid())).limitToLast(2);
            query.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals("background_color")) continue;
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            String sender = "other: ";
                            if(firebaseUser.getUid().equals(message.getSender()))
                                sender = "me: ";
                            if (!message.getText().equals(""))
                                lastMessage.setText(sender+message.getText());
                            if (!message.getPhoto_url().equals(""))
                                lastMessage.setText(sender+"sent a photo");
                            chatTime.setText(Utils.convertTime(message.getTime()));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            Glide.with(context.getApplicationContext())
                    .load(user.getProfile_image())
                    .placeholder(R.drawable.ic_person)
                    .into(userProfileImage);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra("userId", user.getUser_id());
                    context.startActivity(intent);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    createDeleteAlertDialog(firebaseUser.getUid(), user.getUser_id());
                    return true;
                }
            });
        }

        private void createDeleteAlertDialog(final String firebaseUserId, final String userId) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete chat")
                    .setMessage("Are you sure you want to delete this chat?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference("chats")
                                    .child(Utils.getChatId(firebaseUserId, userId)).removeValue();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                            reference.child(firebaseUserId).child("friends").child(userId).setValue(Utils.FriendState.WITHOUT_CHAT);
                            reference.child(userId).child("friends").child(firebaseUserId).setValue(Utils.FriendState.WITHOUT_CHAT);
                            notifyItemRemoved(getAdapterPosition());
                            notifyItemRangeChanged(getAdapterPosition(), chatsUsersList.size());
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setIcon(R.drawable.ic_warning)
                    .show();
        }
    }

}
