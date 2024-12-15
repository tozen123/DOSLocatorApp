package com.christianserwedevs.doslocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.christianserwedevs.doslocator.Activity.UserRegistration.NewUserActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class WelcomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private FirebaseFirestore firestoreDatabase;
    ConstraintLayout main;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        firestoreDatabase = FirebaseFirestore.getInstance();
        main = findViewById(R.id.main);
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        checkUserLogin();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            Button buttonAlreadyHaveAnAccount = findViewById(R.id.button_alreadyHaveAnAccount);
            Button buttonNewUser = findViewById(R.id.button_NewUser);

            buttonAlreadyHaveAnAccount.setOnClickListener(view -> {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                finish();
            });

            buttonNewUser.setOnClickListener(view -> {
                startActivity(new Intent(WelcomeActivity.this, NewUserActivity.class));
                finish();
            });

            return insets;
        });
    }

    private void checkUserLogin() {
        String email = sharedPreferences.getString("email", null);
        String userType = sharedPreferences.getString("userType", null);
        String userId = sharedPreferences.getString("userId", null);

        if (email != null && userType != null && userId != null) {
            main.setVisibility(View.INVISIBLE);
            firestoreDatabase.collection(userType)
                    .whereEqualTo("email", email)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            proceedToMainActivity(userType, userId);
                        } else {
                            clearSharedPreferences();
                        }
                    })
                    .addOnFailureListener(e -> {
                        System.err.println("Error validating user: " + e.getMessage());
                        clearSharedPreferences();
                    });
        }
        else {
            main.setVisibility(View.VISIBLE);

        }
    }

    private void proceedToMainActivity(String userType, String userId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userType", userType);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private void clearSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
