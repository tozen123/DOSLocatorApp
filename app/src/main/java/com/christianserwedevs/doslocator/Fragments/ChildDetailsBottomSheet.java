package com.christianserwedevs.doslocator.Fragments.MainNavigation;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.christianserwedevs.doslocator.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.christianserwedevs.doslocator.R;

public class ChildDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_BIRTHDATE = "birthdate";
    private static final String ARG_CONTACT_NUMBER = "contactNumber";
    private static final String ARG_LAST_KNOWN = "lastKnown";
    private ImageButton openMessaging;
    public static ChildDetailsBottomSheet newInstance(String name, String birthdate, String contactNumber, String lastKnown) {
        ChildDetailsBottomSheet fragment = new ChildDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_BIRTHDATE, birthdate);
        args.putString(ARG_CONTACT_NUMBER, contactNumber);
        args.putString(ARG_LAST_KNOWN, lastKnown);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_child_details, container, false);

        TextView nameTextView = view.findViewById(R.id.child_name);
        TextView birthdateTextView = view.findViewById(R.id.child_birthdate);
        TextView contactNumberTextView = view.findViewById(R.id.child_contact);
        TextView lastKnown = view.findViewById(R.id.child_lastknown_location);
        openMessaging = view.findViewById(R.id.openMessaging);

        openMessaging.setOnClickListener(v -> openMessagingFragment());
        if (getArguments() != null) {
            nameTextView.setText(getArguments().getString(ARG_NAME));
            birthdateTextView.setText(getArguments().getString(ARG_BIRTHDATE));
            contactNumberTextView.setText(getArguments().getString(ARG_CONTACT_NUMBER));
            lastKnown.setText(getArguments().getString(ARG_LAST_KNOWN));
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
