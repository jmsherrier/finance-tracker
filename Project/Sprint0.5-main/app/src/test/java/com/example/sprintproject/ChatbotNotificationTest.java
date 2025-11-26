package com.example.sprintproject;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.example.sprintproject.model.ChatMessage;
import com.example.sprintproject.model.ChatConversation;
import com.example.sprintproject.viewmodel.ChatbotViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for the Chatbot and Notifications feature.
 * Tests cover message handling, conversation management, and error notifications.
 */
public class ChatbotNotificationTest {

    private ChatMessage testUserMessage;
    private ChatMessage testAssistantMessage;
    private ChatConversation testConversation;
    private ChatbotViewModel chatbotViewModel;

    @Before
    public void setUp() {
        // Create test messages
        testUserMessage = new ChatMessage("conv-123", "Hello, how can I save money?", "user", "user-123");
        testAssistantMessage = new ChatMessage("conv-123", "I can help you analyze your spending!", "assistant", "user-123");
        
        // Create test conversation
        testConversation = new ChatConversation("user-123");
        testConversation.setId("conv-123");
        testConversation.setTitle("Budget Planning");
        
        // Initialize ViewModel
        chatbotViewModel = new ChatbotViewModel();
    }

    @Test
    public void testChatMessageCreation() {
        // Test user message creation
        assertNotNull("User message should be created", testUserMessage);
        assertEquals("Conversation ID should match", "conv-123", testUserMessage.getConversationId());
        assertEquals("Content should match", "Hello, how can I save money?", testUserMessage.getContent());
        assertEquals("Role should be user", "user", testUserMessage.getRole());
        assertEquals("User ID should match", "user-123", testUserMessage.getUserId());
        assertNotNull("Timestamp should be set", testUserMessage.getTimestamp());
        assertTrue("Message should be identified as user message", testUserMessage.isUserMessage());
        assertFalse("Message should not be identified as assistant message", testUserMessage.isAssistantMessage());
    }

    @Test
    public void testChatMessageRoleValidation() {
        // Test assistant message role
        assertNotNull("Assistant message should be created", testAssistantMessage);
        assertEquals("Role should be assistant", "assistant", testAssistantMessage.getRole());
        assertTrue("Message should be identified as assistant message", testAssistantMessage.isAssistantMessage());
        assertFalse("Message should not be identified as user message", testAssistantMessage.isUserMessage());
        
        // Test invalid role
        ChatMessage invalidMessage = new ChatMessage("conv-123", "Test", "invalid", "user-123");
        assertFalse("Invalid role should not be user", invalidMessage.isUserMessage());
        assertFalse("Invalid role should not be assistant", invalidMessage.isAssistantMessage());
    }

    @Test
    public void testChatConversationCreation() {
        // Test conversation creation
        assertNotNull("Conversation should be created", testConversation);
        assertEquals("User ID should match", "user-123", testConversation.getUserId());
        assertEquals("Title should match", "Budget Planning", testConversation.getTitle());
        assertNotNull("Created at should be set", testConversation.getCreatedAt());
        assertNotNull("Updated at should be set", testConversation.getUpdatedAt());
        assertNotNull("Message IDs list should be initialized", testConversation.getMessageIds());
    }

    @Test
    public void testChatConversationTimestampUpdate() {
        // Test timestamp update functionality
        Date originalUpdatedAt = testConversation.getUpdatedAt();
        
        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        testConversation.updateTimestamp();
        Date newUpdatedAt = testConversation.getUpdatedAt();
        
        assertNotNull("Updated timestamp should not be null", newUpdatedAt);
        assertTrue("Updated timestamp should be after original", newUpdatedAt.after(originalUpdatedAt));
    }

    @Test
    public void testChatConversationMessageIdManagement() {
        // Test adding message IDs
        assertTrue("Message IDs list should be empty initially", testConversation.getMessageIds().isEmpty());
        
        testConversation.addMessageId("msg-1");
        testConversation.addMessageId("msg-2");
        
        assertEquals("Should have 2 message IDs", 2, testConversation.getMessageIds().size());
        assertTrue("Should contain msg-1", testConversation.getMessageIds().contains("msg-1"));
        assertTrue("Should contain msg-2", testConversation.getMessageIds().contains("msg-2"));
        
        // Test setting message IDs
        List<String> newMessageIds = new ArrayList<>();
        newMessageIds.add("msg-3");
        newMessageIds.add("msg-4");
        testConversation.setMessageIds(newMessageIds);
        
        assertEquals("Should have 2 new message IDs", 2, testConversation.getMessageIds().size());
        assertTrue("Should contain msg-3", testConversation.getMessageIds().contains("msg-3"));
    }

