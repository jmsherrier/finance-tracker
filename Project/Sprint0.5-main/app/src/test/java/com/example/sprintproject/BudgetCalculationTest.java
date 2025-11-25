package com.example.sprintproject;

import com.example.sprintproject.model.Budget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

public class BudgetCalculationTest {

    // Constants for test data to avoid string literal duplication
    private static final String TEST_BUDGET_NAME = "Test Budget";
    private static final String CATEGORY_FOOD_DINING = "Food & Dining";
    private static final String FREQUENCY_MONTHLY = "monthly";
    private static final String TEST_USER = "testUser";

    @Test
    public void testBudgetUtilizationPercentage() {
        Budget budget = new Budget(TEST_BUDGET_NAME, 1000.0, CATEGORY_FOOD_DINING, 
                                   FREQUENCY_MONTHLY, new Date(), TEST_USER);
        budget.setSpentAmount(500.0);
        
        assertEquals("Utilization should be 50%", 50.0, 
                    budget.getUtilizationPercentage(), 0.01);
    }

    @Test
    public void testBudgetRemainingAmount() {
        Budget budget = new Budget(TEST_BUDGET_NAME, 1000.0, CATEGORY_FOOD_DINING, 
                                   FREQUENCY_MONTHLY, new Date(), TEST_USER);
        budget.setSpentAmount(300.0);
        
        assertEquals("Remaining should be 700", 700.0, 
                    budget.getRemainingAmount(), 0.01);
    }

    @Test
    public void testBudgetStatusGreen() {
        Budget budget = new Budget(TEST_BUDGET_NAME, 1000.0, CATEGORY_FOOD_DINING, 
                                   FREQUENCY_MONTHLY, new Date(), TEST_USER);
        budget.setSpentAmount(500.0);
        
        assertEquals("Status should be green", "green", 
                    budget.getStatusColor());
    }

    @Test
    public void testBudgetStatusYellow() {
        Budget budget = new Budget(TEST_BUDGET_NAME, 1000.0, CATEGORY_FOOD_DINING, 
                                   FREQUENCY_MONTHLY, new Date(), TEST_USER);
        budget.setSpentAmount(750.0);
        
        assertEquals("Status should be yellow", "yellow", 
                    budget.getStatusColor());
    }

    @Test
    public void testBudgetStatusRed() {
        Budget budget = new Budget(TEST_BUDGET_NAME, 1000.0, CATEGORY_FOOD_DINING, 
                                   FREQUENCY_MONTHLY, new Date(), TEST_USER);
        budget.setSpentAmount(950.0);
        
        assertEquals("Status should be red", "red", 
                    budget.getStatusColor());
    }
}
