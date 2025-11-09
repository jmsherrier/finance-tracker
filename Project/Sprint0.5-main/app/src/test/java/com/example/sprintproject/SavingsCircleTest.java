package com.example.sprintproject;

import com.example.sprintproject.factory.CircleFactory;
import com.example.sprintproject.model.CircleContribution;
import com.example.sprintproject.model.CircleInvitation;
import com.example.sprintproject.model.CircleMember;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.strategy.ProgressCalculationContext;
import com.example.sprintproject.strategy.SumContributionStrategy;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for Savings Circle functionality.
 */
public class SavingsCircleTest {

    private Date today;
    private Date nextWeek;

    @Before
    public void setUp() {
        today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        nextWeek = cal.getTime();
    }

    /**
     * Test 1: SavingsCircle progress calculation with valid amounts.
     */
    @Test
    public void testSavingsCircleProgressCalculation() {
        SavingsCircle circle = new SavingsCircle(
            "Test Group", "creator@test.com", "creator123",
            "Save $1000", 1000.0, "weekly", today, "Test notes"
        );
        
        // Test 50% progress
        double progress50 = circle.calculateProgressPercentage(500.0);
        assertEquals(50.0, progress50, 0.01);
        
        // Test 100% progress
        double progress100 = circle.calculateProgressPercentage(1000.0);
        assertEquals(100.0, progress100, 0.01);
        
        // Test zero goal amount
        circle.setGoalAmount(0);
        double progressZero = circle.calculateProgressPercentage(100.0);
        assertEquals(0.0, progressZero, 0.01);
    }

    /**
     * Test 2: SavingsCircle isActive method with different scenarios.
     */
    @Test
    public void testSavingsCircleIsActive() {
        // Create active circle
        SavingsCircle activeCircle = new SavingsCircle(
            "Active Group", "creator@test.com", "creator123",
            "Active Challenge", 500.0, "weekly", today, "Notes"
        );
        activeCircle.setStatus("active");
        assertTrue("Circle should be active", activeCircle.isActive());
        
        // Create completed circle
        SavingsCircle completedCircle = new SavingsCircle(
            "Completed Group", "creator@test.com", "creator123",
            "Completed Challenge", 500.0, "weekly", today, "Notes"
        );
        completedCircle.setStatus("completed");
        assertFalse("Completed circle should not be active", completedCircle.isActive());
    }

    /**
     * Test 3: CircleMember contribution percentage calculation.
     */
    @Test
    public void testCircleMemberContributionPercentage() {
        CircleMember member = new CircleMember(
            "user123", "member@test.com", "circle123", 
            "member", today, nextWeek
        );
        
        // Test 25% contribution
        member.setTotalContribution(250.0);
        double percentage25 = member.getContributionPercentage(1000.0);
        assertEquals(25.0, percentage25, 0.01);
        
        // Test 100% contribution
        member.setTotalContribution(1000.0);
        double percentage100 = member.getContributionPercentage(1000.0);
        assertEquals(100.0, percentage100, 0.01);
    }

    /**
     * Test 4: CircleInvitation accept and decline methods.
     */
    @Test
    public void testCircleInvitationAcceptDecline() {
        CircleInvitation invitation = new CircleInvitation(
            "circle123", "inviter@test.com", "inviter123", "invitee@test.com"
        );
        
        // Test initial status
        assertEquals("pending", invitation.getStatus());
        assertTrue("Invitation should be pending initially", invitation.isPending());
        
        // Test accept
        invitation.accept();
        assertEquals("accepted", invitation.getStatus());
        
        // Reset and test decline
        invitation.setStatus("pending");
        invitation.decline();
        assertEquals("declined", invitation.getStatus());
    }

