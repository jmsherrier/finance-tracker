package com.example.sprintproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.ChatConversation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying conversation history in a RecyclerView.
 */
public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {
    private List<ChatConversation> conversations;
    private OnConversationClickListener listener;
    private final SimpleDateFormat dateFormat;

    /**
     * Interface for handling conversation click events.
     */
    public interface OnConversationClickListener {
        /**
         * Called when a conversation is clicked.
         *
         * @param conversation the clicked conversation
         */
        void onConversationClick(ChatConversation conversation);
    }

    /**
     * Constructor initializes the adapter.
     */
    public ConversationListAdapter() {
        this.conversations = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }

    /**
     * Sets the click listener.
     *
     * @param listener the click listener
     */
    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ChatConversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    /**
     * Updates the conversation list.
     *
     * @param newConversations the new list of conversations
     */
    public void updateConversations(List<ChatConversation> newConversations) {
        this.conversations = newConversations != null ? newConversations : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for conversation items.
     */
    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final TextView textSummary;
        private final TextView textDate;

        /**
         * Constructor initializes view references.
         *
         * @param itemView the item view
         */
        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_conversation_title);
            textSummary = itemView.findViewById(R.id.text_conversation_summary);
            textDate = itemView.findViewById(R.id.text_conversation_date);
        }

        /**
         * Binds conversation data to views.
         *
         * @param conversation the conversation to bind
         */
        void bind(ChatConversation conversation) {
            if (conversation == null) {
                return;
            }

            // Set title
            String title = conversation.getTitle();
            if (title == null || title.isEmpty()) {
                title = "New Conversation";
            }
            textTitle.setText(title);

            // Set summary (if available)
            String summary = conversation.getSummary();
            if (summary != null && !summary.isEmpty()) {
                textSummary.setText(summary);
                textSummary.setVisibility(View.VISIBLE);
            } else {
                textSummary.setVisibility(View.GONE);
            }

            // Set date
            Date updatedAt = conversation.getUpdatedAt();
            if (updatedAt != null) {
                textDate.setText(dateFormat.format(updatedAt));
            } else {
                Date createdAt = conversation.getCreatedAt();
                if (createdAt != null) {
                    textDate.setText(dateFormat.format(createdAt));
                } else {
                    textDate.setText("");
                }
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });
        }
    }
}

