package com.example.sprintproject;

import com.example.sprintproject.model.Budget;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class BudgetCalculationTest {

    @Test
    public void testBudgetUtilizationPercentage() {
        Budget budget = new Budget("Test Budget", 1000.0, "Food & Dining", 
                                   "monthly", new Date(), "testUser");
        budget.setSpentAmount(500.0);
        
        assertEquals("Utilization should be 50%", 50.0, 
                    budget.getUtilizationPercentage(), 0.01);
    }

    @Test
    public void testBudgetRemainingAmount() {
        Budget budget = new Budget("Test Budget", 1000.0, "Food & Dining", 
                                   "monthly", new Date(), "testUser");
        budget.setSpentAmount(300.0);
        
        assertEquals("Remaining should be 700", 700.0, 
                    budget.getRemainingAmount(), 0.01);
    }

    @Test
    public void testBudgetStatusGreen() {
        Budget budget = new Budget("Test Budget", 1000.0, "Food & Dining", 
                                   "monthly", new Date(), "testUser");
        budget.setSpentAmount(500.0);
        
        assertEquals("Status should be green", "green", 
                    budget.getStatusColor());
    }

    @Test
    public void testBudgetStatusYellow() {
        Budget budget = new Budget("Test Budget", 1000.0, "Food & Dining", 
                                   "monthly", new Date(), "testUser");
        budget.setSpentAmount(750.0);
        
        assertEquals("Status should be yellow", "yellow", 
                    budget.getStatusColor());
    }

    @Test
    public void testBudgetStatusRed() {
        Budget budget = new Budget("Test Budget", 1000.0, "Food & Dining", 
                                   "monthly", new Date(), "testUser");
        budget.setSpentAmount(950.0);
        
        assertEquals("Status should be red", "red", 
                    budget.getStatusColor());
    }
}
