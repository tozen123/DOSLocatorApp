package com.christianserwedevs.doslocator.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.christianserwedevs.doslocator.Model.Message;
import com.christianserwedevs.doslocator.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CURRENT_USER = 1;
    private static final int VIEW_TYPE_OTHER_USER = 2;

    private final Context context;
    private final List<Message> messageList;
    private final String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_CURRENT_USER : VIEW_TYPE_OTHER_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CURRENT_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_current_user, parent, false);
            return new CurrentUserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_other_user, parent, false);
            return new OtherUserMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof CurrentUserMessageViewHolder) {
            ((CurrentUserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof OtherUserMessageViewHolder) {
            ((OtherUserMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class CurrentUserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;
        private final TextView timestampTextView;

        public CurrentUserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }

        public void bind(Message message) {
            messageTextView.setText(message.getText());
            timestampTextView.setText(message.getTimestamp());
        }
    }

    static class OtherUserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;
        private final TextView timestampTextView;

        public OtherUserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }

        public void bind(Message message) {
            messageTextView.setText(message.getText());
            timestampTextView.setText(message.getTimestamp());
        }
    }
}
