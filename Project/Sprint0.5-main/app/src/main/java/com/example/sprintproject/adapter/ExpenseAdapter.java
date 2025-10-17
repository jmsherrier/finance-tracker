package com.example.sprintproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.Expense;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private SimpleDateFormat dateFormat;

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconCategory;
        private TextView textExpenseName;
        private TextView textCategory;
        private TextView textDate;
        private TextView textAmount;
        private TextView textNotesIndicator;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCategory = itemView.findViewById(R.id.icon_category);
            textExpenseName = itemView.findViewById(R.id.text_expense_name);
            textCategory = itemView.findViewById(R.id.text_category);
            textDate = itemView.findViewById(R.id.text_date);
            textAmount = itemView.findViewById(R.id.text_amount);
            textNotesIndicator = itemView.findViewById(R.id.text_notes_indicator);
        }

        public void bind(Expense expense) {
            textExpenseName.setText(expense.getName());
            textCategory.setText(expense.getCategory());
            textDate.setText(dateFormat.format(expense.getDate()));
            textAmount.setText(String.format("$%.2f", expense.getAmount()));

            // Show notes indicator if notes exist
            if (expense.getNotes() != null && !expense.getNotes().trim().isEmpty()) {
                textNotesIndicator.setVisibility(View.VISIBLE);
            } else {
                textNotesIndicator.setVisibility(View.GONE);
            }

            // Set category icon based on category
            setCategoryIcon(expense.getCategory());
        }

        private void setCategoryIcon(String category) {
            int iconRes;
            switch (category.toLowerCase()) {
                case "food & dining":
                    iconRes = android.R.drawable.ic_menu_gallery;
                    break;
                case "transportation":
                    iconRes = android.R.drawable.ic_menu_directions;
                    break;
                case "shopping":
                    iconRes = android.R.drawable.ic_menu_myplaces;
                    break;
                case "entertainment":
                    iconRes = android.R.drawable.ic_menu_view;
                    break;
                case "bills & utilities":
                    iconRes = android.R.drawable.ic_menu_edit;
                    break;
                case "healthcare":
                    iconRes = android.R.drawable.ic_menu_help;
                    break;
                case "education":
                    iconRes = android.R.drawable.ic_menu_info_details;
                    break;
                case "travel":
                    iconRes = android.R.drawable.ic_menu_mapmode;
                    break;
                default:
                    iconRes = android.R.drawable.ic_menu_gallery;
                    break;
            }
            iconCategory.setImageResource(iconRes);
        }
    }
}
