package com.example.sprintproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.CircleMember;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying circle members in a RecyclerView.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<CircleMember> members;
    private double goalAmount; // Goal amount for calculating percentages
    private SimpleDateFormat dateFormat;

    public MemberAdapter(List<CircleMember> members, double goalAmount) {
        this.members = members;
        this.goalAmount = goalAmount;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        CircleMember member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members != null ? members.size() : 0;
    }

    public void updateMembers(List<CircleMember> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
        notifyDataSetChanged();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView textMemberEmail;
        private TextView textRole;
        private TextView textContribution;
        private TextView textDateRange;
        private TextView textStatus;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            textMemberEmail = itemView.findViewById(R.id.text_member_email);
            textRole = itemView.findViewById(R.id.text_role);
            textContribution = itemView.findViewById(R.id.text_contribution);
            textDateRange = itemView.findViewById(R.id.text_date_range);
            textStatus = itemView.findViewById(R.id.text_member_status);
        }

        void bind(CircleMember member) {
            textMemberEmail.setText(member.getEmail());
            
            // Display role
            if (member.isLeader()) {
                textRole.setText("👑 Leader");
            } else {
                textRole.setText("Member");
            }

            // Display contribution
            double contribution = member.getTotalContribution();
            double percentage = member.getContributionPercentage(goalAmount);
            textContribution.setText(String.format("$%.2f (%.1f%%)", contribution, percentage));

            // Display date range
            if (member.getStartDate() != null && member.getEndDate() != null) {
                String dateRange = dateFormat.format(member.getStartDate()) + " - " 
                    + dateFormat.format(member.getEndDate());
                textDateRange.setText(dateRange);
            } else {
                textDateRange.setText("Date range not set");
            }

            // Display status
            if (member.isActive()) {
                textStatus.setText("Active");
                textStatus.setVisibility(View.VISIBLE);
            } else {
                textStatus.setText("Period ended");
                textStatus.setVisibility(View.VISIBLE);
            }
        }
    }
}

