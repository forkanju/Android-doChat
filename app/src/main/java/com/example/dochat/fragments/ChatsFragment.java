package com.example.dochat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dochat.R;
import com.example.dochat.activities.ChatActivity;
import com.example.dochat.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatList;

    private DatabaseReference chatRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private String currentUid;

    private String retImage = "default_image";




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);
        chatList = privateChatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUid);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, chatViewHolder> mAdapter =
                new FirebaseRecyclerAdapter<Contacts, chatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final chatViewHolder holder, int position, @NonNull Contacts model) {

                        final String usersIDs = getRef(position).getKey();

                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {

                                    if (snapshot.hasChild("image")) {
                                        retImage = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage).into(holder.profileImage);
                                    }

                                    final String retName = snapshot.child("name").getValue().toString();
                                    final String retStatus = snapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName + "");
                                    holder.userStatus.setText("Last Seen: " + "\n" + "Date " + "Time");

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_name", retName + "");
                                            chatIntent.putExtra("visit_user_id", usersIDs + "");
                                            chatIntent.putExtra("visit_image", retImage + "");
                                            startActivity(chatIntent);
                                        }
                                    });


                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.users_display_layout, parent, false);
                        return new chatViewHolder(view);

                    }
                };

        chatList.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userName;
        TextView userStatus;

        public chatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
        }
    }
}