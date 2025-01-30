package com.christianserwedevs.doslocator.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.ChildInfo;
import com.christianserwedevs.doslocator.ChildInfoAdapter;
import com.christianserwedevs.doslocator.R;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.HashMap;

public class ChildrenListDialogFragment extends DialogFragment {

    private ArrayList<ChildInfo> childrenInfoList;
    private GoogleMap googleMap;
    private HashMap<String, com.google.android.gms.maps.model.Marker> childMarkers;

    public ChildrenListDialogFragment(ArrayList<ChildInfo> childrenInfoList, GoogleMap googleMap, HashMap<String, com.google.android.gms.maps.model.Marker> childMarkers) {
        this.childrenInfoList = childrenInfoList;
        this.googleMap = googleMap;
        this.childMarkers = childMarkers;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_children_list, container, false);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewChildren);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ChildInfoAdapter adapter = new ChildInfoAdapter(requireContext(), childrenInfoList, googleMap, childMarkers);
        recyclerView.setAdapter(adapter);



        return dialogView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Set custom height and width
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(300));
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
