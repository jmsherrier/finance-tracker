package com.example.sprintproject.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ExpenseReminder model class.
 */
public class ExpenseReminderTest {

    private ExpenseReminder reminder;

    @Before
    public void setUp() {
        reminder = new ExpenseReminder();
    }

    @Test
    public void testConstructorWithParameters() {
        ExpenseReminder r = new ExpenseReminder(3, 1234567890L, "user123");

        assertEquals(3, r.getDaysSinceLastExpense());
        assertEquals(1234567890L, r.getLastExpenseTimestamp());
        assertEquals("user123", r.getUserId());
    }

    @Test
    public void testSettersAndGetters() {
        reminder.setDaysSinceLastExpense(5);
        reminder.setLastExpenseTimestamp(9876543210L);
        reminder.setUserId("testUser");

        assertEquals(5, reminder.getDaysSinceLastExpense());
        assertEquals(9876543210L, reminder.getLastExpenseTimestamp());
        assertEquals("testUser", reminder.getUserId());
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(reminder);
        assertEquals(0, reminder.getDaysSinceLastExpense());
        assertEquals(0L, reminder.getLastExpenseTimestamp());
        assertNull(reminder.getUserId());
    }

    @Test
    public void testZeroDaysSince() {
        reminder.setDaysSinceLastExpense(0);
        assertEquals(0, reminder.getDaysSinceLastExpense());
    }

    @Test
    public void testNegativeDaysSince() {
        reminder.setDaysSinceLastExpense(-1);
        assertEquals(-1, reminder.getDaysSinceLastExpense());
    }

    @Test
    public void testLargeDaysSince() {
        reminder.setDaysSinceLastExpense(365);
        assertEquals(365, reminder.getDaysSinceLastExpense());
    }

    @Test
    public void testNullUserId() {
        reminder.setUserId(null);
        assertNull(reminder.getUserId());
    }

    @Test
    public void testEmptyUserId() {
        reminder.setUserId("");
        assertEquals("", reminder.getUserId());
    }
}