    /**
     * Test 5: CircleFactory creates weekly and monthly circles correctly.
     */
    @Test
    public void testCircleFactoryCreation() {
        // Test weekly circle
        SavingsCircle weeklyCircle = CircleFactory.createWeeklyCircle(
            "Weekly Group", "creator@test.com", "creator123",
            "Weekly Challenge", 500.0, today, "Weekly notes"
        );
        
        assertNotNull("Weekly circle should not be null", weeklyCircle);
        assertEquals("weekly", weeklyCircle.getFrequency());
        assertEquals("Weekly Group", weeklyCircle.getGroupName());
        assertEquals("active", weeklyCircle.getStatus());
        
        // Test monthly circle
        SavingsCircle monthlyCircle = CircleFactory.createMonthlyCircle(
            "Monthly Group", "creator@test.com", "creator123",
            "Monthly Challenge", 2000.0, today, "Monthly notes"
        );
        
        assertNotNull("Monthly circle should not be null", monthlyCircle);
        assertEquals("monthly", monthlyCircle.getFrequency());
        assertEquals("Monthly Group", monthlyCircle.getGroupName());
        assertEquals("active", monthlyCircle.getStatus());
    }

    /**
     * Test 6: SavingsCircle isComplete method.
     */
    @Test
    public void testSavingsCircleIsComplete() {
        SavingsCircle circle = new SavingsCircle(
            "Test Group", "creator@test.com", "creator123",
            "Save $1000", 1000.0, "weekly", today, "Notes"
        );
        
        assertFalse("Circle should not be complete with 500/1000", 
            circle.isComplete(500.0));
        assertTrue("Circle should be complete with 1000/1000", 
            circle.isComplete(1000.0));
        assertTrue("Circle should be complete with 1500/1000", 
            circle.isComplete(1500.0));
        
        circle.setStatus("completed");
        assertTrue("Circle with completed status should be complete", 
            circle.isComplete(0.0));
    }

    /**
     * Test 7: SavingsCircle getDaysRemaining calculation.
     */
    @Test
    public void testSavingsCircleDaysRemaining() {
        SavingsCircle circle = new SavingsCircle(
            "Test Group", "creator@test.com", "creator123",
            "Weekly Challenge", 500.0, "weekly", today, "Notes"
        );
        
        long daysRemaining = circle.getDaysRemaining();
        assertTrue("Days remaining should be approximately 7", 
            daysRemaining >= 6 && daysRemaining <= 8);
        
        circle.setEndDate(null);
        assertEquals("Days remaining should be 0 with null end date", 
            0, circle.getDaysRemaining());
    }

    /**
     * Test 8: CircleMember isActive method.
     */
    @Test
    public void testCircleMemberIsActive() {
        CircleMember activeMember = new CircleMember(
            "user123", "member@test.com", "circle123",
            "member", today, nextWeek
        );
        assertTrue("Member should be active within date range", activeMember.isActive());
        
        CircleMember nullDateMember = new CircleMember(
            "user456", "member2@test.com", "circle123",
            "member", null, null
        );
        assertFalse("Member with null dates should not be active", nullDateMember.isActive());
    }

    /**
     * Test 9: CircleInvitation isExpired method.
     */
    @Test
    public void testCircleInvitationIsExpired() {
        CircleInvitation validInvitation = new CircleInvitation(
            "circle123", "inviter@test.com", "inviter123", "invitee@test.com"
        );
        assertFalse("Invitation should not be expired", validInvitation.isExpired());
        
        CircleInvitation expiredInvitation = new CircleInvitation(
            "circle123", "inviter@test.com", "inviter123", "invitee@test.com"
        );
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -8);
        expiredInvitation.setExpiresAt(cal.getTime());
        assertTrue("Invitation should be expired", expiredInvitation.isExpired());
    }

    /**
     * Test 10: Strategy pattern - SumContributionStrategy.
     */
    @Test
    public void testSumContributionStrategy() {
        List<CircleContribution> contributions = new ArrayList<>();
        
        CircleContribution c1 = new CircleContribution();
        c1.setAmount(100.0);
        contributions.add(c1);
        
        CircleContribution c2 = new CircleContribution();
        c2.setAmount(200.0);
        contributions.add(c2);
        
        ProgressCalculationContext context = new ProgressCalculationContext(
            new SumContributionStrategy()
        );
        
        double total = context.calculateProgress(contributions);
        assertEquals(300.0, total, 0.01);
        
        // Test with empty list
        double empty = context.calculateProgress(new ArrayList<>());
        assertEquals(0.0, empty, 0.01);
    }
}
