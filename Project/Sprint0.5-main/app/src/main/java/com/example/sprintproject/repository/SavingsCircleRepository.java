package com.example.sprintproject.repository;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.model.CircleContribution;
import com.example.sprintproject.model.CircleInvitation;
import com.example.sprintproject.model.CircleMember;
import com.example.sprintproject.model.SavingsCircle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Singleton repository for managing Savings Circle data in Firestore.
 * All Firestore operations are mediated through this repository.
 */
public final class SavingsCircleRepository {
    private static SavingsCircleRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    /**
     * Callback interface for repository operations.
     *
     * @param <T> the type of result expected when the operation succeeds
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    private SavingsCircleRepository() {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();
        this.db = firestoreManager.getDb();
        this.auth = firestoreManager.getAuth();
    }

    /**
     * Get singleton instance.
     *
     * @return a instance of the class.
     */
    public static synchronized SavingsCircleRepository getInstance() {
        if (instance == null) {
            instance = new SavingsCircleRepository();
        }
        return instance;
    }

    /**
     * Get current user ID.
     * @return The current user ID or null if not logged in
     */
    private String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    /**
     * Get current user email.
     * @return The current user email or null if not logged in
     */
    private String getCurrentUserEmail() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getEmail();
        }
        return null;
    }

    // ==================== Circle CRUD Operations ====================

    /**
     * Create a new savings circle.
     * @param circle The circle to create
     * @param callback Callback for the operation result
     */
    public void createCircle(SavingsCircle circle,
                             RepositoryCallback<SavingsCircle> callback) {
        db.collection("savingsCircles")
                .add(circle)
                .addOnSuccessListener(documentReference -> {
                    circle.setId(documentReference.getId());

                    // Add creator as leader member
                    Date memberStartDate = calculateMemberStartDate(
                            circle.getFrequency(), circle.getStartDate());
                    Date memberEndDate = calculateMemberEndDate(
                            circle.getFrequency(), memberStartDate);

                    CircleMember creator = new CircleMember(
                            circle.getCreatorId(),
                            circle.getCreatorEmail(),
                            circle.getId(),
                            "leader",
                            memberStartDate,
                            memberEndDate
                    );

                    addMember(circle.getId(), creator, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            callback.onSuccess(circle);
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError(
                                "Circle created but failed to add creator: "
                                + error);
                        }
                    });
                })
                .addOnFailureListener(
                        e -> callback.onError("Error creating circle: " + e.getMessage()));
    }

    /**
     * Load all circles where the current user is a member.
     * Alternative approach: Query all circles and check membership
     * (avoids collectionGroup index requirement).
     *
     * @param userId   the ID of the current user whose circles to load
     * @param callback the callback to handle success or error results
     */
    public void loadUserCircles(String userId, RepositoryCallback<List<SavingsCircle>> callback) {
        // Query all savings circles
        db.collection("savingsCircles")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> circleIds = new ArrayList<>();

                    // Check each circle to see if user is a member
                    for (QueryDocumentSnapshot circleDoc : querySnapshot) {
                        String circleId = circleDoc.getId();
                        circleIds.add(circleId);
                    }

                    if (circleIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Check membership for each circle
                    List<SavingsCircle> userCircles = new ArrayList<>();
                    final int[] completed = {0};

                    for (String circleId : circleIds) {
                        // Check if user is a member of this circle
                        db.collection("savingsCircles")
                                .document(circleId)
                                .collection("members")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(memberDoc -> {
                                    if (memberDoc.exists()) {
                                        // User is a member, load the circle
                                        loadCircle(circleId,
                                            new RepositoryCallback<SavingsCircle>() {
                                                    @Override
                                                    public void onSuccess(SavingsCircle circle) {
                                                        userCircles.add(circle);
                                                        completed[0]++;
                                                        if (completed[0] == circleIds.size()) {
                                                            callback.onSuccess(userCircles);
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(String error) {
                                                        completed[0]++;
                                                        if (completed[0] == circleIds.size()) {
                                                            callback.onSuccess(userCircles);
                                                        }
                                                    }
                                                });
                                    } else {
                                        // User is not a member
                                        completed[0]++;
                                        if (completed[0] == circleIds.size()) {
                                            callback.onSuccess(userCircles);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    completed[0]++;
                                    if (completed[0] == circleIds.size()) {
                                        callback.onSuccess(userCircles);
                                    }
                                });
                    }
                })
                .addOnFailureListener(
                        e -> callback.onError("Error loading circles: " + e.getMessage()));
    }

    /**
     * Load a specific circle by ID.
     * @param circleId The circle ID
     * @param callback Callback for the operation result
     */
    public void loadCircle(String circleId,
                           RepositoryCallback<SavingsCircle> callback) {
        db.collection("savingsCircles")
                .document(circleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        SavingsCircle circle = documentSnapshot.toObject(SavingsCircle.class);
                        if (circle != null) {
                            circle.setId(documentSnapshot.getId());
                            callback.onSuccess(circle);
                        } else {
                            callback.onError("Failed to parse circle data");
                        }
                    } else {
                        callback.onError("Circle not found");
                    }
                })
                .addOnFailureListener(e ->
                    callback.onError("Error loading circle: " + e.getMessage()));
    }

    /**
     * Update a circle.
     * @param circle The circle to update
     * @param callback The callback to handle the result
     */
    public void updateCircle(SavingsCircle circle,
                             RepositoryCallback<Void> callback) {
        db.collection("savingsCircles")
                .document(circle.getId())
                .set(circle)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e ->
                    callback.onError("Error updating circle: " + e.getMessage()));
    }

    // ==================== Member Management ====================

    /**
     * Add a member to a circle.
     * @param circleId The circle ID
     * @param member The member to add
     * @param callback The callback to handle the result
     */
    public void addMember(String circleId, CircleMember member,
                          RepositoryCallback<Void> callback) {
        db.collection("savingsCircles")
                .document(circleId)
                .collection("members")
                .document(member.getUserId())
                .set(member)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e ->
                    callback.onError("Error adding member: " + e.getMessage()));
    }

    /**
     * Load all members of a circle.
     * @param circleId The circle ID
     * @param callback The callback to handle the result
     */
    public void loadCircleMembers(String circleId,
                                  RepositoryCallback<List<CircleMember>> callback) {
        db.collection("savingsCircles")
                .document(circleId)
                .collection("members")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CircleMember> members = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CircleMember member = doc.toObject(CircleMember.class);
                        if (member != null) {
                            members.add(member);
                        }
                    }
                    callback.onSuccess(members);
                })
                .addOnFailureListener(e ->
                    callback.onError("Error loading members: " + e.getMessage()));
    }

    /**
     * Update member's total contribution.
     * @param circleId The circle ID
     * @param userId The user ID
     * @param amount The new contribution amount
     * @param callback The callback to handle the result
     */
    public void updateMemberContribution(String circleId, String userId,
                                         double amount,
                                         RepositoryCallback<Void> callback) {
        DocumentReference memberRef = db.collection("savingsCircles")
                .document(circleId)
                .collection("members")
                .document(userId);

        memberRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CircleMember member = documentSnapshot.toObject(CircleMember.class);
                        if (member != null) {
                            member.setTotalContribution(member.getTotalContribution() + amount);
                            memberRef.set(member)
                                    .addOnSuccessListener(
                                        aVoid -> callback.onSuccess(null))
                                    .addOnFailureListener(e -> callback.onError(
                                        "Error updating contribution: "
                                        + e.getMessage()));
                        } else {
                            callback.onError("Member data not found");
                        }
                    } else {
                        callback.onError("Member not found");
                    }
                })
                .addOnFailureListener(e ->
                    callback.onError("Error loading member: " + e.getMessage()));
    }

    // ==================== Invitation Management ====================

    /**
     * Send an invitation to join a circle.
     * @param invitation The invitation to send
     * @param callback The callback to handle the result
     */
    public void sendInvitation(CircleInvitation invitation,
                               RepositoryCallback<Void> callback) {
        db.collection("circleInvitations")
                .add(invitation)
                .addOnSuccessListener(documentReference -> {
                    invitation.setId(documentReference.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e ->
                    callback.onError("Error sending invitation: " + e.getMessage()));
    }

    /**
     * Load pending invitations for the current user.
     * @param userEmail The user's email
     * @param callback The callback to handle the result
     */
    public void loadPendingInvitations(String userEmail,
                                       RepositoryCallback<List<CircleInvitation>> callback) {
        db.collection("circleInvitations")
                .whereEqualTo("inviteeEmail", userEmail)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CircleInvitation> invitations = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CircleInvitation invitation = doc.toObject(CircleInvitation.class);
                        if (invitation != null && !invitation.isExpired()) {
                            invitation.setId(doc.getId());
                            invitations.add(invitation);
                        }
                    }
                    callback.onSuccess(invitations);
                })
                .addOnFailureListener(e ->
                    callback.onError("Error loading invitations: " + e.getMessage()));
    }

    /**
     * Accept an invitation.
     * @param invitationId The invitation ID
     * @param userId The user ID
     * @param userEmail The user's email
     * @param acceptanceDate The date to use for acceptance (from dashboard date selector)
     * @param callback The callback to handle the result
     */
    public void acceptInvitation(String invitationId, String userId,
                                 String userEmail, Date acceptanceDate,
                                 RepositoryCallback<Void> callback) {
        db.collection("circleInvitations")
                .document(invitationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CircleInvitation invitation =
                            documentSnapshot.toObject(CircleInvitation.class);
                        if (invitation != null && invitation.isPending()) {
                            // Update invitation status
                            invitation.accept();
                            db.collection("circleInvitations")
                                    .document(invitationId)
                                    .set(invitation)
                                    .addOnSuccessListener(aVoid -> {
                                        // Load circle to get frequency and dates
                                        loadCircle(invitation.getCircleId(),
                                                new RepositoryCallback<SavingsCircle>() {
                                                @Override
                                                public void onSuccess(
                                                        SavingsCircle circle) {
                                                    // Calculate member dates using dashboard date selector
                                                    Date memberStartDate = calculateMemberStartDate(
                                                            circle.getFrequency(), acceptanceDate);
                                                    Date memberEndDate = calculateMemberEndDate(
                                                            circle.getFrequency(), memberStartDate);

                                                    // Add user as member
                                                    CircleMember member = new CircleMember(
                                                            userId,
                                                            userEmail,
                                                            circle.getId(),
                                                            "member",
                                                            memberStartDate,
                                                            memberEndDate
                                                    );

                                                    addMember(circle.getId(), member, callback);
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    callback.onError(
                                                        "Error loading circle: "
                                                        + error);
                                                }
                                            });
                                    })
                                    .addOnFailureListener(e -> callback.onError(
                                        "Error updating invitation: "
                                        + e.getMessage()));
                        } else {
                            callback.onError("Invitation is no longer valid");
                        }
                    } else {
                        callback.onError("Invitation not found");
                    }
                })
                .addOnFailureListener(e ->
                    callback.onError("Error loading invitation: " + e.getMessage()));
    }

    /**
     * Decline an invitation.
     * @param invitationId The invitation ID
     * @param callback The callback to handle the result
     */
    public void declineInvitation(String invitationId,
                                  RepositoryCallback<Void> callback) {
        db.collection("circleInvitations")
                .document(invitationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CircleInvitation invitation =
                            documentSnapshot.toObject(CircleInvitation.class);
                        if (invitation != null) {
                            invitation.decline();
                            db.collection("circleInvitations")
                                    .document(invitationId)
                                    .set(invitation)
                                    .addOnSuccessListener(
                                        aVoid -> callback.onSuccess(null))
                                    .addOnFailureListener(e -> callback.onError(
                                        "Error declining invitation: "
                                        + e.getMessage()));
                        } else {
                            callback.onError("Invalid invitation data");
                        }
                    } else {
                        callback.onError("Invitation not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(
                    "Error loading invitation: " + e.getMessage()));
    }

    // ==================== Contribution Management ====================

    /**
     * Add a contribution to a circle.
     * @param contribution The contribution to add
     * @param callback Callback for the operation result
     */
    public void addContribution(CircleContribution contribution,
                                RepositoryCallback<CircleContribution> callback) {
        db.collection("savingsCircles")
                .document(contribution.getCircleId())
                .collection("contributions")
                .add(contribution)
                .addOnSuccessListener(documentReference -> {
                    contribution.setId(documentReference.getId());

                    // Update member's total contribution
                    updateMemberContribution(contribution.getCircleId(),
                            contribution.getUserId(), contribution.getAmount(),
                            new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    // Check if goal is reached
                                    checkAndUpdateCompletion(contribution.getCircleId());
                                    callback.onSuccess(contribution);
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError(
                                        "Contribution added but failed to update member: "
                                        + error);
                                }
                            });
                })
                .addOnFailureListener(e -> callback.onError(
                    "Error adding contribution: " + e.getMessage()));
    }

    /**
     * Load all contributions for a circle.
     * @param circleId The ID of the circle
     * @param callback Callback for the operation result
     */
    public void loadCircleContributions(String circleId,
            RepositoryCallback<List<CircleContribution>> callback) {
        db.collection("savingsCircles")
                .document(circleId)
                .collection("contributions")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CircleContribution> contributions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CircleContribution contribution = doc.toObject(CircleContribution.class);
                        if (contribution != null) {
                            contribution.setId(doc.getId());
                            contributions.add(contribution);
                        }
                    }
                    callback.onSuccess(contributions);
                })
                .addOnFailureListener(e -> callback.onError(
                    "Error loading contributions: " + e.getMessage()));
    }

    /**
     * Calculate total progress for a circle.
     * @param circleId The ID of the circle
     * @param callback Callback for the operation result
     */
    public void calculateTotalProgress(String circleId,
                                       RepositoryCallback<Double> callback) {
        db.collection("savingsCircles")
                .document(circleId)
                .collection("contributions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double total = 0.0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CircleContribution contribution = doc.toObject(CircleContribution.class);
                        if (contribution != null) {
                            total += contribution.getAmount();
                        }
                    }
                    callback.onSuccess(total);
                })
                .addOnFailureListener(e -> callback.onError(
                    "Error calculating progress: " + e.getMessage()));
    }

    // ==================== Real-time Listeners ====================

    /**
     * Observe circle progress in real-time.
     * @param circleId The ID of the circle
     * @param callback Callback for the operation result
     * @return ListenerRegistration to manage the listener
     */
    public ListenerRegistration observeCircleProgress(String circleId,
            RepositoryCallback<Double> callback) {
        return db.collection("savingsCircles")
                .document(circleId)
                .collection("contributions")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    if (snapshot != null) {
                        double total = 0.0;
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            CircleContribution contribution =
                                doc.toObject(CircleContribution.class);
                            if (contribution != null) {
                                total += contribution.getAmount();
                            }
                        }
                        callback.onSuccess(total);
                    }
                });
    }

    /**
     * Observe circle members in real-time.
     * @param circleId The ID of the circle
     * @param callback Callback for the operation result
     * @return ListenerRegistration to manage the listener
     */
    public ListenerRegistration observeCircleMembers(String circleId,
            RepositoryCallback<List<CircleMember>> callback) {
        return db.collection("savingsCircles")
                .document(circleId)
                .collection("members")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error.getMessage());
                        return;
                    }

                    if (snapshot != null) {
                        List<CircleMember> members = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            CircleMember member = doc.toObject(CircleMember.class);
                            if (member != null) {
                                members.add(member);
                            }
                        }
                        callback.onSuccess(members);
                    }
                });
    }

    // ==================== Helper Methods ====================

    /**
     * Calculate member start date based on frequency and acceptance date.
     * Weekly: starts from acceptance date (Day 1 of 7)
     * Monthly: starts from Day 1 of current month
     * @param frequency The frequency of the circle
     * @param acceptanceDate The date the member accepted
     * @return The calculated start date
     */
    private Date calculateMemberStartDate(String frequency,
                                          Date acceptanceDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(acceptanceDate);

        if ("weekly".equalsIgnoreCase(frequency)) {
            // Start from acceptance date
            return acceptanceDate;
        } else if ("monthly".equalsIgnoreCase(frequency)) {
            // Start from first day of the month
            cal.set(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }

        return acceptanceDate;
    }

    /**
     * Calculate member end date based on frequency and start date.
     * @param frequency The frequency of the circle
     * @param startDate The start date
     * @return The calculated end date
     */
    private Date calculateMemberEndDate(String frequency,
                                        Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        if ("weekly".equalsIgnoreCase(frequency)) {
            cal.add(Calendar.DAY_OF_YEAR, 7);
        } else if ("monthly".equalsIgnoreCase(frequency)) {
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1); // Last day of month
        }

        return cal.getTime();
    }

    /**
     * Check and update circle completion status if goal is reached.
     */
    private void checkAndUpdateCompletion(String circleId) {
        loadCircle(circleId, new RepositoryCallback<SavingsCircle>() {
            @Override
            public void onSuccess(SavingsCircle circle) {
                calculateTotalProgress(circleId,
                        new RepositoryCallback<Double>() {
                        @Override
                        public void onSuccess(Double totalProgress) {
                            if (totalProgress >= circle.getGoalAmount()
                                && "active".equals(circle.getStatus())) {
                                circle.setStatus("completed");
                                updateCircle(circle, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // Goal completed
                                    }

                                    @Override
                                    public void onError(String error) {
                                        // Log error but don't fail
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            // Log error but don't fail
                        }
                    });
            }

            @Override
            public void onError(String error) {
                // Log error but don't fail
            }
        });
    }
}

