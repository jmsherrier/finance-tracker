package com.example.sprintproject.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a conversation session with the AI chatbot.
 * Stores conversation metadata including title and summary.
 */
public class ChatConversation {
    private String id;
    private String title;
    private String summary;
    private Date createdAt;
    private Date updatedAt;
    private String userId;
    private List<String> messageIds;

    /**
     * Default constructor for Firestore.
     */
    public ChatConversation() {
        this.messageIds = new ArrayList<>();
    }

    /**
     * Constructor for creating a new conversation.
     *
     * @param userId the user ID who owns this conversation
     */
    public ChatConversation(String userId) {
        this.userId = userId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.messageIds = new ArrayList<>();
    }

    /**
     * Gets the conversation ID.
     *
     * @return the conversation ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the conversation ID.
     *
     * @param id the conversation ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the conversation title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the conversation title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the conversation summary.
     *
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the conversation summary.
     *
     * @param summary the summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Gets the creation date.
     *
     * @return the creation date
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation date.
     *
     * @param createdAt the creation date
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update date.
     *
     * @return the last update date
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update date.
     *
     * @param updatedAt the last update date
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Updates the updatedAt timestamp to current time.
     */
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the list of message IDs.
     *
     * @return the list of message IDs
     */
    public List<String> getMessageIds() {
        return messageIds;
    }

    /**
     * Sets the list of message IDs.
     *
     * @param messageIds the list of message IDs
     */
    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }

    /**
     * Adds a message ID to the conversation.
     *
     * @param messageId the message ID to add
     */
    public void addMessageId(String messageId) {
        if (messageIds == null) {
            messageIds = new ArrayList<>();
        }
        messageIds.add(messageId);
    }

    @Override
    public String toString() {
        return "ChatConversation{"
                + "id='" + id + '\''
                + ", title='" + title + '\''
                + ", summary='" + summary + '\''
                + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt
                + ", userId='" + userId + '\''
                + ", messageIds=" + messageIds
                + '}';
    }
}


