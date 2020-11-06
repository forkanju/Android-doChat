package com.example.dochat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dochat.R;
import com.example.dochat.adapters.MessagesAdapter;
import com.example.dochat.model.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ActionBar actionBar;
    private String messageReceiverName;
    private String messageSenderId;
    private String messageReceiverId;

    private String checker = "";
    private String myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ImageButton sendMessageButton;
    private ImageButton sendImageButton;
    private ImageButton sendFileButton;
    private EditText messageInputText;

    private RecyclerView userMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter mAdapter;

    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        sendMessageButton = findViewById(R.id.private_chat_send_button);
        sendImageButton = findViewById(R.id.send_image_button);
        sendFileButton = findViewById(R.id.send_file_button);
        messageInputText = findViewById(R.id.input_private_chat);

        messageReceiverName = getIntent().getExtras().get("user_name").toString();
        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();

        actionBar.setTitle("" + messageReceiverName);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        userMessagesList = findViewById(R.id.private_users_chat_list);

        mAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(mAdapter);

        mDialog = new ProgressDialog(this);


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };
                final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 1);
                        }
                        if (which == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 1);
                        }
                        if (which == 2) {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MSWord File"), 1);
                        }

                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        rootRef.child("Message").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        mAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //   Back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void sendMessage() {
        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(getApplicationContext(), "First write your message..", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Message/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Message/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Message")
                    .child(messageSenderId)
                    .child(messageReceiverId)
                    .push();
            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            Map messageBodyDetails = new HashMap();

            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message sent successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            mDialog.setTitle("Sending File");
            mDialog.setMessage("Please wait, while sending the file.");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {


                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Message/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Message/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Message")
                        .child(messageSenderId)
                        .child(messageReceiverId)
                        .push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map messageImageBody = new HashMap();
                                messageImageBody.put("message", downloadUrl);
                                messageImageBody.put("name", fileUri.getLastPathSegment());
                                messageImageBody.put("type", checker);
                                messageImageBody.put("from", messageSenderId);
                                messageImageBody.put("to", messageReceiverId);
                                messageImageBody.put("messageID", messagePushId);
//                                messageImageBody.put("time", saveCurrentTime);
//                                messageImageBody.put("date", saveCurrentDate);


                                Map messageBodyDetail = new HashMap();
                                messageBodyDetail.put(messageSenderRef + "/" + messagePushId, messageImageBody);
                                messageBodyDetail.put(messageReceiverRef + "/" + messagePushId, messageImageBody);

                                rootRef.updateChildren(messageBodyDetail);
                                mDialog.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mDialog.dismiss();
                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mDialog.setMessage((int) p + " % Uploading...");
                    }
                });


            } else if (checker.equals("image")) {


                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Message/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Message/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Message")
                        .child(messageSenderId)
                        .child(messageReceiverId)
                        .push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();


                            Map messageTextBody = new HashMap();

                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", messageReceiverId);
                            messageTextBody.put("messageID", messagePushId);

                            Map messageBodyDetails = new HashMap();

                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        mDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        mDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });


            } else {
                mDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}