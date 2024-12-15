package com.christianserwedevs.doslocator.Fragments.MainNavigation;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.christianserwedevs.doslocator.R;

public class MessagesFragment extends Fragment {

    private OnSwitchToMapListener switchToMapListener;

    // Interface to communicate with the hosting activity
    public interface OnSwitchToMapListener {
        void switchToMapFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Ensure the activity implements the interface
            switchToMapListener = (OnSwitchToMapListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSwitchToMapListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        Button backToMapButton = view.findViewById(R.id.buttomBackToMap);
        backToMapButton.setOnClickListener(v -> {
            if (switchToMapListener != null) {
                switchToMapListener.switchToMapFragment();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        switchToMapListener = null; // Avoid memory leaks
    }
}
