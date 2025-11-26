package com.example.sprintproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying chat messages.
 * Handles both user and assistant messages with different layouts.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;

    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    /**
     * Constructor initializes the adapter with an empty message list.
     */
    public ChatMessageAdapter() {
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    }

    /**
     * Gets the view type for the item at the given position.
     *
     * @param position the position of the item
     * @return VIEW_TYPE_USER or VIEW_TYPE_ASSISTANT
     */
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.isUserMessage()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_ASSISTANT;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == VIEW_TYPE_USER) {
            view = inflater.inflate(R.layout.item_message_user, parent, false);
        } else {
            view = inflater.inflate(R.layout.item_message_assistant, parent, false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Updates the message list and notifies the adapter.
     *
     * @param newMessages the new list of messages
     */
    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages != null ? new ArrayList<>(newMessages) : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Adds a new message to the list.
     *
     * @param message the message to add
     */
    public void addMessage(ChatMessage message) {
        if (message != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    /**
     * ViewHolder for chat messages.
     */
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessageContent;
        private TextView textMessageTime;

        /**
         * Constructor initializes view references.
         *
         * @param itemView the item view
         */
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessageContent = itemView.findViewById(R.id.text_message_content);
            textMessageTime = itemView.findViewById(R.id.text_message_time);
        }

        /**
         * Binds message data to the view.
         *
         * @param message the message to display
         */
        void bind(ChatMessage message) {
            if (message != null) {
                textMessageContent.setText(message.getContent());

                if (message.getTimestamp() != null) {
                    textMessageTime.setText(timeFormat.format(message.getTimestamp()));
                } else {
                    textMessageTime.setText("");
                }
            }
        }
    }
}

