package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Util.Utils;
import com.example.chatapp.View.MessageActivity;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList = new ArrayList<>();
    private final Context context;

    public UserAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void submitList(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }


    public class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final CircleImageView userProfileImage;
        private final View view;

        private final ImageButton accept, cancel;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            userName = itemView.findViewById(R.id.user_item_profile_name);
            userProfileImage = itemView.findViewById(R.id.user_item_profile_image);

            // in case there's request.
            accept = itemView.findViewById(R.id.user_item_accept_button);
            cancel = itemView.findViewById(R.id.user_item_cancel_button);
        }

        public void bind(final User user) {
            userName.setText(user.getUser_name());
            Glide.with(context.getApplicationContext())
                    .load(user.getProfile_image())
                    .placeholder(R.drawable.ic_person)
                    .into(userProfileImage);
            final FirebaseUser firebaseUser = Utils.getUser(context);
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users")
                    .child(firebaseUser.getUid())
                    .child("friends")
                    .child(user.getUser_id());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() == null || snapshot.getValue(String.class).equals(Utils.FriendState.SENT.toString()))
                        launchProfileActivity(view, user.getUser_id());
                    else if (snapshot.getValue(String.class).equals(Utils.FriendState.PENDING.toString())) {
                        setupAcceptListener(firebaseUser, user, reference);
                        setUpCancelListener(firebaseUser, user, reference);
                        launchProfileActivity(view, user.getUser_id());
                    } else if (snapshot.getValue(String.class).equals(Utils.FriendState.BLOCK.toString())) {
                        setUpCancelListener(firebaseUser, user, reference);
                    } else
                        launchMessageActivity(view, user.getUser_id());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }


        private void setupAcceptListener(final FirebaseUser firebaseUser, final User user, final DatabaseReference reference) {
            accept.setVisibility(View.VISIBLE);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference("chats")
                            .child(Utils.getChatId(user.getUser_id(), firebaseUser.getUid()))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                                            .child(user.getUser_id())
                                            .child("friends")
                                            .child(firebaseUser.getUid());
                                    if (snapshot.getValue() == null) {
                                        reference.setValue(Utils.FriendState.WITHOUT_CHAT);
                                        databaseReference.setValue(Utils.FriendState.WITHOUT_CHAT);
                                    }else{
                                        reference.setValue(Utils.FriendState.WITH_CHAT.toString());
                                        databaseReference.setValue(Utils.FriendState.WITH_CHAT);
                                    }

                                    accept.setVisibility(View.INVISIBLE);
                                    cancel.setVisibility(View.INVISIBLE);
                                    userList.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    notifyItemRangeChanged(getAdapterPosition(), userList.size());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            });


        }

        private void setUpCancelListener(final FirebaseUser firebaseUser, final User user, final DatabaseReference reference) {
            cancel.setVisibility(View.VISIBLE);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reference.removeValue();
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(user.getUser_id())
                            .child("friends")
                            .child(firebaseUser.getUid())
                            .removeValue();
                    accept.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.INVISIBLE);
                    userList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), userList.size());
                }
            });
        }

    }


    private void launchMessageActivity(View view, final String userId) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });
    }

    private void launchProfileActivity(View view, final String userId) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });
    }


}
