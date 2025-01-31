package com.christianserwedevs.doslocator;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.Activity.MessagingActivity;
import com.christianserwedevs.doslocator.Model.ChatUserInfo;

import java.util.List;

public class DynamicAdapter extends RecyclerView.Adapter<DynamicAdapter.DynamicViewHolder> {

    private final Context context;
    private final List<ChatUserInfo> chatUserList;

    public DynamicAdapter(Context context, List<ChatUserInfo> chatUserList) {
        this.context = context;
        this.chatUserList = chatUserList;
    }

    @NonNull
    @Override
    public DynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_to_chat, parent, false);
        return new DynamicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicViewHolder holder, int position) {
        ChatUserInfo chatUser = chatUserList.get(position);
        holder.childNameTextView.setText(chatUser.getFullName());
        holder.childEmailTextView.setText(chatUser.getEmail());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MessagingActivity.class);
            intent.putExtra("chatUserName", chatUser.getFullName());
            intent.putExtra("chatUserId", chatUser.getUserId());
            intent.putExtra("chatUserType", chatUser.getUserType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatUserList.size();
    }

    public static class DynamicViewHolder extends RecyclerView.ViewHolder {
        TextView childNameTextView, childEmailTextView;

        public DynamicViewHolder(@NonNull View itemView) {
            super(itemView);
            childNameTextView = itemView.findViewById(R.id.userNameTextView);
            childEmailTextView = itemView.findViewById(R.id.userEmailTextView);
        }
    }
}
