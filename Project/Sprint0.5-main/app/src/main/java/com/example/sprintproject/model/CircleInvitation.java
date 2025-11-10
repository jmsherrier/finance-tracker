package com.example.sprintproject.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Represents an invitation to join a savings circle.
 */
public class CircleInvitation {
    private String id;
    private String circleId;
    private String inviterEmail;
    private String inviterId;
    private String inviteeEmail;
    private String status; // "pending", "accepted", "declined"
    private Date createdAt;
    private Date expiresAt;

    /** Required empty constructor for Firestore. */
    public CircleInvitation() {
    }

    /**
     * Constructor for creating a new invitation.
     * @param circleId The circle ID
     * @param inviterEmail The inviter's email
     * @param inviterId The inviter's ID
     * @param inviteeEmail The invitee's email
     */
    public CircleInvitation(String circleId, String inviterEmail, String inviterId,
                           String inviteeEmail) {
        this.circleId = circleId;
        this.inviterEmail = inviterEmail;
        this.inviterId = inviterId;
        this.inviteeEmail = inviteeEmail;
        this.status = "pending";
        this.createdAt = new Date();
        
        // Set expiration to 7 days from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        this.expiresAt = cal.getTime();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getInviterEmail() {
        return inviterEmail;
    }

    public void setInviterEmail(String inviterEmail) {
        this.inviterEmail = inviterEmail;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Check if invitation has expired.
     * @return true if the invitation has expired, false otherwise
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return new Date().after(expiresAt);
    }

    /**
     * Accept the invitation.
     */
    public void accept() {
        this.status = "accepted";
    }

    /**
     * Decline the invitation.
     */
    public void decline() {
        this.status = "declined";
    }

    /**
     * Check if invitation is pending.
     * @return true if the invitation is pending, false otherwise
     */
    public boolean isPending() {
        return "pending".equals(status) && !isExpired();
    }

    @Override
    public String toString() {
        return "CircleInvitation{"
                + "id='" + id + '\''
                + ", circleId='" + circleId + '\''
                + ", inviteeEmail='" + inviteeEmail + '\''
                + ", status='" + status + '\''
                + '}';
    }
}

