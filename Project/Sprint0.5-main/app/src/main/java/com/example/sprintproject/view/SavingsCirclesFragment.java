package com.example.sprintproject.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.adapter.CircleAdapter;
import com.example.sprintproject.adapter.InvitationAdapter;
import com.example.sprintproject.model.CircleInvitation;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.viewmodel.SavingsCircleViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SavingsCirclesFragment extends Fragment {
    private SavingsCircleViewModel viewModel;
    private TimeViewModel timeViewModel;
    
    private RecyclerView recyclerViewCircles;
    private View emptyCard;
    private View invitationsSection;
    private TextView circleCount;
    private TextView textCirclesSectionTitle;
    private CircleAdapter circleAdapter;
    private InvitationAdapter invitationAdapter;

    public SavingsCirclesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_savings_circles, container, false);

        // Initialize ViewModels
        viewModel = new ViewModelProvider(requireActivity()).get(SavingsCircleViewModel.class);
        timeViewModel = new ViewModelProvider(requireActivity()).get(TimeViewModel.class);
        
        // Connect TimeViewModel to SavingsCircleViewModel for dashboard date selector
        viewModel.setTimeViewModel(timeViewModel);

        // Initialize UI components
        recyclerViewCircles = view.findViewById(R.id.recycler_circles);
        RecyclerView recyclerViewInvitations = view.findViewById(R.id.recycler_invitations);
        emptyCard = view.findViewById(R.id.empty_card);
        invitationsSection = view.findViewById(R.id.invitations_section);
        circleCount = view.findViewById(R.id.circle_count);
        textCirclesSectionTitle = view.findViewById(R.id.text_circles_section_title);
        FloatingActionButton fabCreateCircle = view.findViewById(R.id.fab_create_circle);

        // Setup Circles RecyclerView
        recyclerViewCircles.setLayoutManager(new LinearLayoutManager(getContext()));
        circleAdapter = new CircleAdapter(new ArrayList<>(), this::onCircleClick);
        recyclerViewCircles.setAdapter(circleAdapter);

        // Setup Invitations RecyclerView
        recyclerViewInvitations.setLayoutManager(new LinearLayoutManager(getContext()));
        invitationAdapter = new InvitationAdapter(new ArrayList<>(), 
            new InvitationAdapter.OnInvitationActionListener() {
                @Override
                public void onAccept(String invitationId) {
                    viewModel.acceptInvitation(invitationId);
                }

                @Override
                public void onDecline(String invitationId) {
                    viewModel.declineInvitation(invitationId);
                }
            });
        recyclerViewInvitations.setAdapter(invitationAdapter);

        // Setup FAB
        fabCreateCircle.setOnClickListener(v -> showCreateCircleDialog());

        // Observe LiveData
        observeData();

        // Load circles and pending invitations
        viewModel.loadUserCircles();
        viewModel.loadPendingInvitations();

        return view;
    }

    private void observeData() {
        // Observe circles list
        viewModel.getCircles().observe(getViewLifecycleOwner(), circles -> {
            if (circles != null) {
                updateCirclesList(circles);
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

        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Could show/hide progress indicator here
        });

        // Observe pending invitations
        viewModel.getPendingInvitations().observe(getViewLifecycleOwner(), this::updateInvitationsList);
    }

    private void updateInvitationsList(List<CircleInvitation> invitations) {
        if (invitations != null && !invitations.isEmpty()) {
            invitationsSection.setVisibility(View.VISIBLE);
            invitationAdapter.updateInvitations(invitations);
        } else {
            invitationsSection.setVisibility(View.GONE);
        }
    }

    private void updateCirclesList(List<SavingsCircle> circles) {
        if (circles == null || circles.isEmpty()) {
            recyclerViewCircles.setVisibility(View.GONE);
            textCirclesSectionTitle.setVisibility(View.GONE);
            emptyCard.setVisibility(View.VISIBLE);
            circleCount.setText("0 circles");
        } else {
            recyclerViewCircles.setVisibility(View.VISIBLE);
            textCirclesSectionTitle.setVisibility(View.VISIBLE);
            emptyCard.setVisibility(View.GONE);
            circleCount.setText(circles.size() + " circle" + (circles.size() == 1 ? "" : "s"));
            circleAdapter.updateCircles(circles);
            
            // Load progress for each circle
            for (SavingsCircle circle : circles) {
                loadCircleProgress(circle.getId());
            }
        }
    }

    private void loadCircleProgress(String circleId) {
        // Load progress for this circle through ViewModel
        viewModel.calculateCircleProgress(circleId,
            new com.example.sprintproject.repository
                .SavingsCircleRepository.RepositoryCallback<Double>() {
                @Override
                public void onSuccess(Double result) {
                    // Update adapter with progress
                    circleAdapter.updateProgress(circleId, result);
                }

                @Override
                public void onError(String error) {
                    // If error, just show 0 progress
                    circleAdapter.updateProgress(circleId, 0.0);
                }
            });
    }

    private void onCircleClick(SavingsCircle circle) {
        // Navigate to detail fragment
        CircleDetailFragment detailFragment = CircleDetailFragment.newInstance(circle.getId());
        
        FragmentTransaction transaction = requireActivity()
            .getSupportFragmentManager()
            .beginTransaction();
        transaction.replace(R.id.fragment_container, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showCreateCircleDialog() {
        CreateCircleDialog dialog = new CreateCircleDialog(
            getContext(),
            viewModel,
            timeViewModel,
            () -> viewModel.loadUserCircles()
        );
        dialog.show();
    }
}
