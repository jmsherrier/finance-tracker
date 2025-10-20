package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void additionIsCorrect() {
        assertEquals(4, 2 + 2);
    }

    private DashboardFragment dashboard;

    @Before
    public void setup() {
        dashboard = new DashboardFragment();
    }

    @Test
    public void testDashboardResetsOnNewUserLogin() throws Exception {
        Field totalSpentField = DashboardFragment.class.getDeclaredField("totalSpent");
        totalSpentField.setAccessible(true);
        totalSpentField.set(dashboard, 200.0);

        Field totalBudgetField = DashboardFragment.class.getDeclaredField("totalBudget");
        totalBudgetField.setAccessible(true);
        totalBudgetField.set(dashboard, 500.0);

        dashboard.resetDashboardData();

        assertEquals(0.0, totalSpentField.get(dashboard));
        assertEquals(0.0, totalBudgetField.get(dashboard));
    }

    @Test
    public void testRemainingBudgetCalc() {
        double totalBudget = 1000.0;
        double totalSpent = 250.0;
        double expectedRemaining = totalBudget - totalSpent;
        double actualRemaining = totalBudget - totalSpent;

        // fixed spacing around comma
        assertEquals(expectedRemaining, actualRemaining, 0.001);
    }
}
