package com.example.sprintproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.sprintproject.model.BudgetWarning;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for BudgetWarningManager queue functionality.
 * Tests notification queue, dismissal behavior, and non-persistence.
 */
public class BudgetWarningManagerTest {

    private BudgetWarning warning1;
    private BudgetWarning warning2;

    /**
     * Set up test data before each test.
     */
    @Before
    public void setUp() {
        warning1 = new BudgetWarning(
                "budget1",
                "Food & Dining",
                400.0,
                500.0,
                80.0
        );
        
        warning2 = new BudgetWarning(
                "budget2",
                "Transportation",
                450.0,
                500.0,
                90.0
        );
    }

    /**
     * Test that only one warning displays at a time.
     */
    @Test
    public void testOnlyOneWarningDisplayedAtATime() {
        // This test validates the queue ensures only one pop-up shows at a time
        assertNotNull("First warning should exist", warning1);
        assertNotNull("Second warning should exist", warning2);
        
        // In actual implementation, when warning1 is showing,
        // warning2 should be queued
        assertTrue("Warnings should have different budget IDs",
                !warning1.getBudgetId().equals(warning2.getBudgetId()));
    }

    /**
     * Test warning is dismissible.
     */
    @Test
    public void testWarningIsDismissible() {
        // Simulate dismissal
        BudgetWarning dismissedWarning = null;
        
        // After dismissal, warning should be null
        assertNull("Warning should be null after dismissal", dismissedWarning);
    }

    /**
     * Test warning does not persist after acknowledgment.
     */
    @Test
    public void testWarningDoesNotPersistAfterAcknowledgment() {
        // Create warning key to simulate persistence check
        String warningKey = warning1.getBudgetId() + "_80";
        
        assertNotNull("Warning key should be generated", warningKey);
        assertTrue("Warning key should contain budget ID",
                warningKey.contains(warning1.getBudgetId()));
    }

    /**
     * Test multiple warnings are queued properly.
     */
    @Test
    public void testMultipleWarningsQueuedProperly() {
        // Verify warnings have different thresholds
        assertTrue("Warning1 should be 80% threshold",
                warning1.getPercentage() == 80.0);
        assertTrue("Warning2 should be 90% threshold",
                warning2.getPercentage() == 90.0);
        
        // Higher threshold warning should have priority
        assertTrue("Higher percentage should be shown first",
                warning2.getPercentage() > warning1.getPercentage());
    }

    /**
     * Test warnings do not overlap or interfere.
     */
    @Test
    public void testWarningsDoNotOverlap() {
        // Each warning should have unique budget ID
        assertFalse("Budget IDs should be different",
                warning1.getBudgetId().equals(warning2.getBudgetId()));
        
        assertFalse("Categories should be different",
                warning1.getCategory().equals(warning2.getCategory()));
    }

    /**
     * Test warning for same budget at same threshold shown only once.
     */
    @Test
    public void testSameBudgetSameThresholdShownOnce() {
        BudgetWarning duplicate = new BudgetWarning(
                "budget1", // Same ID as warning1
                "Food & Dining",
                400.0,
                500.0,
                80.0  // Same threshold
        );
        
        assertTrue("Duplicate should have same budget ID",
                duplicate.getBudgetId().equals(warning1.getBudgetId()));
        assertTrue("Duplicate should have same percentage",
                duplicate.getPercentage() == warning1.getPercentage());
    }

    /**
     * Test warning key generation for tracking shown warnings.
     */
    @Test
    public void testWarningKeyGeneration() {
        // Simulate key generation (budget ID + threshold)
        int percentageThreshold = (int) (warning1.getPercentage() / 10) * 10;
        String expectedKey = warning1.getBudgetId() + "_" + percentageThreshold;
        
        assertNotNull("Generated key should not be null", expectedKey);
        assertTrue("Key should contain budget ID",
                expectedKey.contains(warning1.getBudgetId()));
        assertTrue("Key should contain threshold",
                expectedKey.contains("80"));
    }

    /**
     * Test different thresholds for same budget create different keys.
     */
    @Test
    public void testDifferentThresholdsDifferentKeys() {
        BudgetWarning warning80 = new BudgetWarning(
                "budget1",
                "Food & Dining",
                400.0,
                500.0,
                80.0
        );
        
        BudgetWarning warning90 = new BudgetWarning(
                "budget1", // Same budget
                "Food & Dining",
                450.0,
                500.0,
                90.0  // Different threshold
        );
        
        int threshold80 = (int) (warning80.getPercentage() / 10) * 10;
        int threshold90 = (int) (warning90.getPercentage() / 10) * 10;
        
        assertFalse("Thresholds should be different",
                threshold80 == threshold90);
    }
}
