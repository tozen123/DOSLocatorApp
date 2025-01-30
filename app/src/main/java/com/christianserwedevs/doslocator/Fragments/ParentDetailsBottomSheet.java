package com.christianserwedevs.doslocator.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.christianserwedevs.doslocator.Fragments.MainNavigation.MessagesFragment;
import com.christianserwedevs.doslocator.MainActivity;
import com.christianserwedevs.doslocator.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ParentDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_BIRTHDATE = "birthdate";
    private static final String ARG_CONTACT = "contact";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_LAST_KNOWN = "lastKnown";
    private ImageButton openMessaging;
    public static ParentDetailsBottomSheet newInstance(String name, String birthdate, String contact, String email,  String lastKnown) {
        ParentDetailsBottomSheet fragment = new ParentDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_BIRTHDATE, birthdate);
        args.putString(ARG_CONTACT, contact);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_LAST_KNOWN, lastKnown);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_parent_details, container, false);

        TextView nameTextView = view.findViewById(R.id.parent_name);
        TextView birthdateTextView = view.findViewById(R.id.parent_birthdate);
        TextView contactTextView = view.findViewById(R.id.parent_contact);
        TextView emailTextView = view.findViewById(R.id.parent_email);
        TextView lastKnwonTextView = view.findViewById(R.id.parent_lastknown_location);
        openMessaging = view.findViewById(R.id.openMessaging);

        openMessaging.setOnClickListener(v -> openMessagingFragment());
        if (getArguments() != null) {
            nameTextView.setText(getArguments().getString(ARG_NAME));
            birthdateTextView.setText(getArguments().getString(ARG_BIRTHDATE));
            contactTextView.setText(getArguments().getString(ARG_CONTACT));
            emailTextView.setText(getArguments().getString(ARG_EMAIL));
            lastKnwonTextView.setText(getArguments().getString(ARG_LAST_KNOWN));
        }

        return view;
    }
    private void openMessagingFragment() {
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).switchFragment(((MainActivity) activity).messagesFragment);
        }
        dismiss(); // Close the BottomSheet
    }


}
