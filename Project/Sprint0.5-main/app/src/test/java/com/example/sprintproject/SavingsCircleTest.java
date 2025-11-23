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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for Savings Circle functionality.
 */
public class SavingsCircleTest {

    // Constants for test data to avoid string literal duplication
    private static final String STATUS_ACTIVE = "active";
    private static final String ROLE_LEADER = "leader";
    private static final String ROLE_MEMBER = "member";
    private static final String TEST_CIRCLE_ID = "circle123";
    private static final String TEST_MEMBER_EMAIL = "member@test.com";
    private static final String TEST_INVITER_EMAIL = "inviter@test.com";
    private static final String TEST_INVITEE_EMAIL = "invitee@test.com";
    private static final String TEST_INVITER_ID = "inviter123";
    private static final String TEST_CREATOR_EMAIL = TEST_CREATOR_EMAIL;
    private static final String TEST_CREATOR_ID = TEST_CREATOR_ID;
    private static final String TEST_GROUP_NAME = TEST_GROUP_NAME;
    private static final String FREQUENCY_WEEKLY = FREQUENCY_WEEKLY;
    private static final String TEST_NOTES = TEST_NOTES;

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

    /** Test 1: SavingsCircle progress calculation with valid amounts. */
    @Test
    public void testSavingsCircleProgressCalculation() {
        SavingsCircle circle = new SavingsCircle(
            TEST_GROUP_NAME, TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Save $1000", 1000.0, FREQUENCY_WEEKLY, today, "Test notes"
        );
        
        // Test 50% progress
        double progress50 = circle.calculateProgressPercentage(500.0);
        assertEquals(50.0, progress50, 0.01);

        double progress100 = circle.calculateProgressPercentage(1000.0);
        assertEquals(100.0, progress100, 0.01);

        circle.setGoalAmount(0);
        double progressZero = circle.calculateProgressPercentage(100.0);
        assertEquals(0.0, progressZero, 0.01);
    }

    /** Test 2: SavingsCircle isActive method with different scenarios. */
    @Test
    public void testSavingsCircleIsActive() {
        SavingsCircle activeCircle = new SavingsCircle(
            "Active Group", TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Active Challenge", 500.0, FREQUENCY_WEEKLY, today, TEST_NOTES
        );
        activeCircle.setStatus(STATUS_ACTIVE);
        assertTrue("Circle should be active", activeCircle.isActive());

        SavingsCircle completedCircle = new SavingsCircle(
            "Completed Group", TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Completed Challenge", 500.0, FREQUENCY_WEEKLY, today, TEST_NOTES
        );
        completedCircle.setStatus("completed");
        assertFalse("Completed circle should not be active", completedCircle.isActive());
    }

    /** Test 3: CircleMember contribution percentage calculation. */
    @Test
    public void testCircleMemberContributionPercentage() {
        CircleMember member = new CircleMember(
            "user123", TEST_MEMBER_EMAIL, TEST_CIRCLE_ID, 
            ROLE_MEMBER, today, nextWeek
        );
        
        // Test 25% contribution
        member.setTotalContribution(250.0);
        double percentage25 = member.getContributionPercentage(1000.0);
        assertEquals(25.0, percentage25, 0.01);

        member.setTotalContribution(1000.0);
        double percentage100 = member.getContributionPercentage(1000.0);
        assertEquals(100.0, percentage100, 0.01);
    }

    /** Test 4: CircleInvitation accept and decline methods. */
    @Test
    public void testCircleInvitationAcceptDecline() {
        CircleInvitation invitation = new CircleInvitation(
            TEST_CIRCLE_ID, TEST_INVITER_EMAIL, TEST_INVITER_ID, TEST_INVITEE_EMAIL
        );
        
        // Test initial status
        assertEquals("pending", invitation.getStatus());
        assertTrue(invitation.isPending());

        invitation.accept();
        assertEquals("accepted", invitation.getStatus());

        invitation.setStatus("pending");
        invitation.decline();
        assertEquals("declined", invitation.getStatus());
    }

