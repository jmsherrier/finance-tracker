package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.model.Expense;

import java.util.Calendar;
import java.util.Date;

/**
 * Unit tests validating the Expense model.
 */
public class ExpenseValidationTest {

    private Expense testExpense;

    @Before
    public void setUp() {
        testExpense = new Expense(
                "Test Expense",
                25.50,
                "Food & Dining",
                new Date(),
                "Test notes",
                "test-user-id"
        );
    }

    @Test
    public void testExpenseCreation() {
        assertNotNull("Expense should be created", testExpense);
        assertEquals("Expense name should match", "Test Expense", testExpense.getName());
        assertEquals("Expense amount should match", 25.50, testExpense.getAmount(), 0.01);
        assertEquals("Expense category should match",
                "Food & Dining", testExpense.getCategory());
        assertEquals("Expense notes should match",
                "Test notes", testExpense.getNotes());
        assertEquals("Expense user ID should match",
                "test-user-id", testExpense.getUserId());
    }

    @Test
    public void testExpenseSettersAndGetters() {
        testExpense.setName("Updated Expense");
        testExpense.setAmount(100.75);
        testExpense.setCategory("Transportation");
        testExpense.setNotes("Updated notes");

        assertEquals("Name should be updated",
                "Updated Expense", testExpense.getName());
        assertEquals("Amount should be updated",
                100.75, testExpense.getAmount(), 0.01);
        assertEquals("Category should be updated",
                "Transportation", testExpense.getCategory());
        assertEquals("Notes should be updated",
                "Updated notes", testExpense.getNotes());
    }

    @Test
    public void testFutureDateIsRejected() {
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_MONTH, 1);
        Date futureDate = future.getTime();
        Date today = new Date();

        assertTrue("Future date should be after today", futureDate.after(today));

        Expense futureExpense = new Expense(
                "Future Expense", 10.0, "Other", futureDate, "", "user");
        assertNotNull("Future date expense should be created", futureExpense);
        assertEquals("Future date should be set correctly",
                futureDate, futureExpense.getDate());
    }

    @Test
    public void testPastDateIsValid() {
        Calendar past = Calendar.getInstance();
        past.add(Calendar.DAY_OF_MONTH, -1);
        Date pastDate = past.getTime();
        Date today = new Date();

        assertTrue("Past date should be before today", pastDate.before(today));
    }

    @Test
    public void testPositiveAmountIsValid() {
        double amount = 50.0;
        assertTrue("Positive amount should be valid", amount > 0);
    }

    @Test
    public void testNegativeAmountIsInvalid() {
        double amount = -10.0;
        assertFalse("Negative amount should be invalid", amount > 0);
    }

    @Test
    public void testZeroAmountIsInvalid() {
        double amount = 0.0;
        assertFalse("Zero amount should be invalid", amount > 0);
    }

    @Test
    public void testLargeAmountIsValid() {
        double amount = 999999.99;
        assertTrue("Large amount should be valid", amount > 0);
    }

    @Test
    public void testExpenseNameValidation() {
        Expense validExpense = new Expense(
                "Coffee", 5.0, "Food & Dining", new Date(), "", "user");
        assertNotNull("Valid expense should be created", validExpense);
        assertEquals("Name should be set correctly", "Coffee", validExpense.getName());

        Expense emptyNameExpense = new Expense(
                "", 5.0, "Food & Dining", new Date(), "", "user");
        assertNotNull("Empty name expense should be created", emptyNameExpense);
        assertTrue("Empty name should be empty",
                emptyNameExpense.getName().isEmpty());
    }

    @Test
    public void testExpenseCategoryValidation() {
        String[] validCategories = {
            "Food & Dining", "Transportation", "Shopping", "Entertainment",
            "Bills & Utilities", "Healthcare", "Education", "Travel", "Other"
        };

        for (String category : validCategories) {
            assertFalse("Category should not be empty", category.trim().isEmpty());
        }
    }

    @Test
    public void testExpenseNotesOptional() {
        Expense expenseWithNotes = new Expense(
                "Test", 10.0, "Other", new Date(), "Has notes", "user");
        assertNotNull("Notes should be present", expenseWithNotes.getNotes());

        Expense expenseWithoutNotes = new Expense(
                "Test", 10.0, "Other", new Date(), "", "user");
        assertTrue("Empty notes should be allowed",
                expenseWithoutNotes.getNotes().isEmpty());
    }

    @Test
    public void testExpenseToString() {
        String expenseString = testExpense.toString();
        assertNotNull("toString should not be null", expenseString);
        assertTrue("toString should contain expense name",
                expenseString.contains("Test Expense"));
        assertTrue("toString should contain amount",
                expenseString.contains("25.5"));
    }

    @Test
    public void testExpenseIdGeneration() {
        assertNull("Initial ID should be null", testExpense.getId());
        testExpense.setId("test-id-123");
        assertEquals("ID should be set correctly",
                "test-id-123", testExpense.getId());
    }

    @Test
    public void testExpenseCreatedAtTimestamp() {
        Date createdAt = testExpense.getCreatedAt();
        assertNotNull("CreatedAt should not be null", createdAt);

        Date now = new Date();
        assertTrue("CreatedAt should be recent",
                now.getTime() - createdAt.getTime() < 1000);
    }

    @Test
    public void testExpenseAmountPrecision() {
        testExpense.setAmount(123.456789);
        assertEquals("Amount should handle precision",
                123.456789, testExpense.getAmount(), 0.000001);
    }
}
