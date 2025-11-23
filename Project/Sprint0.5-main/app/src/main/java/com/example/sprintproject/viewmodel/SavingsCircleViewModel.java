package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.FirestoreManager;
import com.example.sprintproject.factory.CircleFactory;
import com.example.sprintproject.model.CircleContribution;
import com.example.sprintproject.model.CircleInvitation;
import com.example.sprintproject.model.CircleMember;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.repository.SavingsCircleRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for Savings Circle functionality.
 * Mediates between Views and Repository - no direct Firestore access.
 */
public class SavingsCircleViewModel extends ViewModel {
    private static final String ERROR_USER_NOT_AUTHENTICATED = "User not authenticated";
    
    private final SavingsCircleRepository repository;
    private final FirebaseAuth auth;
    private TimeViewModel timeViewModel;

    // LiveData for UI observation
    private final MutableLiveData<List<SavingsCircle>> circlesLiveData = new MutableLiveData<>();
    private final MutableLiveData<SavingsCircle> currentCircleLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CircleMember>> membersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CircleInvitation>> pendingInvitationsLiveData =
            new MutableLiveData<>();
    private final MutableLiveData<List<CircleContribution>> contributionsLiveData =
            new MutableLiveData<>();
    private final MutableLiveData<Double> circleProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successMessageLiveData = new MutableLiveData<>();

    // Store listener registrations for cleanup
    private ListenerRegistration progressListener;
    private ListenerRegistration membersListener;

    public SavingsCircleViewModel() {
        this.repository = SavingsCircleRepository.getInstance();
        this.auth = FirestoreManager.getInstance().getAuth();
    }

    // Set the TimeViewModel to access dashboard date selector.
    public void setTimeViewModel(TimeViewModel timeViewModel) {
        this.timeViewModel = timeViewModel;
    }

    // ==================== LiveData Getters ====================

    public LiveData<List<SavingsCircle>> getCircles() {
        return circlesLiveData;
    }

    public LiveData<SavingsCircle> getCurrentCircle() {
        return currentCircleLiveData;
    }

    public LiveData<List<CircleMember>> getMembers() {
        return membersLiveData;
    }

    public LiveData<List<CircleInvitation>> getPendingInvitations() {
        return pendingInvitationsLiveData;
    }

    public LiveData<List<CircleContribution>> getContributions() {
        return contributionsLiveData;
    }

    public LiveData<Double> getCircleProgress() {
        return circleProgressLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessageLiveData;
    }

    // ==================== Circle Operations ====================

    public void createCircle(String groupName, String challengeTitle, double goalAmount,
                             String frequency, Date startDate, String notes) {
        if (!validateCircleCreation(groupName, challengeTitle, goalAmount, frequency)) {
            return;
        }

        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        String userEmail = getCurrentUserEmail();

        if (userId == null || userEmail == null) {
            errorLiveData.setValue(ERROR_USER_NOT_AUTHENTICATED);
            loadingLiveData.setValue(false);
            return;
        }

        // Use the new CircleFactory.CircleParams builder
        CircleFactory.CircleParams params = new CircleFactory.CircleParams()
                .setFrequency(frequency)
                .setGroupName(groupName)
                .setCreatorEmail(userEmail)
                .setCreatorId(userId)
                .setChallengeTitle(challengeTitle)
                .setGoalAmount(goalAmount)
                .setStartDate(startDate)
                .setNotes(notes);

        SavingsCircle circle = CircleFactory.createCircle(params);

        repository.createCircle(circle,
                new SavingsCircleRepository.RepositoryCallback<SavingsCircle>() {
                    @Override
                    public void onSuccess(SavingsCircle result) {
                        successMessageLiveData.setValue("Circle created successfully!");
                        loadUserCircles();
                        loadingLiveData.setValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                        loadingLiveData.setValue(false);
                    }
                });
    }