    /** Test 5: CircleFactory creates weekly and monthly circles correctly. */
    @Test
    public void testCircleFactoryCreation() {
        SavingsCircle weeklyCircle = CircleFactory.createWeeklyCircle(
            "Weekly Group", TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Weekly Challenge", 500.0, today, "Weekly notes"
        );
        
        assertNotNull("Weekly circle should not be null", weeklyCircle);
        assertEquals(FREQUENCY_WEEKLY, weeklyCircle.getFrequency());
        assertEquals("Weekly Group", weeklyCircle.getGroupName());
        assertEquals(STATUS_ACTIVE, weeklyCircle.getStatus());
        
        // Test monthly circle
        SavingsCircle monthlyCircle = CircleFactory.createMonthlyCircle(
            "Monthly Group", TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Monthly Challenge", 2000.0, today, "Monthly notes"
        );
        
        assertNotNull("Monthly circle should not be null", monthlyCircle);
        assertEquals("monthly", monthlyCircle.getFrequency());
        assertEquals("Monthly Group", monthlyCircle.getGroupName());
        assertEquals(STATUS_ACTIVE, monthlyCircle.getStatus());
    }

    /** Test 6: SavingsCircle completion detection. */
    @Test
    public void testSavingsCircleCompletion() {
        SavingsCircle circle = new SavingsCircle(
                "Goal Group", TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
                "Goal Challenge", 1000.0, "monthly", today, TEST_NOTES
        );

        assertFalse(circle.isComplete(500.0));
        assertTrue(circle.isComplete(1000.0));
        assertTrue(circle.isComplete(1500.0));
    }

    /** Test 7: Changing goal amount affects progress calculation. */
    @Test
    public void testSavingsCircleGoalChangeAffectsProgress() {
        SavingsCircle circle = new SavingsCircle(
                "Mutable Goal Group", TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
                "Change Goal Challenge", 1000.0, FREQUENCY_WEEKLY, today, TEST_NOTES
        );

        double progress50 = circle.calculateProgressPercentage(500.0);
        assertEquals(50.0, progress50, 0.01);

        circle.setGoalAmount(2000.0);
        double progress25 = circle.calculateProgressPercentage(500.0);
        assertEquals(25.0, progress25, 0.01);
    }

    /** Test 8: CircleMember activity period detection. */
    @Test
    public void testCircleMemberIsActive() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date start = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date end = cal.getTime();

