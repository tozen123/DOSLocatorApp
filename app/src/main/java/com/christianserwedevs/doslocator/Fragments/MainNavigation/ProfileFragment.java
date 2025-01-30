package com.christianserwedevs.doslocator.Fragments.MainNavigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.christianserwedevs.doslocator.LoginActivity;
import com.christianserwedevs.doslocator.Prompts.ConfirmationDialog;
import com.christianserwedevs.doslocator.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FirebaseFirestore firestoreDatabase;
    private TextView textViewAddress, textViewBirthdate, textViewContact, textViewEmail, textViewFullName, textViewParentEmail;
    LinearLayout parentRowLL;

    LinearLayout birthdate_rowLL;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firestoreDatabase = FirebaseFirestore.getInstance();

        textViewAddress = view.findViewById(R.id.textViewAddress);
        textViewBirthdate = view.findViewById(R.id.textViewBirthdate);
        textViewContact = view.findViewById(R.id.textViewContact);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewFullName = view.findViewById(R.id.textViewFullName);
        textViewParentEmail = view.findViewById(R.id.textViewParentEmail);
        parentRowLL = view.findViewById(R.id.parentRowLL);
        birthdate_rowLL = view.findViewById(R.id.birthdate_row);


        textViewParentEmail = view.findViewById(R.id.textViewParentEmail);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "No ID Found");
        String userType = sharedPreferences.getString("userType", "Unknown");

        showUserTypeToast(userType);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        if (userType != null && userId != null) {
            fetchUserProfileData(userType, userId);
        } else {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void showUserTypeToast(String userType) {
        String message;

        switch (userType) {
            case "parents":
                message = "Logged in as: Parent";
                parentRowLL.setVisibility(View.GONE);
                break;
            case "children":
                message = "Logged in as: Child";
                break;
            case "responders":
                message = "Logged in as: Responder";
                birthdate_rowLL.setVisibility(View.GONE);
                parentRowLL.setVisibility(View.GONE);

                break;
            default:
                message = "User type: Unknown";
                break;
        }

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmation() {
        ConfirmationDialog.show(requireContext(), "Logout", "Are you sure you want to logout?", new ConfirmationDialog.OnDialogClickListener() {
            @Override
            public void onConfirm() {
                performLogout();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    private void performLogout() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LoginPrefs", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
    }

    private String capitalizeUserType(String userType) {
        switch (userType) {
            case "parents":
                return "Parent";
            case "children":
                return "Child";
            case "responders":
                return "Responder";
            default:
                return "User";
        }
    }

    private void fetchUserProfileData(String userType, String userId) {
        firestoreDatabase.collection(userType).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displayUserData(documentSnapshot);
                    } else {
                        Toast.makeText(requireContext(), "No user data found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void displayUserData(DocumentSnapshot documentSnapshot) {
        String firstName = documentSnapshot.getString("firstName");
        String middleName = documentSnapshot.getString("middleName");
        String lastName = documentSnapshot.getString("lastName");
        String email = documentSnapshot.getString("email");

        String fullName = firstName + " " + (middleName != null ? middleName + " " : "") + lastName;
        textViewFullName.setText(fullName);

        textViewEmail.setText((email != null ? email : "N/A"));
        textViewAddress.setText(documentSnapshot.getString("address"));
        textViewBirthdate.setText(documentSnapshot.getString("birthdate"));
        textViewContact.setText(documentSnapshot.getString("contact"));
        textViewParentEmail.setText(documentSnapshot.getString("parentEmail"));
    }
}
