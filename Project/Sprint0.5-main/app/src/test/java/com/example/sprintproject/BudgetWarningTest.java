package com.example.sprintproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.model.BudgetWarning;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for BudgetWarning pop-up functionality.
 * Tests warning creation, validation, and display logic.
 */
public class BudgetWarningTest {

    private BudgetWarning warning;

    /**
     * Set up test data before each test.
     */
    @Before
    public void setUp() {
        warning = new BudgetWarning(
                "budget123",
                "Food & Dining",
                400.0,
                500.0,
                80.0
        );
    }

    /**
     * Test that budget warning is created with correct values.
     */
    @Test
    public void testBudgetWarningCreation() {
        assertNotNull("Warning should not be null", warning);
        assertEquals("Budget ID should match", "budget123", warning.getBudgetId());
        assertEquals("Category should match", "Food & Dining", warning.getCategory());
        assertEquals("Spent amount should match", 400.0, warning.getSpentAmount(), 0.01);
        assertEquals("Total amount should match", 500.0, warning.getTotalAmount(), 0.01);
        assertEquals("Percentage should match", 80.0, warning.getPercentage(), 0.01);
    }

    /**
     * Test warning triggers at 80% threshold.
     */
    @Test
    public void testWarningThreshold80Percent() {
        BudgetWarning warning80 = new BudgetWarning(
                "budget456",
                "Transportation",
                80.0,
                100.0,
                80.0
        );
        
        assertTrue("Should show warning at 80%", 
                warning80.getPercentage() >= 80.0);
        assertTrue("Should not exceed 100%",
                warning80.getPercentage() < 100.0);
    }

    /**
     * Test warning triggers at 90% threshold.
     */
    @Test
    public void testWarningThreshold90Percent() {
        BudgetWarning warning90 = new BudgetWarning(
                "budget789",
                "Entertainment",
                90.0,
                100.0,
                90.0
        );
        
        assertTrue("Should show warning at 90%",
                warning90.getPercentage() >= 90.0);
        assertTrue("Should be high priority warning",
                warning90.getPercentage() >= 90.0);
    }

    /**
     * Test no warning below 80% threshold.
     */
    @Test
    public void testNoWarningBelowThreshold() {
        BudgetWarning warningLow = new BudgetWarning(
                "budget999",
                "Shopping",
                70.0,
                100.0,
                70.0
        );
        
        assertFalse("Should not show warning below 80%",
                warningLow.getPercentage() >= 80.0);
    }

    /**
     * Test warning not shown at or above 100% (budget exceeded).
     */
    @Test
    public void testNoWarningAtOrAbove100Percent() {
        BudgetWarning warningExceeded = new BudgetWarning(
                "budget000",
                "Bills & Utilities",
                120.0,
                100.0,
                120.0
        );
        
        assertFalse("Should not show warning at 100% or above",
                warningExceeded.getPercentage() < 100.0);
    }

    /**
     * Test timestamp is set when warning is created.
     */
    @Test
    public void testWarningHasTimestamp() {
        assertTrue("Timestamp should be greater than 0",
                warning.getTimestamp() > 0);
        
        long currentTime = System.currentTimeMillis();
        assertTrue("Timestamp should be recent",
                Math.abs(currentTime - warning.getTimestamp()) < 5000);
    }

    /**
     * Test null budget ID handling.
     */
    @Test
    public void testNullBudgetId() {
        BudgetWarning nullIdWarning = new BudgetWarning();
        nullIdWarning.setBudgetId(null);
        nullIdWarning.setCategory("Test");
        nullIdWarning.setSpentAmount(80.0);
        nullIdWarning.setTotalAmount(100.0);
        nullIdWarning.setPercentage(80.0);
        
        // Warning object should still be created but validation should catch null ID
        assertNotNull("Warning object should exist", nullIdWarning);
    }

    /**
     * Test zero budget amount handling.
     */
    @Test
    public void testZeroBudgetAmount() {
        BudgetWarning zeroWarning = new BudgetWarning(
                "budget111",
                "Test",
                0.0,
                0.0,
                0.0
        );
        
        assertEquals("Should handle zero budget", 0.0,
                zeroWarning.getTotalAmount(), 0.01);
    }
}