        CircleMember activeMember = new CircleMember(
                "u1", "active@test.com", "c1", ROLE_MEMBER, start, end
        );
        assertTrue("Member should be active", activeMember.isActive());

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date futureStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 5);
        Date futureEnd = cal.getTime();

        CircleMember futureMember = new CircleMember(
                "u2", "future@test.com", "c2", ROLE_MEMBER, futureStart, futureEnd
        );
        assertFalse("Member should not yet be active", futureMember.isActive());

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, -5);
        Date pastStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date pastEnd = cal.getTime();

        CircleMember pastMember = new CircleMember(
                "u3", "past@test.com", "c3", ROLE_MEMBER, pastStart, pastEnd
        );
        assertFalse("Member's challenge period is over", pastMember.isActive());
    }

    /** Test 9: CircleMember leader detection, joinedAt, and toString. */
    @Test
    public void testCircleMemberLeaderAndToString() {
        CircleMember leader = new CircleMember(
                "lead1", "leader@test.com", "circle1", ROLE_LEADER, today, nextWeek
        );
        CircleMember member = new CircleMember(
                "mem1", TEST_MEMBER_EMAIL, "circle1", ROLE_MEMBER, today, nextWeek
        );

        // Role checks
        assertTrue("Leader should return true for isLeader()", leader.isLeader());
        assertFalse("Member should return false for isLeader()", member.isLeader());

        // Basic field checks
        assertEquals(ROLE_LEADER, leader.getRole());
        assertEquals("leader@test.com", leader.getEmail());
        assertNotNull(leader.getJoinedAt());

        String s = leader.toString();
        assertTrue(s.contains("lead1"));
        assertTrue(s.contains("leader@test.com"));
        assertTrue(s.contains(ROLE_LEADER));
    }

    /** Test 10: CircleMember contribution percentage edge cases. */
    @Test
    public void testCircleMemberContributionEdgeCases() {
        CircleMember member = new CircleMember(
                "u4", "edge@test.com", "c4", ROLE_MEMBER, today, nextWeek
        );

        member.setTotalContribution(0.0);
        assertEquals(0.0, member.getContributionPercentage(0.0), 0.01);

        member.setTotalContribution(250.0);
        assertEquals(25.0, member.getContributionPercentage(1000.0), 0.01);

        member.setTotalContribution(1500.0);
        assertEquals(150.0, member.getContributionPercentage(1000.0), 0.01);
    }

    /** Test 11: SavingsCircle isComplete method. */
    @Test
    public void testSavingsCircleIsComplete() {
        SavingsCircle circle = new SavingsCircle(
            TEST_GROUP_NAME, TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Save $1000", 1000.0, FREQUENCY_WEEKLY, today, TEST_NOTES
        );
        
        assertFalse("Circle should not be complete with 500/1000", 
            circle.isComplete(500.0));
        assertTrue("Circle should be complete with 1000/1000", 
            circle.isComplete(1000.0));
        assertTrue("Circle should be complete with 1500/1000", 
            circle.isComplete(1500.0));
        
        circle.setStatus("completed");
        assertTrue(circle.isComplete(0.0));
    }

    /** Test 12: SavingsCircle getDaysRemaining calculation. */
    @Test
    public void testSavingsCircleDaysRemaining() {
        SavingsCircle circle = new SavingsCircle(
            TEST_GROUP_NAME, TEST_CREATOR_EMAIL, TEST_CREATOR_ID,
            "Weekly Challenge", 500.0, FREQUENCY_WEEKLY, today, TEST_NOTES
        );
        
        long daysRemaining = circle.getDaysRemaining();
        assertTrue(daysRemaining >= 6 && daysRemaining <= 8);

        circle.setEndDate(null);
        assertEquals(0, circle.getDaysRemaining());
    }

    /** Test 13: CircleMember isActive range validation. */
    @Test
    public void testCircleMemberIsActiveRange() {
        CircleMember activeMember = new CircleMember(
            "user123", TEST_MEMBER_EMAIL, TEST_CIRCLE_ID,
            ROLE_MEMBER, today, nextWeek
        );
        assertTrue("Member should be active within date range", activeMember.isActive());
        
        CircleMember nullDateMember = new CircleMember(
            "user456", "member2@test.com", TEST_CIRCLE_ID,
            ROLE_MEMBER, null, null
        );
        assertFalse("Member with null dates should not be active", nullDateMember.isActive());
    }

    /** Test 14: CircleInvitation isExpired method. */
    @Test
    public void testCircleInvitationIsExpired() {
        CircleInvitation validInvitation = new CircleInvitation(
            TEST_CIRCLE_ID, TEST_INVITER_EMAIL, TEST_INVITER_ID, TEST_INVITEE_EMAIL
        );
        assertFalse("Invitation should not be expired", validInvitation.isExpired());
        
        CircleInvitation expiredInvitation = new CircleInvitation(
            TEST_CIRCLE_ID, TEST_INVITER_EMAIL, TEST_INVITER_ID, TEST_INVITEE_EMAIL
        );
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -8);
        expiredInvitation.setExpiresAt(cal.getTime());
        assertTrue(expiredInvitation.isExpired());
    }

    /** Test 15: Strategy pattern - SumContributionStrategy. */
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
                new SumContributionStrategy());

        double total = context.calculateProgress(contributions);
        assertEquals(300.0, total, 0.01);

        double empty = context.calculateProgress(new ArrayList<>());
        assertEquals(0.0, empty, 0.01);
    }
}
