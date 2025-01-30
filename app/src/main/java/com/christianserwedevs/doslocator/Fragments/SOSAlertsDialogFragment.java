package com.christianserwedevs.doslocator.Fragments;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.Model.SOSAlertInfo;
import com.christianserwedevs.doslocator.R;
import com.christianserwedevs.doslocator.SOSAlertsAdapter;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

public class SOSAlertsDialogFragment extends DialogFragment {

    private ArrayList<SOSAlertInfo> alertsList;
    private GoogleMap googleMap;

    public SOSAlertsDialogFragment(ArrayList<SOSAlertInfo> alertsList, GoogleMap googleMap) {
        this.alertsList = alertsList;
        this.googleMap = googleMap;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_sos_alerts_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.sos_alerts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SOSAlertsAdapter adapter = new SOSAlertsAdapter(alertsList, googleMap, this);
        recyclerView.setAdapter(adapter);

        return view;
    }
}

