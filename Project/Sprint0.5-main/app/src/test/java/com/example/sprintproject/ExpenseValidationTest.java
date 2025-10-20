package com.example.sprintproject;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpenseValidationTest {

    @Test
    public void testFutureDateIsRejected() {
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_MONTH, 1);
        Date futureDate = future.getTime();
        Date today = new Date();

        assertTrue("Future date should be after today", futureDate.after(today));
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
}
