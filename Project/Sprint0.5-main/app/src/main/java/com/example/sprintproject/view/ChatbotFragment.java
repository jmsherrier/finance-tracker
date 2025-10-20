package com.example.sprintproject.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sprintproject.R;
import com.example.sprintproject.viewmodel.ChatbotViewModel;
import com.example.sprintproject.viewmodel.TimeViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment handling chatbot logic and reacting to date changes.
 */
public class ChatbotFragment extends Fragment {

    private ChatbotViewModel chatbotViewModel;
    private TimeViewModel timeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_chatbot, container, false);

        chatbotViewModel = new ViewModelProvider(requireActivity())
                .get(ChatbotViewModel.class);

        timeViewModel = new ViewModelProvider(requireActivity())
                .get(TimeViewModel.class);

        timeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt =
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Log.d("ChatbotFragment", "Date changed to " + fmt.format(date));
            reloadExpensesFor(date);
        });

        return view;
    }

    private void reloadExpensesFor(Date date) {
        // Use this date to sync chatbot logic
    }
}
