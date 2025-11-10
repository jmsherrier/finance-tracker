package com.example.sprintproject.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.adapter.MemberAdapter;
import com.example.sprintproject.model.CircleMember;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

/**
 * Fragment for displaying detailed view of a savings circle.
 */
public class CircleDetailFragment extends Fragment {

    private static final String ARG_CIRCLE_ID = "circle_id";

    private SavingsCircleViewModel viewModel;
    private String circleId;
    
    // UI Components
    private TextView textGroupName;
    private TextView textChallengeTitle;
    private TextView textGoalAmount;
    private TextView textCurrentProgress;
    private TextView textProgressPercentage;
    private TextView textDaysRemaining;
    private TextView textStatus;
    private LinearProgressIndicator progressBar;
    private RecyclerView recyclerViewMembers;
    private Button btnAddContribution;
    private Button btnInviteMember;
    private TextView textEmptyMembers;

    private MemberAdapter memberAdapter;

    public static CircleDetailFragment newInstance(String circleId) {
        CircleDetailFragment fragment = new CircleDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CIRCLE_ID, circleId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            circleId = getArguments().getString(ARG_CIRCLE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circle_detail, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(SavingsCircleViewModel.class);

        // Initialize UI components
        textGroupName = view.findViewById(R.id.text_group_name_detail);
        textChallengeTitle = view.findViewById(R.id.text_challenge_title_detail);
        textGoalAmount = view.findViewById(R.id.text_goal_amount_detail);
        textCurrentProgress = view.findViewById(R.id.text_current_progress);
        textProgressPercentage = view.findViewById(R.id.text_progress_percentage);
        textDaysRemaining = view.findViewById(R.id.text_days_remaining_detail);
        textStatus = view.findViewById(R.id.text_status_detail);
        progressBar = view.findViewById(R.id.progress_circle_detail);
        recyclerViewMembers = view.findViewById(R.id.recycler_members);
        btnAddContribution = view.findViewById(R.id.btn_add_contribution);
        btnInviteMember = view.findViewById(R.id.btn_invite_member);
        textEmptyMembers = view.findViewById(R.id.text_empty_members);

        // Setup RecyclerView
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new MemberAdapter(null, 0);
        recyclerViewMembers.setAdapter(memberAdapter);

        // Setup button listeners
        btnAddContribution.setOnClickListener(v -> {
            AddContributionDialog dialog = new AddContributionDialog(
                getContext(), viewModel, circleId);
            dialog.show();
        });

        btnInviteMember.setOnClickListener(v -> {
            if (viewModel.isUserLeader(circleId)) {
                InviteMemberDialog dialog = new InviteMemberDialog(
                    getContext(), viewModel, circleId);
                dialog.show();
            } else {
                Toast.makeText(getContext(), 
                    "Only the leader can send invitations", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Observe LiveData
        observeData();

        // Load circle details
        if (circleId != null) {
            viewModel.loadCircleDetails(circleId);
        }

        return view;
    }

    private void observeData() {
        // Observe current circle
        viewModel.getCurrentCircle().observe(getViewLifecycleOwner(), circle -> {
            if (circle != null && circle.getId().equals(circleId)) {
                updateCircleInfo(circle);
            }
        });

        // Observe members
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                updateMembersList(members);
            }
        });

        // Observe progress
        viewModel.getCircleProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                updateProgress(progress);
            }
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCircleInfo(SavingsCircle circle) {
        textGroupName.setText(circle.getGroupName());
        textChallengeTitle.setText(circle.getChallengeTitle());
        textGoalAmount.setText(String.format("Goal: $%.2f", circle.getGoalAmount()));

        // Update days remaining
        long daysRemaining = circle.getDaysRemaining();
        if (daysRemaining > 0) {
            textDaysRemaining.setText(daysRemaining + " days remaining");
        } else {
            textDaysRemaining.setText("Challenge ended");
        }

        // Update status
        if ("completed".equals(circle.getStatus())) {
            textStatus.setText("✓ Completed");
            textStatus.setVisibility(View.VISIBLE);
        } else if (circle.isActive()) {
            textStatus.setText("Active");
            textStatus.setVisibility(View.VISIBLE);
        } else {
            textStatus.setVisibility(View.GONE);
        }

        // Update member adapter goal amount
        memberAdapter.setGoalAmount(circle.getGoalAmount());

        // Show/hide invite button based on leadership
        if (viewModel.isUserLeader(circleId)) {
            btnInviteMember.setVisibility(View.VISIBLE);
        } else {
            btnInviteMember.setVisibility(View.GONE);
        }
    }

    private void updateProgress(double progress) {
        SavingsCircle circle = viewModel.getCurrentCircle().getValue();
        if (circle != null) {
            double percentage = circle.calculateProgressPercentage(progress);
            textCurrentProgress.setText(String.format("$%.2f", progress));
            textProgressPercentage.setText(String.format("%.1f%%", percentage));
            
            int progressInt = (int) Math.min(100, percentage);
            progressBar.setProgress(progressInt);
        }
    }

    private void updateMembersList(List<CircleMember> members) {
        if (members == null || members.isEmpty()) {
            recyclerViewMembers.setVisibility(View.GONE);
            textEmptyMembers.setVisibility(View.VISIBLE);
        } else {
            recyclerViewMembers.setVisibility(View.VISIBLE);
            textEmptyMembers.setVisibility(View.GONE);
            memberAdapter.updateMembers(members);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop observing when fragment is destroyed
        viewModel.stopObservingProgress();
    }
}

