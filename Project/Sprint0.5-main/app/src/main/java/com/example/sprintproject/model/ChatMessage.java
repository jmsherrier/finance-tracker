package com.example.sprintproject.model;

import java.util.Date;

/**
 * Represents a chat message in a conversation.
 * Can be from either the user or the AI assistant.
 */
public class ChatMessage {
    private String id;
    private String conversationId;
    private String content;
    private String role; // "user" or "assistant"
    private Date timestamp;
    private String userId;

    /**
     * Default constructor for Firestore.
     */
    public ChatMessage() {
    }

    /**
     * Constructor for creating a new chat message.
     *
     * @param conversationId the ID of the conversation this message belongs to
     * @param content the message content
     * @param role "user" or "assistant"
     * @param userId the user ID
     */
    public ChatMessage(String conversationId, String content, String role,
                       String userId) {
        this.conversationId = conversationId;
        this.content = content;
        this.role = role;
        this.userId = userId;
        this.timestamp = new Date();
    }

    /**
     * Gets the message ID.
     *
     * @return the message ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the message ID.
     *
     * @param id the message ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the conversation ID.
     *
     * @return the conversation ID
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Sets the conversation ID.
     *
     * @param conversationId the conversation ID
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Gets the message content.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the message content.
     *
     * @param content the message content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the message role (user or assistant).
     *
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the message role.
     *
     * @param role "user" or "assistant"
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the message timestamp.
     *
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the message timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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
     * Checks if this message is from the user.
     *
     * @return true if the message is from the user
     */
    public boolean isUserMessage() {
        return "user".equals(role);
    }

    /**
     * Checks if this message is from the assistant.
     *
     * @return true if the message is from the assistant
     */
    public boolean isAssistantMessage() {
        return "assistant".equals(role);
    }

    @Override
    public String toString() {
        return "ChatMessage{"
                + "id='" + id + '\''
                + ", conversationId='" + conversationId + '\''
                + ", content='" + content + '\''
                + ", role='" + role + '\''
                + ", timestamp=" + timestamp
                + ", userId='" + userId + '\''
                + '}';
    }
}


