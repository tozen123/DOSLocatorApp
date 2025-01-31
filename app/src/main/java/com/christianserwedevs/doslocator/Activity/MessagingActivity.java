package com.christianserwedevs.doslocator.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.Adapter.MessageAdapter;
import com.christianserwedevs.doslocator.Model.Message;
import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private TextView chattingUserName;
    private EditText messageInput;
    private ImageButton sendButton;

    private FirebaseFirestore firestoreDatabase;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageList;

    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Initialize Firestore
        firestoreDatabase = FirebaseFirestore.getInstance();

        // Initialize UI elements
        chattingUserName = findViewById(R.id.headerBar).findViewById(R.id.userNameTextView); // Ensure proper ID
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Fetch current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userId", null);
        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);


        if (currentUserId == null) {
            Toast.makeText(this, "Error: Current user ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch other chat details from Intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("chatUserId");
        otherUserName = getIntent().getStringExtra("chatUserName");

        // Set the chatting user's name
        chattingUserName.setText(otherUserName != null ? otherUserName : "Chat");

        if (chatId == null) {
            checkOrCreateChat();
        } else {
            loadMessages();
        }

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void checkOrCreateChat() {
        firestoreDatabase.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null && participants.contains(otherUserId)) {
                            chatId = document.getId();
                            loadMessages();
                            return;
                        }
                    }
                    createChat(); // No existing chat, create a new one
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error checking for existing chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createChat() {
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(otherUserId);

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", participants);
        chatData.put("createdAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        chatData.put("conversation", new ArrayList<Map<String, Object>>());

        firestoreDatabase.collection("chats")
                .add(chatData)
                .addOnSuccessListener(documentReference -> {
                    chatId = documentReference.getId();
                    Toast.makeText(this, "Chat created!", Toast.LENGTH_SHORT).show();
                    loadMessages();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        firestoreDatabase.collection("chats").document(chatId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        messageList.clear();
                        List<Map<String, Object>> messages = (List<Map<String, Object>>) snapshot.get("conversation");

                        if (messages != null) {
                            for (Map<String, Object> msg : messages) {
                                String senderId = (String) msg.get("senderId");
                                String text = (String) msg.get("text");
                                String timestamp = (String) msg.get("timestamp");

                                messageList.add(new Message(senderId, text, timestamp));
                            }
                        }

                        messageAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Cannot send an empty message.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("text", text);
        message.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        firestoreDatabase.collection("chats").document(chatId)
                .update("conversation", FieldValue.arrayUnion(message))
                .addOnSuccessListener(aVoid -> {
                    messageInput.setText("");
                    Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
