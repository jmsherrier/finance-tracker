package com.example.sprintproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.SavingsCircle;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying savings circles in a RecyclerView.
 */
public class CircleAdapter extends RecyclerView.Adapter<CircleAdapter.CircleViewHolder> {

    public interface OnCircleClickListener {
        void onCircleClick(SavingsCircle circle);
    }

    private List<SavingsCircle> circles;
    private OnCircleClickListener listener;
    private Map<String, Double> circleProgressMap; // Store progress for each circle by ID

    public CircleAdapter(List<SavingsCircle> circles, OnCircleClickListener listener) {
        this.circles = circles;
        this.listener = listener;
        this.circleProgressMap = new HashMap<>();
    }

    @NonNull
    @Override
    public CircleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_circle, parent, false);
        return new CircleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CircleViewHolder holder, int position) {
        SavingsCircle circle = circles.get(position);
        holder.bind(circle);
    }

    @Override
    public int getItemCount() {
        return circles != null ? circles.size() : 0;
    }

    public void updateCircles(List<SavingsCircle> newCircles) {
        this.circles = newCircles;
        // Clear progress map when circles are updated - progress will be reloaded
        circleProgressMap.clear();
        notifyDataSetChanged();
    }

    /**
     * Update progress for a specific circle.
     * @param circleId The circle ID to update
     * @param progress The new progress value
     */
    public void updateProgress(String circleId, double progress) {
        circleProgressMap.put(circleId, progress);
        // Notify only the specific item that changed
        for (int i = 0; i < circles.size(); i++) {
            if (circles.get(i).getId().equals(circleId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    class CircleViewHolder extends RecyclerView.ViewHolder {
        private TextView textGroupName;
        private TextView textChallengeTitle;
        private TextView textGoalAmount;
        private TextView textDaysRemaining;
        private TextView textProgress;
        private LinearProgressIndicator progressBar;
        private TextView textStatus;

        CircleViewHolder(@NonNull View itemView) {
            super(itemView);
            textGroupName = itemView.findViewById(R.id.text_group_name);
            textChallengeTitle = itemView.findViewById(R.id.text_challenge_title);
            textGoalAmount = itemView.findViewById(R.id.text_goal_amount);
            textDaysRemaining = itemView.findViewById(R.id.text_days_remaining);
            textProgress = itemView.findViewById(R.id.text_progress);
            progressBar = itemView.findViewById(R.id.progress_circle);
            textStatus = itemView.findViewById(R.id.text_status);
        }

        void bind(SavingsCircle circle) {
            textGroupName.setText(circle.getGroupName());
            textChallengeTitle.setText(circle.getChallengeTitle());
            textGoalAmount.setText(String.format("Goal: $%.2f", circle.getGoalAmount()));

            // Get progress for this specific circle
            Double progress = circleProgressMap.get(circle.getId());
            if (progress == null) {
                progress = 0.0;
            }
            
            double percentage = circle.calculateProgressPercentage(progress);
            textProgress.setText(String.format("$%.2f / $%.2f (%.1f%%)", 
                progress, circle.getGoalAmount(), percentage));
            
            int progressInt = (int) Math.min(100, percentage);
            progressBar.setProgress(progressInt);

            // Display days remaining
            long daysRemaining = circle.getDaysRemaining();
            if (daysRemaining > 0) {
                textDaysRemaining.setText(daysRemaining + " days remaining");
            } else {
                textDaysRemaining.setText("Challenge ended");
            }

            // Display status
            if ("completed".equals(circle.getStatus())) {
                textStatus.setText("✓ Completed");
                textStatus.setVisibility(View.VISIBLE);
            } else if (circle.isActive()) {
                textStatus.setText("Active");
                textStatus.setVisibility(View.VISIBLE);
            } else {
                textStatus.setVisibility(View.GONE);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCircleClick(circle);
                }
            });
        }
    }
}

