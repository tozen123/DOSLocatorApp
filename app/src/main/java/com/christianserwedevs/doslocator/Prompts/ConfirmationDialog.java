package com.christianserwedevs.doslocator.Prompts;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.christianserwedevs.doslocator.R;

public class ConfirmationDialog {

    public interface OnDialogClickListener {
        void onConfirm();
        void onCancel();
    }

    public static void show(Context context, String title, String message, final OnDialogClickListener listener) {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null);

        // Initialize dialog components
        TextView textViewTitle = dialogView.findViewById(R.id.textViewTitle);
        TextView textViewMessage = dialogView.findViewById(R.id.textViewMessage);
        Button buttonYes = dialogView.findViewById(R.id.buttonYes);
        Button buttonNo = dialogView.findViewById(R.id.buttonNo);

        // Set title and message
        textViewTitle.setText(title);
        textViewMessage.setText(message);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Button listeners
        buttonYes.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        buttonNo.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onCancel();
            }
        });

        dialog.show();
    }
}
