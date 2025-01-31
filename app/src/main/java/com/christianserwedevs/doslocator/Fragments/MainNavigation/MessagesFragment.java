package com.christianserwedevs.doslocator.Fragments.MainNavigation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.DynamicAdapter;
import com.christianserwedevs.doslocator.Model.ChatUserInfo;
import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private RecyclerView messagesRecyclerView;
    private DynamicAdapter dynamicAdapter;
    private FirebaseFirestore firestoreDatabase;

    private List<ChatUserInfo> chatUserList = new ArrayList<>();
    private String userType;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        messagesRecyclerView = view.findViewById(R.id.availabletochat);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        dynamicAdapter = new DynamicAdapter(requireContext(), chatUserList);
        messagesRecyclerView.setAdapter(dynamicAdapter);

        firestoreDatabase = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        userType = sharedPreferences.getString("userType", null);
        userId = sharedPreferences.getString("userId", null);

        if (userType == null || userId == null) {
            Toast.makeText(requireContext(), "User type or ID not found.", Toast.LENGTH_SHORT).show();
            return view;
        }

        fetchChatUsers();

        return view;
    }

    private void fetchChatUsers() {
        if ("parents".equals(userType)) {
            fetchChildrenByParentId(userId);
        } else if ("children".equals(userType)) {
            fetchParentByChildId(userId);
        } else if ("responders".equals(userType)) {
            fetchChildrenForResponder();
        } else {
            Toast.makeText(requireContext(), "Invalid user type for messaging.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchChildrenByParentId(String parentId) {
        firestoreDatabase.collection("parents")
                .document(parentId)
                .get()
                .addOnSuccessListener(document -> {
                    String parentEmail = document.getString("email");
                    if (parentEmail != null) {
                        firestoreDatabase.collection("children")
                                .whereEqualTo("parentEmail", parentEmail)
                                .get()
                                .addOnSuccessListener(task -> populateChatUserList(task, "child"))
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Failed to fetch children: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to fetch parent details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchParentByChildId(String childId) {
        firestoreDatabase.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(document -> {
                    String parentEmail = document.getString("parentEmail");
                    if (parentEmail != null) {
                        firestoreDatabase.collection("parents")
                                .whereEqualTo("email", parentEmail)
                                .get()
                                .addOnSuccessListener(task -> populateChatUserList(task, "parent"))
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Failed to fetch parent details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to fetch child details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchChildrenForResponder() {
        firestoreDatabase.collection("sos_alerts")
                .whereEqualTo("KeepTracking", true)
                .get()
                .addOnSuccessListener(task -> {
                    chatUserList.clear();
                    List<String> userIds = new ArrayList<>();

                    for (DocumentSnapshot document : task.getDocuments()) {
                        String userId = document.getString("userId");
                        if (userId != null) {
                            userIds.add(userId);
                        }
                    }

                    if (!userIds.isEmpty()) {
                        firestoreDatabase.collection("children")
                                .whereIn("userId", userIds)
                                .get()
                                .addOnSuccessListener(childTask -> populateChatUserList(childTask, "child"))
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Failed to fetch child details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(requireContext(), "No active SOS alerts found for children.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to fetch SOS alerts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void populateChatUserList(QuerySnapshot task, String chatUserType) {
        chatUserList.clear();
        for (DocumentSnapshot document : task.getDocuments()) {
            String fullName = document.getString("firstName") + " " +
                    (document.getString("middleName") != null ? document.getString("middleName") + " " : "") +
                    document.getString("lastName");
            String email = document.getString("email");
            String userId = document.getId();
            chatUserList.add(new ChatUserInfo(fullName, email, userId, chatUserType));
        }
        dynamicAdapter.notifyDataSetChanged();
    }
}
