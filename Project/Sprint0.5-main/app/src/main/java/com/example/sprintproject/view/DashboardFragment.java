package com.example.sprintproject.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.utils.Utils;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private TextView timeDisplay;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTimeRunnable;

    private DashboardViewModel dashboardViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Views
        timeDisplay = view.findViewById(R.id.time_display);
        TextView selectedDateDisplay = view.findViewById(R.id.selected_date_display);
        ImageView calendarIcon = view.findViewById(R.id.calendar_icon);
        Button logoutButton = view.findViewById(R.id.logout_button);
        Button darkModeButton = view.findViewById(R.id.btn_dark_mode);
        ImageView profilePicture = view.findViewById(R.id.profile_picture);
        com.google.android.material.card.MaterialCardView profileCard = view.findViewById(R.id.profile_picture_card);

        // Charts
        PieChart pieChart = view.findViewById(R.id.pieChart);
        BarChart barChart = view.findViewById(R.id.barChart);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false);
        pieChart.setCenterText("Spending Breakdown");
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);

        // ViewModel
        dashboardViewModel =
                new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Observe date label
        dashboardViewModel.getCurrentDate().observe(
                getViewLifecycleOwner(),
                date -> {
                    if (date == null) {
                        date = new Date();
                    }
                    SimpleDateFormat fmt =
                            new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    selectedDateDisplay.setText("Date: " + fmt.format(date));
                });

        // Calendar picker
        calendarIcon.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            Date current = dashboardViewModel.getCurrentDate().getValue();
            if (current != null) {
                cal.setTime(current);
            }
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, y, m, d) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(y, m, d);
                        dashboardViewModel.setCurrentDate(c.getTime());
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Live clock
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat fmt =
                        new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                timeDisplay.setText(fmt.format(new Date()));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateTimeRunnable);

        // Dark mode toggle
        final String PREFS_NAME = "app_prefs";
        final String KEY_DARK_MODE = "dark_mode";
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        updateDarkModeButton(darkModeButton, isDarkMode);
        
        darkModeButton.setOnClickListener(v -> {
            boolean currentMode = prefs.getBoolean(KEY_DARK_MODE, false);
            boolean newMode = !currentMode;
            prefs.edit().putBoolean(KEY_DARK_MODE, newMode).apply();
            
            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            
            updateDarkModeButton(darkModeButton, newMode);
        });

        // Profile picture click - open profile dialog
        if (profileCard != null) {
            profileCard.setOnClickListener(v -> showProfileDialog());
        }

        // Load profile picture if exists
        if (profilePicture != null) {
            loadProfilePicture(profilePicture);
        }

        // Logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Utils.clearCache(requireContext());
            requireActivity().getViewModelStore().clear();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();

            Toast.makeText(getContext(),
                    "Logged out successfully!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe combined dashboard data once
        dashboardViewModel.getDashboardData().observe(
                getViewLifecycleOwner(),
                data -> {
                    if (data == null) {
                        return;
                    }
                    updateDashboardUI(view, data);
                    dashboardViewModel.updateCharts(data);
                }
        );

        // Observe Pie chart data
        dashboardViewModel.getPieData().observe(
                getViewLifecycleOwner(),
                pieData -> {
                    if (pieData == null) {
                        return;
                    }
                    PieChart pieChart = view.findViewById(R.id.pieChart);
                    pieChart.setData(pieData);
                    pieChart.invalidate();
                }
        );

        // Observe Bar chart data
        dashboardViewModel.getBarData().observe(
                getViewLifecycleOwner(),
                barData -> {
                    if (barData == null) {
                        return;
                    }
                    BarChart barChart = view.findViewById(R.id.barChart);
                    barChart.setData(barData);
                    barChart.invalidate();
                }
        );
    }

    private void updateDashboardUI(@NonNull View view, Map<String, Object> data) {
        updateTotalAmounts(view, data);
        updateCategoriesList(view, data);
    }

    private void updateTotalAmounts(@NonNull View view, Map<String, Object> data) {
        Double totalSpentObj = (Double) data.get("totalSpent");
        Double totalBudgetObj = (Double) data.get("totalBudget");
        double totalSpent = totalSpentObj != null ? totalSpentObj : 0.0;
        double totalBudget = totalBudgetObj != null ? totalBudgetObj : 0.0;
        double remaining = Math.max(0.0, totalBudget - totalSpent);

        TextView totalSpentText = view.findViewById(R.id.text_total_spent);
        TextView totalRemainingText = view.findViewById(R.id.text_total_remaining);

        totalSpentText.setText(String.format(
                Locale.getDefault(),
                "Total Spent This Period: $%.2f",
                totalSpent
        ));
        totalRemainingText.setText(String.format(
                Locale.getDefault(),
                "Remaining Budget: $%.2f",
                remaining
        ));
    }

    @SuppressWarnings("unchecked")
    private void updateCategoriesList(@NonNull View view, Map<String, Object> data) {
        LinearLayout categoriesContainer = view.findViewById(R.id.categories_container);
        categoriesContainer.removeAllViews();
        Map<String, Double> categories = (Map<String, Double>) data.get("categories");

        if (categories != null && !categories.isEmpty()) {
            addCategoryViews(categoriesContainer, categories);
        } else {
            addEmptyCategoryView(categoriesContainer);
        }
    }

    private void addCategoryViews(LinearLayout container, Map<String, Double> categories) {
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            TextView tv = createCategoryTextView(entry.getKey(), entry.getValue());
            container.addView(tv);
        }
    }

    private void addEmptyCategoryView(LinearLayout container) {
        TextView tv = new TextView(requireContext());
        tv.setText("No expenses for this period.");
        tv.setTextSize(16);
        tv.setPadding(0, 4, 0, 4);
        container.addView(tv);
    }

    private TextView createCategoryTextView(String category, Double amount) {
        TextView tv = new TextView(requireContext());
        tv.setText(String.format(
                Locale.getDefault(),
                "%s: $%.2f",
                category,
                amount
        ));
        tv.setTextSize(16);
        tv.setPadding(0, 4, 0, 4);
        return tv;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateTimeRunnable);
    }

    /**
     * Updates the dark mode button icon based on current mode.
     */
    private void updateDarkModeButton(Button button, boolean isDarkMode) {
        if (button != null) {
            button.setText(isDarkMode ? "☀️" : "🌙");
        }
    }

    /**
     * Loads profile picture from storage.
     */
    private void loadProfilePicture(ImageView imageView) {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId != null && imageView != null) {
            String imagePath = prefs.getString("profile_picture_" + userId, null);
            if (imagePath != null) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    // Use default icon if loading fails
                }
            }
        }
    }

    /**
     * Shows profile dialog with picture upload and friends list.
     */
    private void showProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_profile, null);

        ImageView profilePictureDialog = dialogView.findViewById(R.id.profile_picture_dialog);
        com.google.android.material.button.MaterialButton btnUploadPicture =
                dialogView.findViewById(R.id.btn_upload_picture);
        androidx.recyclerview.widget.RecyclerView recyclerFriends =
                dialogView.findViewById(R.id.recycler_friends);
        TextView textNoFriends = dialogView.findViewById(R.id.text_no_friends);
        com.google.android.material.button.MaterialButton btnClose =
                dialogView.findViewById(R.id.btn_close_profile);

        // Load current profile picture
        loadProfilePicture(profilePictureDialog);

        // Setup friends list
        recyclerFriends.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        FriendsAdapter friendsAdapter = new FriendsAdapter();
        recyclerFriends.setAdapter(friendsAdapter);

        // Load friends from savings circles
        loadFriends(friendsAdapter, textNoFriends, recyclerFriends);

        // Upload picture button
        btnUploadPicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Profile")
                .setCancelable(true)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Loads friends from savings circles.
     */
    private void loadFriends(FriendsAdapter adapter, TextView emptyView, androidx.recyclerview.widget.RecyclerView recyclerView) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) {
            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            return;
        }

        com.example.sprintproject.repository.SavingsCircleRepository repository =
                com.example.sprintproject.repository.SavingsCircleRepository.getInstance();
        
        repository.loadUserCircles(userId, new com.example.sprintproject.repository.SavingsCircleRepository.RepositoryCallback<java.util.List<com.example.sprintproject.model.SavingsCircle>>() {
            @Override
            public void onSuccess(java.util.List<com.example.sprintproject.model.SavingsCircle> circles) {
                if (circles == null || circles.isEmpty()) {
                    if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                    if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                    return;
                }

                // Collect all friends from all circles
                java.util.Set<String> friendIds = new java.util.HashSet<>();
                java.util.Map<String, String> friendEmails = new java.util.HashMap<>();
                final int[] completed = {0};
                final int total = circles.size();

                for (com.example.sprintproject.model.SavingsCircle circle : circles) {
                    repository.loadCircleMembers(circle.getId(),
                            new com.example.sprintproject.repository.SavingsCircleRepository.RepositoryCallback<java.util.List<com.example.sprintproject.model.CircleMember>>() {
                                @Override
                                public void onSuccess(java.util.List<com.example.sprintproject.model.CircleMember> members) {
                                    if (members != null) {
                                        for (com.example.sprintproject.model.CircleMember member : members) {
                                            if (member != null && !member.getUserId().equals(userId)) {
                                                friendIds.add(member.getUserId());
                                                friendEmails.put(member.getUserId(), member.getEmail());
                                            }
                                        }
                                    }
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        // Update UI on main thread
                                        requireActivity().runOnUiThread(() -> {
                                            if (friendIds.isEmpty()) {
                                                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                                                if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                                            } else {
                                                if (emptyView != null) emptyView.setVisibility(View.GONE);
                                                if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
                                                java.util.List<Friend> friends = new java.util.ArrayList<>();
                                                for (String friendId : friendIds) {
                                                    friends.add(new Friend(friendId, friendEmails.get(friendId)));
                                                }
                                                adapter.updateFriends(friends);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    completed[0]++;
                                    if (completed[0] == total) {
                                        requireActivity().runOnUiThread(() -> {
                                            if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                                            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                                        });
                                    }
                                }
                            });
                }
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                    if (recyclerView != null) recyclerView.setVisibility(View.GONE);
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                saveProfilePicture(imageUri);
                // Reload profile picture
                View view = getView();
                if (view != null) {
                    ImageView profilePicture = view.findViewById(R.id.profile_picture);
                    if (profilePicture != null) {
                        loadProfilePicture(profilePicture);
                    }
                }
            }
        }
    }

    /**
     * Saves profile picture to local storage.
     */
    private void saveProfilePicture(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), imageUri);
            
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (userId != null) {
                java.io.File file = new java.io.File(requireContext().getFilesDir(), "profile_" + userId + ".jpg");
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                
                SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
                prefs.edit().putString("profile_picture_" + userId, file.getAbsolutePath()).apply();
                
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * For unit tests.
     */
    public void resetDashboardData() {
        if (dashboardViewModel != null) {
            dashboardViewModel.clearData();
        }
    }

    /**
     * Friend model for display.
     */
    private static class Friend {
        private String userId;
        private String email;

        Friend(String userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        String getUserId() {
            return userId;
        }

        String getEmail() {
            return email;
        }
    }

    /**
     * Adapter for friends list.
     */
    private static class FriendsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
        private java.util.List<Friend> friends = new java.util.ArrayList<>();

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            Friend friend = friends.get(position);
            holder.textEmail.setText(friend.getEmail() != null ? friend.getEmail() : "Unknown");
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        void updateFriends(java.util.List<Friend> newFriends) {
            this.friends = newFriends != null ? newFriends : new java.util.ArrayList<>();
            notifyDataSetChanged();
        }

        static class FriendViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView textEmail;

            FriendViewHolder(@NonNull View itemView) {
                super(itemView);
                textEmail = itemView.findViewById(R.id.text_friend_email);
            }
        }
    }
}