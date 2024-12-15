package com.christianserwedevs.doslocator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.christianserwedevs.doslocator.Fragments.MainNavigation.MapFragment;
import com.christianserwedevs.doslocator.Fragments.MainNavigation.MessagesFragment;
import com.christianserwedevs.doslocator.Fragments.MainNavigation.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements MessagesFragment.OnSwitchToMapListener {

    private SharedPreferences sharedPreferences;
    private TextView textView_userType;

    // Fragments
    private Fragment mapFragment, messagesFragment, profileFragment;
    private Fragment activeFragment;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Shared Preferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        textView_userType = findViewById(R.id.textView_userType);

        String userType = sharedPreferences.getString("userType", null);
        textView_userType.setText(userType != null ? capitalizeUserType(userType) : "User");

        // Initialize Fragments
        mapFragment = new MapFragment();
        messagesFragment = new MessagesFragment();
        profileFragment = new ProfileFragment();
        activeFragment = mapFragment;

        fragmentManager = getSupportFragmentManager();

        // Add all fragments to the FragmentManager
        fragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, mapFragment, "MAP_FRAGMENT")
                .add(R.id.fragmentContainer, messagesFragment, "MESSAGES_FRAGMENT")
                .hide(messagesFragment)
                .add(R.id.fragmentContainer, profileFragment, "PROFILE_FRAGMENT")
                .hide(profileFragment)
                .commit();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBar);

        // Set Map as the default selected item

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_messages) {
                switchFragment(messagesFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                switchFragment(profileFragment);
                return true;
            } else {
                switchFragment(mapFragment);
                return true;
            }
        });

    }

    private void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            fragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }
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
    @Override
    public void switchToMapFragment() {
        switchFragment(mapFragment);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
    }

}