    @Test
    public void testChatMessageSettersAndGetters() {
        // Test all getters and setters
        ChatMessage message = new ChatMessage();
        
        message.setId("test-id");
        message.setConversationId("test-conv");
        message.setContent("Test content");
        message.setRole("user");
        message.setUserId("test-user");
        Date testDate = new Date();
        message.setTimestamp(testDate);
        
        assertEquals("ID should be set correctly", "test-id", message.getId());
        assertEquals("Conversation ID should be set correctly", "test-conv", message.getConversationId());
        assertEquals("Content should be set correctly", "Test content", message.getContent());
        assertEquals("Role should be set correctly", "user", message.getRole());
        assertEquals("User ID should be set correctly", "test-user", message.getUserId());
        assertEquals("Timestamp should be set correctly", testDate, message.getTimestamp());
    }

    @Test
    public void testChatConversationSettersAndGetters() {
        // Test all getters and setters
        ChatConversation conversation = new ChatConversation();
        
        conversation.setId("test-id");
        conversation.setTitle("Test Title");
        conversation.setSummary("Test Summary");
        conversation.setUserId("test-user");
        Date testDate = new Date();
        conversation.setCreatedAt(testDate);
        conversation.setUpdatedAt(testDate);
        
        assertEquals("ID should be set correctly", "test-id", conversation.getId());
        assertEquals("Title should be set correctly", "Test Title", conversation.getTitle());
        assertEquals("Summary should be set correctly", "Test Summary", conversation.getSummary());
        assertEquals("User ID should be set correctly", "test-user", conversation.getUserId());
        assertEquals("Created at should be set correctly", testDate, conversation.getCreatedAt());
        assertEquals("Updated at should be set correctly", testDate, conversation.getUpdatedAt());
    }

    @Test
    public void testChatMessageDefaultConstructor() {
        // Test default constructor for Firestore deserialization
        ChatMessage emptyMessage = new ChatMessage();
        
        assertNull("Default ID should be null", emptyMessage.getId());
        assertNull("Default conversation ID should be null", emptyMessage.getConversationId());
        assertNull("Default content should be null", emptyMessage.getContent());
        assertNull("Default role should be null", emptyMessage.getRole());
        assertNull("Default timestamp should be null", emptyMessage.getTimestamp());
        assertNull("Default user ID should be null", emptyMessage.getUserId());
    }

    @Test
    public void testChatConversationDefaultConstructor() {
        // Test default constructor for Firestore deserialization
        ChatConversation emptyConversation = new ChatConversation();
        
        assertNull("Default ID should be null", emptyConversation.getId());
        assertNull("Default title should be null", emptyConversation.getTitle());
        assertNull("Default summary should be null", emptyConversation.getSummary());
        assertNull("Default user ID should be null", emptyConversation.getUserId());
        assertNull("Default created at should be null", emptyConversation.getCreatedAt());
        assertNull("Default updated at should be null", emptyConversation.getUpdatedAt());
        assertNotNull("Message IDs list should be initialized", emptyConversation.getMessageIds());
        assertTrue("Message IDs list should be empty", emptyConversation.getMessageIds().isEmpty());
    }

    @Test
    public void testChatbotViewModelLiveDataInitialization() {
        // Test that ViewModel initializes LiveData correctly
        assertNotNull("Messages LiveData should be initialized", chatbotViewModel.getMessages());
        assertNotNull("Conversations LiveData should be initialized", chatbotViewModel.getConversations());
        assertNotNull("Error LiveData should be initialized", chatbotViewModel.getError());
        assertNotNull("Loading LiveData should be initialized", chatbotViewModel.getLoading());
        assertNotNull("Current conversation ID LiveData should be initialized", chatbotViewModel.getCurrentConversationId());
        assertNotNull("Show context dialog LiveData should be initialized", chatbotViewModel.getShowContextDialog());
        
        // Test initial values
        assertFalse("Loading should be false initially", chatbotViewModel.getLoading().getValue() != null && chatbotViewModel.getLoading().getValue());
        assertFalse("Show context dialog should be false initially", chatbotViewModel.getShowContextDialog().getValue() != null && chatbotViewModel.getShowContextDialog().getValue());
    }


}

