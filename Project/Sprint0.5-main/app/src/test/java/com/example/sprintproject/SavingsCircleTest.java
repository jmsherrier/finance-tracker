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
     * Test 6: SavingsCircle completion detection.
     */
    @Test
    public void testSavingsCircleCompletion() {
        SavingsCircle circle = new SavingsCircle(
                "Goal Group", "creator@test.com", "creator123",
                "Goal Challenge", 1000.0, "monthly", today, "Notes"
        );

        // Not complete at 500.0
        assertFalse(circle.isComplete(500.0));

        // Complete at goal
        assertTrue(circle.isComplete(1000.0));

        // Over-complete
        assertTrue(circle.isComplete(1500.0));
    }

    /**
     * Test 7: Changing goal amount affects progress calculation.
     */
    @Test
    public void testSavingsCircleGoalChangeAffectsProgress() {
        SavingsCircle circle = new SavingsCircle(
                "Mutable Goal Group", "creator@test.com", "creator123",
                "Change Goal Challenge", 1000.0, "weekly", today, "Notes"
        );

        // Initial: 500 / 1000 = 50%
        double progress50 = circle.calculateProgressPercentage(500.0);
        assertEquals(50.0, progress50, 0.01);

        // Change goal to 2000, now 500 / 2000 = 25%
        circle.setGoalAmount(2000.0);
        double progress25 = circle.calculateProgressPercentage(500.0);
        assertEquals(25.0, progress25, 0.01);
    }

    /**
     * Test 8: CircleMember activity period detection.
     */
    @Test
    public void testCircleMemberIsActive() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();

        // Active: yesterday to tomorrow
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date end = cal.getTime();

        CircleMember activeMember = new CircleMember(
                "u1", "active@test.com", "c1", "member", start, end
        );
        assertTrue("Member should be active", activeMember.isActive());

        // Not yet active: starts tomorrow
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date futureStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 5);
        Date futureEnd = cal.getTime();

        CircleMember futureMember = new CircleMember(
                "u2", "future@test.com", "c2", "member", futureStart, futureEnd
        );
        assertFalse("Member should not yet be active", futureMember.isActive());

        // Challenge ended: ended yesterday
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, -5);
        Date pastStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date pastEnd = cal.getTime();

        CircleMember pastMember = new CircleMember(
                "u3", "past@test.com", "c3", "member", pastStart, pastEnd
        );
        assertFalse("Member's challenge period is over", pastMember.isActive());
    }

    /**
     * Test 9: CircleMember leader detection, joinedAt, and toString.
     */
    @Test
    public void testCircleMemberLeaderAndToString() {
        CircleMember leader = new CircleMember(
                "lead1", "leader@test.com", "circle1", "leader", today, nextWeek
        );
        CircleMember member = new CircleMember(
                "mem1", "member@test.com", "circle1", "member", today, nextWeek
        );

        // Role checks
        assertTrue("Leader should return true for isLeader()", leader.isLeader());
        assertFalse("Member should return false for isLeader()", member.isLeader());

        // Basic field checks
        assertEquals("leader", leader.getRole());
        assertEquals("leader@test.com", leader.getEmail());
        assertNotNull("joinedAt should be initialized", leader.getJoinedAt());

        // toString contains key info
        String s = leader.toString();
        assertTrue(s.contains("lead1"));
        assertTrue(s.contains("leader@test.com"));
        assertTrue(s.contains("leader"));
    }

    /**
     * Test 10: CircleMember contribution percentage edge cases.
     */
    @Test
    public void testCircleMemberContributionEdgeCases() {
        CircleMember member = new CircleMember(
                "u4", "edge@test.com", "c4", "member", today, nextWeek
        );

        // Zero contribution, zero goal
        member.setTotalContribution(0.0);
        assertEquals(0.0, member.getContributionPercentage(0.0), 0.01);

        // Partial progress
        member.setTotalContribution(250.0);
        assertEquals(25.0, member.getContributionPercentage(1000.0), 0.01);

        // Over 100% progress
        member.setTotalContribution(1500.0);
        assertEquals(150.0, member.getContributionPercentage(1000.0), 0.01);
    }

    /**
     * Test 11: SavingsCircle isComplete method.
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
     * Test 12: SavingsCircle getDaysRemaining calculation.
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
     * Test 13: CircleMember isActive method.
     * This method is different
     * from the other testCircleMEmberIsActive
     * because it tests to see if the user is
     * within a
     * set amount range
     * of dates
     * that are valid
     * to be tested
     * by the app
     * and this unit
     * test.
     * It creates a new circle member
     * with specific rules
     * and asserts that
     * they are within
     * the active date range.
     * Creates also an inactive one that should
     * be considered non active.
     */
    @Test
    public void testCircleMemberIsActiveRange() {
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
     * Test 14: CircleInvitation isExpired method.
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
     * Test 15: Strategy pattern - SumContributionStrategy.
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
