package com.example.dochat.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Browser;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dochat.R;
import com.example.dochat.activities.HomeActivity;
import com.example.dochat.activities.ImageViewActivity;
import com.example.dochat.model.Messages;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("image")) {
                    String receiverImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {


            if (fromUserId.equals(messageSenderId)) {

                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                //  holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage());

            } else {


                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

//                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setBackgroundResource(R.drawable.reciever_messages_layout);
                holder.receiverMessageText.setText(messages.getMessage());
            }
        } else if (fromMessageType.equals("image")) {

            if (fromUserId.equals(messageSenderId)) {

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);


            } else {

                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);


            }
        }

        //sender portion

        if (fromUserId.equals(messageSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Save File",
                                        "Cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    //  deleteSentMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (which == 1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (which == 3) {
                                    // deleteMessageForEveryOne(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel",
                                        "Delete for Everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //  deleteSentMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                }

                                if (which == 2) {
                                    //  deleteMessageForEveryOne(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "View this Image",
                                        "Cancel",
                                        "Delete for Everyone"

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //  deleteSentMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if (which == 2) {

                                }
                                if (which == 3) {
                                    //  deleteMessageForEveryOne(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                }
            });

            //Receiver portion
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Save file",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //deleteReceiveMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });

                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel",

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //  deleteReceiveMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });

                        builder.show();
                    } else if (userMessagesList.get(position).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "View this Image",
                                        "Cancel",


                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //  deleteReceiveMessages(position, holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), HomeActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if (which == 1) {

                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                }

                            }
                        });

                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView senderMessageText;
        private TextView receiverMessageText;
        private CircleImageView receiverProfileImage;
        private ImageView messageSenderPicture;
        private ImageView messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_messages_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
        }
    }


    private void deleteSentMessages(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessage())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(),
                            "Message has been Deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(),
                            "Error Occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void deleteReceiveMessages(final int position, final MessageViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessage())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(),
                            "Message has been Deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(),
                            "Error Occurr ed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteMessageForEveryOne(final int position, final MessageViewHolder holder) {

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessage())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessage())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(),
                                        "Message has been Deleted.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                } else {
                    Toast.makeText(holder.itemView.getContext(),
                            "Error Occurr ed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
