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
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays all budgets in a RecyclerView with utilization and color-coded status.
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    private List<Budget> budgets;
    private List<Expense> expenses;
    private OnBudgetClickListener listener;
    private SimpleDateFormat dateFormat;

    public BudgetAdapter(List<Budget> budgets, List<Expense> expenses,
                         OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.expenses = expenses;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
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
        Budget budget = budgets.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textMeta;
        private TextView textAmount;
        private View statusDot;
        private LinearProgressIndicator progress;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textMeta = itemView.findViewById(R.id.text_meta);
            textAmount = itemView.findViewById(R.id.text_amount);
            statusDot = itemView.findViewById(R.id.status_dot);
            progress = itemView.findViewById(R.id.progress);
        }

        void bind(Budget budget) {
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
    }
}
