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
                "Test Group", "creator@test.com", "creator123",
                "Save $1000", 1000.0, "weekly", today)
                .withNotes("Test notes");

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
                "Active Group", "creator@test.com", "creator123",
                "Active Challenge", 500.0, "weekly", today)
                .withNotes("Notes");
        activeCircle.setStatus("active");
        assertTrue("Circle should be active", activeCircle.isActive());

        SavingsCircle completedCircle = new SavingsCircle(
                "Completed Group", "creator@test.com", "creator123",
                "Completed Challenge", 500.0, "weekly", today)
                .withNotes("Notes");
        completedCircle.setStatus("completed");
        assertFalse("Completed circle should not be active", completedCircle.isActive());
    }

    /** Test 3: CircleMember contribution percentage calculation. */
    @Test
    public void testCircleMemberContributionPercentage() {
        CircleMember member = new CircleMember(
                "user123", "member@test.com", "circle123",
                "member", today, nextWeek);

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
                "circle123", "inviter@test.com", "inviter123", "invitee@test.com");

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
                        "Weekly Group", "creator@test.com", "creator123",
                        "Weekly Challenge", 500.0, today)
                .withNotes("Weekly notes");

        assertNotNull(weeklyCircle);
        assertEquals("weekly", weeklyCircle.getFrequency());
        assertEquals("Weekly Group", weeklyCircle.getGroupName());
        assertEquals("active", weeklyCircle.getStatus());

        SavingsCircle monthlyCircle = CircleFactory.createMonthlyCircle(
                        "Monthly Group", "creator@test.com", "creator123",
                        "Monthly Challenge", 2000.0, today)
                .withNotes("Monthly notes");

        assertNotNull(monthlyCircle);
        assertEquals("monthly", monthlyCircle.getFrequency());
        assertEquals("Monthly Group", monthlyCircle.getGroupName());
        assertEquals("active", monthlyCircle.getStatus());
    }

    /** Test 6: SavingsCircle completion detection. */
    @Test
    public void testSavingsCircleCompletion() {
        SavingsCircle circle = new SavingsCircle(
                "Goal Group", "creator@test.com", "creator123",
                "Goal Challenge", 1000.0, "monthly", today)
                .withNotes("Notes");

        assertFalse(circle.isComplete(500.0));
        assertTrue(circle.isComplete(1000.0));
        assertTrue(circle.isComplete(1500.0));
    }

    /** Test 7: Changing goal amount affects progress calculation. */
    @Test
    public void testSavingsCircleGoalChangeAffectsProgress() {
        SavingsCircle circle = new SavingsCircle(
                "Mutable Goal Group", "creator@test.com", "creator123",
                "Change Goal Challenge", 1000.0, "weekly", today)
                .withNotes("Notes");

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
                "u1", "active@test.com", "c1", "member", start, end);
        assertTrue(activeMember.isActive());

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date futureStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 5);
        Date futureEnd = cal.getTime();

        CircleMember futureMember = new CircleMember(
                "u2", "future@test.com", "c2", "member", futureStart, futureEnd);
        assertFalse(futureMember.isActive());

        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR, -5);
        Date pastStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date pastEnd = cal.getTime();

        CircleMember pastMember = new CircleMember(
                "u3", "past@test.com", "c3", "member", pastStart, pastEnd);
        assertFalse(pastMember.isActive());
    }

    /** Test 9: CircleMember leader detection, joinedAt, and toString. */
    @Test
    public void testCircleMemberLeaderAndToString() {
        CircleMember leader = new CircleMember(
                "lead1", "leader@test.com", "circle1", "leader", today, nextWeek);
        CircleMember member = new CircleMember(
                "mem1", "member@test.com", "circle1", "member", today, nextWeek);

        assertTrue(leader.isLeader());
        assertFalse(member.isLeader());
        assertEquals("leader", leader.getRole());
        assertEquals("leader@test.com", leader.getEmail());
        assertNotNull(leader.getJoinedAt());

        String s = leader.toString();
        assertTrue(s.contains("lead1"));
        assertTrue(s.contains("leader@test.com"));
        assertTrue(s.contains("leader"));
    }

    /** Test 10: CircleMember contribution percentage edge cases. */
    @Test
    public void testCircleMemberContributionEdgeCases() {
        CircleMember member = new CircleMember(
                "u4", "edge@test.com", "c4", "member", today, nextWeek);

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
                "Test Group", "creator@test.com", "creator123",
                "Save $1000", 1000.0, "weekly", today)
                .withNotes("Notes");

        assertFalse(circle.isComplete(500.0));
        assertTrue(circle.isComplete(1000.0));
        assertTrue(circle.isComplete(1500.0));

        circle.setStatus("completed");
        assertTrue(circle.isComplete(0.0));
    }

    /** Test 12: SavingsCircle getDaysRemaining calculation. */
    @Test
    public void testSavingsCircleDaysRemaining() {
        SavingsCircle circle = new SavingsCircle(
                "Test Group", "creator@test.com", "creator123",
                "Weekly Challenge", 500.0, "weekly", today)
                .withNotes("Notes");

        long daysRemaining = circle.getDaysRemaining();
        assertTrue(daysRemaining >= 6 && daysRemaining <= 8);

        circle.setEndDate(null);
        assertEquals(0, circle.getDaysRemaining());
    }

    /** Test 13: CircleMember isActive range validation. */
    @Test
    public void testCircleMemberIsActiveRange() {
        CircleMember activeMember = new CircleMember(
                "user123", "member@test.com", "circle123",
                "member", today, nextWeek);
        assertTrue(activeMember.isActive());

        CircleMember nullDateMember = new CircleMember(
                "user456", "member2@test.com", "circle123",
                "member", null, null);
        assertFalse(nullDateMember.isActive());
    }

    /** Test 14: CircleInvitation isExpired method. */
    @Test
    public void testCircleInvitationIsExpired() {
        CircleInvitation validInvitation = new CircleInvitation(
                "circle123", "inviter@test.com", "inviter123", "invitee@test.com");
        assertFalse(validInvitation.isExpired());

        CircleInvitation expiredInvitation = new CircleInvitation(
                "circle123", "inviter@test.com", "inviter123", "invitee@test.com");
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
