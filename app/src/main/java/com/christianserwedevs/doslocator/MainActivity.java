package com.christianserwedevs.doslocator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.christianserwedevs.doslocator.Fragments.MainNavigation.MapFragment;
import com.christianserwedevs.doslocator.Fragments.MainNavigation.MessagesFragment;
import com.christianserwedevs.doslocator.Fragments.MainNavigation.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView textView_userType;

    // Fragments
    private Fragment mapFragment;
    public Fragment messagesFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        textView_userType = findViewById(R.id.textView_userType);

        String userType = sharedPreferences.getString("userType", null);
        textView_userType.setText(userType != null ? capitalizeUserType(userType) : "User");

        mapFragment = new MapFragment();
        messagesFragment = new MessagesFragment();
        profileFragment = new ProfileFragment();
        activeFragment = mapFragment;

        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, mapFragment, "MAP_FRAGMENT")
                .add(R.id.fragmentContainer, messagesFragment, "MESSAGES_FRAGMENT")
                .hide(messagesFragment)
                .add(R.id.fragmentContainer, profileFragment, "PROFILE_FRAGMENT")
                .hide(profileFragment)
                .commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBar);

        bottomNavigationView.setSelectedItemId(R.id.navigation_map);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_map) {
                switchFragment(mapFragment);
                return true;
            } else if (itemId == R.id.navigation_messages) {
                switchFragment(messagesFragment);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
        // **Handle Intent from Notification**
        if (getIntent().getBooleanExtra("open_map_fragment", false)) {
            openMapFragment();
        }

    }
    private void openMapFragment() {
        switchFragment(mapFragment);
    }
    public void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            fragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBar);

        int targetItemId = getMenuItemIdForFragment(targetFragment);
        if (bottomNavigationView.getSelectedItemId() != targetItemId) {
            bottomNavigationView.setOnItemSelectedListener(null); // 🚨 Temporarily remove listener
            bottomNavigationView.setSelectedItemId(targetItemId);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                return handleNavigationSelection(item);
            });
        }
    }

    private int getMenuItemIdForFragment(Fragment fragment) {
        if (fragment instanceof MapFragment) {
            return R.id.navigation_map;
        } else if (fragment instanceof MessagesFragment) {
            return R.id.navigation_messages;
        } else if (fragment instanceof ProfileFragment) {
            return R.id.navigation_profile;
        }
        return R.id.navigation_map; // Default
    }

    private boolean handleNavigationSelection(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_map) {
            switchFragment(mapFragment);
            return true;
        } else if (itemId == R.id.navigation_messages) {
            switchFragment(messagesFragment);
            return true;
        } else if (itemId == R.id.navigation_profile) {
            switchFragment(profileFragment);
            return true;
        }
        return false;
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


}