    public void loadUserCircles() {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();

        if (userId == null) {
            errorLiveData.setValue(ERROR_USER_NOT_AUTHENTICATED);
            loadingLiveData.setValue(false);
            return;
        }

        repository.loadUserCircles(userId,
                new SavingsCircleRepository.RepositoryCallback<List<SavingsCircle>>() {
                    @Override
                    public void onSuccess(List<SavingsCircle> result) {
                        circlesLiveData.setValue(result);
                        loadingLiveData.setValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                        circlesLiveData.setValue(new ArrayList<>());
                        loadingLiveData.setValue(false);
                    }
                });
    }

    public void loadCircleDetails(String circleId) {
        loadingLiveData.setValue(true);
        repository.loadCircle(circleId,
                new SavingsCircleRepository.RepositoryCallback<SavingsCircle>() {
                    @Override
                    public void onSuccess(SavingsCircle circle) {
                        currentCircleLiveData.setValue(circle);
                        loadCircleMembers(circleId);
                        loadCircleContributions(circleId);
                        startObservingProgress(circleId);
                        loadingLiveData.setValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                        loadingLiveData.setValue(false);
                    }
                });
    }

    // ==================== Member Operations ====================

    private void loadCircleMembers(String circleId) {
        repository.loadCircleMembers(circleId,
                new SavingsCircleRepository.RepositoryCallback<List<CircleMember>>() {
                    @Override
                    public void onSuccess(List<CircleMember> result) {
                        membersLiveData.setValue(result);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue("Error loading members: " + error);
                    }
                });
    }

    public boolean isUserLeader(String circleId) {
        SavingsCircle circle = currentCircleLiveData.getValue();
        if (circle == null || !circle.getId().equals(circleId)) {
            return false;
        }
        String userId = getCurrentUserId();
        return userId != null && userId.equals(circle.getCreatorId());
    }

    // ==================== Invitation Operations ====================

