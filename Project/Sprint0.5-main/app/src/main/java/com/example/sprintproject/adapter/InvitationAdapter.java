package com.example.sprintproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.CircleInvitation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

<<<<<<< HEAD
/**
 * Adapter for displaying circle invitations in a RecyclerView.
 */
=======
// Adapter for displaying circle invitations in a RecyclerView.
>>>>>>> f820f1a (temp)
public class InvitationAdapter
        extends RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder> {

    public interface OnInvitationActionListener {
        void onAccept(String invitationId);
        void onDecline(String invitationId);
    }

    private List<CircleInvitation> invitations;
    private OnInvitationActionListener listener;
    private SimpleDateFormat dateFormat;

<<<<<<< HEAD
    public InvitationAdapter(List<CircleInvitation> invitations,
                             OnInvitationActionListener listener) {
=======
    public InvitationAdapter(
            List<CircleInvitation> invitations,
            OnInvitationActionListener listener) {
>>>>>>> f820f1a (temp)
        this.invitations = invitations;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        CircleInvitation invitation = invitations.get(position);
        holder.bind(invitation);
    }

    @Override
    public int getItemCount() {
        return invitations != null ? invitations.size() : 0;
    }

    public void updateInvitations(List<CircleInvitation> newInvitations) {
        this.invitations = newInvitations;
        notifyDataSetChanged();
    }

    class InvitationViewHolder extends RecyclerView.ViewHolder {
        private TextView textInviterEmail;
        private TextView textInviteDate;
        private Button btnAccept;
        private Button btnDecline;

        InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            textInviterEmail = itemView.findViewById(R.id.text_inviter_email);
            textInviteDate = itemView.findViewById(R.id.text_invite_date);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }

        void bind(CircleInvitation invitation) {
            textInviterEmail.setText(invitation.getInviterEmail()
                    + " invited you");
            
            if (invitation.getCreatedAt() != null) {
<<<<<<< HEAD
                textInviteDate.setText("Invited on "
                    + dateFormat.format(invitation.getCreatedAt()));
=======
                textInviteDate.setText(
                        "Invited on " + dateFormat.format(invitation.getCreatedAt()));
>>>>>>> f820f1a (temp)
            } else {
                textInviteDate.setText("");
            }

            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccept(invitation.getId());
                }
            });

            btnDecline.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecline(invitation.getId());
                }
            });
        }
    }
}

