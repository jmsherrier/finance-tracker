package com.example.sprintproject.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Budget;
import com.example.sprintproject.model.Expense;
import com.example.sprintproject.model.SavingsCircle;
import com.example.sprintproject.repository.SavingsCircleRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Unified adapter that displays both personal budgets and group-linked savings circles.
 * Group goals have a distinct visual indicator.
 */
public class UnifiedBudgetAdapter
        extends RecyclerView.Adapter<UnifiedBudgetAdapter.BudgetViewHolder> {

    public interface OnItemClickListener {
        void onBudgetClick(Budget budget);
        void onCircleClick(SavingsCircle circle);
    }

    private List<Budget> budgets;
    private List<SavingsCircle> circles;
    private List<Expense> expenses;
    private Map<String, Double> circleProgressMap; // Store progress for each circle
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat;
    private SavingsCircleRepository repository;

    public UnifiedBudgetAdapter(List<Budget> budgets, List<SavingsCircle> circles,
                               List<Expense> expenses, OnItemClickListener listener) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        this.circles = circles != null ? circles : new ArrayList<>();
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.circleProgressMap = new HashMap<>();
        this.repository = SavingsCircleRepository.getInstance();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        if (position < budgets.size()) {
            // It's a personal budget
            Budget budget = budgets.get(position);
            holder.bindBudget(budget);
        } else {
            // It's a savings circle (group goal)
            int circleIndex = position - budgets.size();
            SavingsCircle circle = circles.get(circleIndex);
            holder.bindCircle(circle);
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size() + circles.size();
    }

    public void updateBudgets(List<Budget> newBudgets) {
        this.budgets = newBudgets != null ? newBudgets : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateCircles(List<SavingsCircle> newCircles) {
        this.circles = newCircles != null ? newCircles : new ArrayList<>();
        notifyDataSetChanged();
        
        // Load progress for each circle
        for (SavingsCircle circle : this.circles) {
            loadCircleProgress(circle.getId());
        }
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses != null ? newExpenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Update progress for a specific circle.
     * @param circleId The circle ID to update
     * @param progress The new progress value
     */
    public void updateCircleProgress(String circleId, double progress) {
        circleProgressMap.put(circleId, progress);
        // Find and notify the specific item
        for (int i = 0; i < circles.size(); i++) {
            if (circles.get(i).getId().equals(circleId)) {
                notifyItemChanged(budgets.size() + i);
                break;
            }
        }
    }

    private void loadCircleProgress(String circleId) {
        repository.calculateTotalProgress(circleId, 
            new SavingsCircleRepository.RepositoryCallback<Double>() {
                @Override
                public void onSuccess(Double result) {
                    updateCircleProgress(circleId, result);
                }

                @Override
                public void onError(String error) {
                    updateCircleProgress(circleId, 0.0);
                }
            });
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView iconCategory;
        private TextView textTitle;
        private TextView textMeta;
        private TextView textAmount;
        private View statusDot;
        private LinearProgressIndicator progress;
        private TextView textGroupBadge; // For group indicator

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            iconCategory = itemView.findViewById(R.id.icon_category);
            textTitle = itemView.findViewById(R.id.text_title);
            textMeta = itemView.findViewById(R.id.text_meta);
            textAmount = itemView.findViewById(R.id.text_amount);
            statusDot = itemView.findViewById(R.id.status_dot);
            progress = itemView.findViewById(R.id.progress);
            textGroupBadge = itemView.findViewById(R.id.text_group_badge);
        }

        void bindBudget(Budget budget) {
            // Hide group indicator
            if (textGroupBadge != null) {
                textGroupBadge.setVisibility(View.GONE);
            }
            
            // Reset card background to default white
            cardView.setCardBackgroundColor(0xFFFFFFFF);
            
            // Reset icon color to default (green for budgets)
            iconCategory.setImageTintList(ColorStateList.valueOf(0xFF10B981));
            
            textTitle.setText(budget.getTitle());
            textMeta.setText(budget.getCategory() + " • "
                    + budget.getFrequency() + " • "
                    + dateFormat.format(budget.getStartDate()));
            textAmount.setText(String.format("$%.2f", budget.getTotalAmount()));

            double used = computeUsed(budget);
            int percent =
                    (int) Math.min(100, (used / Math.max(1e-9, budget.getTotalAmount())) * 100);
            progress.setProgress(percent);

            int color = getStatusColor(itemView, budget, used);
            statusDot.setBackgroundTintList(ColorStateList.valueOf(color));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBudgetClick(budget);
                }
            });
        }

        void bindCircle(SavingsCircle circle) {
            // Show group indicator badge
            if (textGroupBadge != null) {
                textGroupBadge.setVisibility(View.VISIBLE);
            }
            
            // Set distinct card background color for group goals (light blue tint)
            cardView.setCardBackgroundColor(0xFFF0F9FF);
            
            // Set distinct icon color for group goals (blue instead of green)
            iconCategory.setImageTintList(ColorStateList.valueOf(0xFF6366F1));
            
            // Show challenge title (not group name)
            textTitle.setText(circle.getChallengeTitle());
            
            // Meta info for circle
            String meta = circle.getGroupName() + " • " 
                + circle.getFrequency() + " • "
                + dateFormat.format(circle.getStartDate());
            textMeta.setText(meta);
            
            textAmount.setText(String.format("$%.2f", circle.getGoalAmount()));

            // Get progress for this circle
            Double progressValue = circleProgressMap.get(circle.getId());
            if (progressValue == null) {
                progressValue = 0.0;
            }
            
            double percentage = circle.calculateProgressPercentage(progressValue);
            int percent = (int) Math.min(100, percentage);
            progress.setProgress(percent);

            // Status color based on progress
            int color = getCircleStatusColor(itemView, circle, progressValue);
            statusDot.setBackgroundTintList(ColorStateList.valueOf(color));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCircleClick(circle);
                }
            });
        }

        private double computeUsed(Budget budget) {
            double total = 0;
            for (Expense e : expenses) {
                if (budget.getCategory().equalsIgnoreCase(e.getCategory())) {
                    Date d = e.getDate();
                    if (!d.after(windowEnd(budget)) && !d.before(windowStart(budget))) {
                        total += e.getAmount();
                    }
                }
            }
            return total;
        }

        private Date windowStart(Budget budget) {
            Calendar c = Calendar.getInstance();
            c.setTime(budget.getStartDate());
            return c.getTime();
        }

        private Date windowEnd(Budget budget) {
            Calendar c = Calendar.getInstance();
            c.setTime(budget.getStartDate());
            if ("weekly".equalsIgnoreCase(budget.getFrequency())) {
                c.add(Calendar.DAY_OF_YEAR, 7);
            } else {
                c.add(Calendar.MONTH, 1);
            }
            return c.getTime();
        }

        private int getStatusColor(View v, Budget budget, double used) {
            int green = v.getResources().getColor(android.R.color.holo_green_dark);
            int yellow = v.getResources().getColor(android.R.color.holo_orange_dark);
            int red = v.getResources().getColor(android.R.color.holo_red_dark);
            boolean past = new Date().after(windowEnd(budget));
            if (used >= budget.getTotalAmount()) {
                return green;
            }
            if (past && used < budget.getTotalAmount()) {
                return red;
            }
            return yellow;
        }

        private int getCircleStatusColor(View v, SavingsCircle circle, double progress) {
            int green = v.getResources().getColor(android.R.color.holo_green_dark);
            int yellow = v.getResources().getColor(android.R.color.holo_orange_dark);
            int blue = 0xFF6366F1; // Distinct color for group goals
            
            if (circle.isComplete(progress)) {
                return green;
            }
            if (progress >= circle.getGoalAmount() * 0.7) {
                return yellow;
            }
            return blue; // Default blue for group goals
        }
    }
}

