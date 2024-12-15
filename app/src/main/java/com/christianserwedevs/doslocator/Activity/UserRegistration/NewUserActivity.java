package com.christianserwedevs.doslocator.Activity.UserRegistration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.christianserwedevs.doslocator.LoginActivity;
import com.christianserwedevs.doslocator.R;

public class NewUserActivity extends AppCompatActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);





            TextView textView_AlreadyHaveAnAccount = findViewById(R.id.textView_AlreadyHaveAnAccount);

            textView_AlreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(NewUserActivity.this, LoginActivity.class));
                    finish();
                }
            });


            FrameLayout buttonParent = findViewById(R.id.buttonParent);
            FrameLayout buttonChild = findViewById(R.id.buttonChild);
            FrameLayout buttonResponder = findViewById(R.id.buttonResponder);


            setFrameLayoutButton(buttonParent, UserInformationRegistrationActivity.class, "userType", "ParentType");
            setFrameLayoutButton(buttonChild, UserInformationRegistrationActivity.class, "userType", "ChildType");
            setFrameLayoutButton(buttonResponder, UserInformationRegistrationActivity.class, "userType", "ResponderType");




            return insets;
        });
    }

    private void setFrameLayoutButton(FrameLayout frameLayout, Class<?> targetActivity, String key, String value) {
        frameLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, targetActivity);
            Bundle bundle = new Bundle();
            bundle.putString(key, value);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }



}