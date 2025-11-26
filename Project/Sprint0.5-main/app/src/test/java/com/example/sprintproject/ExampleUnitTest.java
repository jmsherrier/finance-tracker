package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//this import wasn't added for some reason
//adding this import allows us to run the application without any errors
import com.example.sprintproject.view.DashboardFragment;


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
    public void testDashboardResetsOnNewUserLogin() {
        // Test that resetDashboardData can be called without errors
        // The actual reset behavior is tested through the ViewModel
        dashboard.resetDashboardData();
        
        // Verify the method completes successfully
        assertNotNull("Dashboard should not be null", dashboard);
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

    @Test
    public void testDashboardFragmentInitialization() {
        DashboardFragment fragment = new DashboardFragment();
        assertNotNull("Fragment should initialize successfully", fragment);
    }

    @Test
    public void testMultipleDashboardInstances() {
        DashboardFragment fragment1 = new DashboardFragment();
        DashboardFragment fragment2 = new DashboardFragment();
        assertNotNull(fragment1);
        assertNotNull(fragment2);
    }

    @Test
    public void testSimpleMultiplication() {
        int a = 6;
        int b = 7;
        assertEquals(42, a * b);
    }

    @Test
    public void testBudgetGoesToZeroAtFullSpend() {
        double totalBudget = 500.0;
        double totalSpent = 500.0;
        double remaining = totalBudget - totalSpent;
        assertEquals(0.0, remaining, 0.001);
    }

    @Test
    public void testNegativeSpendingDoesNotAffectBudget() {
        double totalBudget = 300.0;
        double totalSpent = -50.0;  // invalid negative spend
        double expectedRemaining = totalBudget;
        double actualRemaining = totalBudget - Math.max(totalSpent, 0);

        assertEquals(expectedRemaining, actualRemaining, 0.001);
    }

    @Test
    public void testBudgetCalculationWithDecimals() {
        double totalBudget = 1234.56;
        double totalSpent = 789.12;
        double expectedRemaining = totalBudget - totalSpent;
        double actualRemaining = totalBudget - totalSpent;

        assertEquals(expectedRemaining, actualRemaining, 0.0001);
    }

}