    public void sendInvitation(String circleId, String inviteeEmail) {
        if (!isUserLeader(circleId)) {
            errorLiveData.setValue("Only the leader can send invitations");
            return;
        }

        if (inviteeEmail == null || inviteeEmail.trim().isEmpty()) {
            errorLiveData.setValue("Please enter a valid email address");
            return;
        }

        loadingLiveData.setValue(true);
        String inviterId = getCurrentUserId();
        String inviterEmail = getCurrentUserEmail();

        if (inviterId == null || inviterEmail == null) {
            errorLiveData.setValue(ERROR_USER_NOT_AUTHENTICATED);
            loadingLiveData.setValue(false);
            return;
        }

        CircleInvitation invitation =
                new CircleInvitation(circleId, inviterEmail, inviterId, inviteeEmail.trim());

        repository.sendInvitation(invitation,
                new SavingsCircleRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        successMessageLiveData.setValue("Invitation sent successfully!");
                        loadingLiveData.setValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                        loadingLiveData.setValue(false);
                    }
                });
    }

    public void loadPendingInvitations() {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return;
        }

        repository.loadPendingInvitations(userEmail,
                new SavingsCircleRepository.RepositoryCallback<List<CircleInvitation>>() {
                    @Override
                    public void onSuccess(List<CircleInvitation> result) {
                        pendingInvitationsLiveData.setValue(result);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue("Error loading invitations: " + error);
                    }
                });
    }

    public void acceptInvitation(String invitationId) {
        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();
        String userEmail = getCurrentUserEmail();

        if (userId == null || userEmail == null) {
            errorLiveData.setValue(ERROR_USER_NOT_AUTHENTICATED);
            loadingLiveData.setValue(false);
            return;
        }

        Date acceptanceDate = new Date();
        if (timeViewModel != null && timeViewModel.getCurrentDate().getValue() != null) {
            acceptanceDate = timeViewModel.getCurrentDate().getValue();
        }

        repository.acceptInvitation(invitationId, userId, userEmail, acceptanceDate,
                new SavingsCircleRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        successMessageLiveData.setValue(
                                "Invitation accepted! You've joined the circle.");
                        loadPendingInvitations();
                        loadUserCircles();
                        loadingLiveData.setValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                        loadingLiveData.setValue(false);
                    }
                });
    }

    public void declineInvitation(String invitationId) {
        repository.declineInvitation(invitationId,
                new SavingsCircleRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadPendingInvitations();
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                    }
                });
    }

    // ==================== Contribution Operations ====================

    public void addContribution(String circleId, double amount, Date date, String notes) {
        if (amount <= 0) {
            errorLiveData.setValue("Contribution amount must be greater than 0");
            return;
        }

        loadingLiveData.setValue(true);
        String userId = getCurrentUserId();

        if (userId == null) {
            errorLiveData.setValue(ERROR_USER_NOT_AUTHENTICATED);
            loadingLiveData.setValue(false);
            return;
        }

        CircleContribution contribution =
                new CircleContribution(circleId, userId, amount, date, notes);

        repository.addContribution(contribution,
                new SavingsCircleRepository.RepositoryCallback<CircleContribution>() {
                    @Override
                    public void onSuccess(CircleContribution result) {
                        successMessageLiveData.setValue("Contribution added successfully!");
                        loadCircleContributions(circleId);
                        loadingLiveData.setValue(false);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue(error);
                        loadingLiveData.setValue(false);
                    }
                });
    }

    private void loadCircleContributions(String circleId) {
        repository.loadCircleContributions(circleId,
                new SavingsCircleRepository.RepositoryCallback<List<CircleContribution>>() {
                    @Override
                    public void onSuccess(List<CircleContribution> result) {
                        contributionsLiveData.setValue(result);
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue("Error loading contributions: " + error);
                    }
                });
    }

    // ==================== Real-time Observers ====================

    private void startObservingProgress(String circleId) {
        stopObservingProgress();

        progressListener = repository.observeCircleProgress(circleId,
                new SavingsCircleRepository.RepositoryCallback<Double>() {
                    @Override
                    public void onSuccess(Double result) {
                        circleProgressLiveData.setValue(result);
                        updateCircleStatusIfComplete(result);
                    }

                    private void updateCircleStatusIfComplete(Double result) {
                        SavingsCircle circle = currentCircleLiveData.getValue();
                        if (circle == null) {
                            return;
                        }
                        if (circle.isComplete(result) && !"completed".equals(circle.getStatus())) {
                            circle.setStatus("completed");
                            currentCircleLiveData.setValue(circle);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        errorLiveData.setValue("Error observing progress: " + error);
                    }
                });

        membersListener = repository.observeCircleMembers(circleId,
                new SavingsCircleRepository.RepositoryCallback<List<CircleMember>>() {
                    @Override
                    public void onSuccess(List<CircleMember> result) {
                        membersLiveData.setValue(result);
                    }

                    @Override
                    public void onError(String error) {
                        // Ignore minor listener errors
                    }
                });
    }

    public void stopObservingProgress() {
        if (progressListener != null) {
            progressListener.remove();
            progressListener = null;
        }
        if (membersListener != null) {
            membersListener.remove();
            membersListener = null;
        }
    }

    // ==================== Validation & Helpers ====================

    private boolean validateCircleCreation(String groupName, String challengeTitle,
                                           double goalAmount, String frequency) {
        if (groupName == null || groupName.trim().isEmpty()) {
            errorLiveData.setValue("Group name cannot be empty");
            return false;
        }
        if (challengeTitle == null || challengeTitle.trim().isEmpty()) {
            errorLiveData.setValue("Challenge title cannot be empty");
            return false;
        }
        if (goalAmount <= 0) {
            errorLiveData.setValue("Goal amount must be greater than 0");
            return false;
        }
        if (!"weekly".equalsIgnoreCase(frequency) && !"monthly".equalsIgnoreCase(frequency)) {
            errorLiveData.setValue("Frequency must be 'weekly' or 'monthly'");
            return false;
        }
        return true;
    }

    public void calculateCircleProgress(String circleId,
                                        SavingsCircleRepository.RepositoryCallback<Double>
                                                callback) {
        repository.calculateTotalProgress(circleId, callback);
    }

    private String getCurrentUserId() {
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
    }

    private String getCurrentUserEmail() {
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getEmail() : null;
    }

    public void clearMessages() {
        errorLiveData.setValue(null);
        successMessageLiveData.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopObservingProgress();
    }
}



