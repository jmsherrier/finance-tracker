package com.example.sprintproject;

import static org.junit.Assert.*;

import com.example.sprintproject.model.ChatMessage;
import com.example.sprintproject.model.ChatConversation;

import org.junit.Test;

import java.util.Date;

public class ChatBotTest {

    // ------------------------------------------------------------
    // ChatMessage Tests (5 tests)
    // ------------------------------------------------------------

    @Test
    public void testMessageConstructorStoresFields() {
        ChatMessage msg = new ChatMessage("c1", "Hello", "user", "u1");

        assertEquals("c1", msg.getConversationId());
        assertEquals("Hello", msg.getContent());
        assertEquals("user", msg.getRole());
        assertEquals("u1", msg.getUserId());
    }

    @Test
    public void testMessageTimestampInitialized() {
        ChatMessage msg = new ChatMessage("c1", "Test", "assistant", "u2");

        assertNotNull(msg.getTimestamp());
        assertTrue(msg.getTimestamp() instanceof Date);
    }

    @Test
    public void testIsUserMessage() {
        ChatMessage msg = new ChatMessage("c1", "Hi", "user", "u1");

        assertTrue(msg.isUserMessage());
        assertFalse(msg.isAssistantMessage());
    }

    @Test
    public void testIsAssistantMessage() {
        ChatMessage msg = new ChatMessage("c1", "Hello", "assistant", "u1");

        assertFalse(msg.isUserMessage());
        assertTrue(msg.isAssistantMessage());
    }

    @Test
    public void testSettersWorkCorrectly() {
        ChatMessage msg = new ChatMessage("c1", "Hello", "user", "u1");

        msg.setContent("Updated");
        msg.setRole("assistant");

        assertEquals("Updated", msg.getContent());
        assertEquals("assistant", msg.getRole());
    }

    // ------------------------------------------------------------
    // ChatConversation Tests (5 tests)
    // ------------------------------------------------------------

    @Test
    public void testConversationStartsEmpty() {
        ChatConversation conv = new ChatConversation();
        assertEquals(0, conv.getMessageIds().size());
    }

    @Test
    public void testAddMessageIdIncreasesListSize() {
        ChatConversation conv = new ChatConversation();
        conv.addMessageId("m1");

        assertEquals(1, conv.getMessageIds().size());
        assertEquals("m1", conv.getMessageIds().get(0));
    }

    @Test
    public void testConversationConstructorStoresUserId() {
        ChatConversation conv = new ChatConversation("user123");

        assertEquals("user123", conv.getUserId());
        assertNotNull(conv.getCreatedAt());
        assertNotNull(conv.getUpdatedAt());
    }

    @Test
    public void testUpdateTimestampChangesValue() throws InterruptedException {
        ChatConversation conv = new ChatConversation("u1");
        Date before = conv.getUpdatedAt();

        Thread.sleep(10); // ensure difference
        conv.updateTimestamp();
        Date after = conv.getUpdatedAt();

        assertTrue(after.after(before));
    }

    @Test
    public void testSettersUpdateFieldsCorrectly() {
        ChatConversation conv = new ChatConversation();

        conv.setTitle("My Chat");
        conv.setSummary("This is a summary.");
        conv.setUserId("u99");

        assertEquals("My Chat", conv.getTitle());
        assertEquals("This is a summary.", conv.getSummary());
        assertEquals("u99", conv.getUserId());
    }
}
