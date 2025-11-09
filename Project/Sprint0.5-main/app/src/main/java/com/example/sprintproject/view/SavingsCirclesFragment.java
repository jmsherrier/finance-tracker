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
    private TextView textEmpty;
    private FloatingActionButton fabCreateCircle;
    private CircleAdapter circleAdapter;

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

        // Initialize UI components
        recyclerViewCircles = view.findViewById(R.id.recycler_circles);
        textEmpty = view.findViewById(R.id.text_empty_circles);
        fabCreateCircle = view.findViewById(R.id.fab_create_circle);

        // Setup RecyclerView
        recyclerViewCircles.setLayoutManager(new LinearLayoutManager(getContext()));
        circleAdapter = new CircleAdapter(new ArrayList<>(), this::onCircleClick);
        recyclerViewCircles.setAdapter(circleAdapter);

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
        viewModel.getPendingInvitations().observe(getViewLifecycleOwner(), invitations -> {
            if (invitations != null && !invitations.isEmpty()) {
                // Show invitation notification or handle invitations
                // For now, just reload circles
                viewModel.loadUserCircles();
            }
        });
    }

    private void updateCirclesList(List<SavingsCircle> circles) {
        if (circles == null || circles.isEmpty()) {
            recyclerViewCircles.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCircles.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
            circleAdapter.updateCircles(circles);
            
            // Load progress for each circle
            for (SavingsCircle circle : circles) {
                loadCircleProgress(circle.getId());
            }
        }
    }

    private void loadCircleProgress(String circleId) {
        // This will be handled by the detail view, but we can preload here
        // For now, just update the adapter when progress changes
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
            () -> {
                // Refresh list after creation
                viewModel.loadUserCircles();
            }
        );
        dialog.show();
    }
}
