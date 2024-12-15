package com.christianserwedevs.doslocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.christianserwedevs.doslocator.Activity.UserRegistration.NewUserActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore firestoreDatabase;
    private EditText emailField, passwordField;
    private Button loginButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firestoreDatabase = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        emailField = findViewById(R.id.editText_email);
        passwordField = findViewById(R.id.editText_password);
        loginButton = findViewById(R.id.button_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            TextView textView_alreadyHaveAnAccount = findViewById(R.id.text_alreadyHaveAnAccount);
            textView_alreadyHaveAnAccount.setOnClickListener(view -> {
                startActivity(new Intent(LoginActivity.this, NewUserActivity.class));
                finish();
            });

            return insets;
        });

        loginButton.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        checkUserInCollection("parents", email, password);
    }

    private void checkUserInCollection(String collectionName, String email, String password) {
        firestoreDatabase.collection(collectionName)
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            String userId = querySnapshot.getDocuments().get(0).getId();
                            saveToSharedPreferences(email, collectionName, userId); // Save details
                            proceedToDashboard(collectionName, userId);
                        } else if (collectionName.equals("parents")) {
                            checkUserInCollection("children", email, password);
                        } else if (collectionName.equals("children")) {
                            checkUserInCollection("responders", email, password);
                        } else if (collectionName.equals("responders")) {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToDashboard(String userType, String userId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userType", userType);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private void saveToSharedPreferences(String email, String userType, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("userType", userType);
        editor.putString("userId", userId);
        editor.apply(); // Save changes
        Toast.makeText(this, "User details saved.", Toast.LENGTH_SHORT).show();
    }
}
