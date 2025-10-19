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
import com.example.sprintproject.viewmodel.TimeViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatbotFragment extends Fragment {
    private TimeViewModel timeViewModel;

    public ChatbotFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.activity_chatbot, container, false);

        timeViewModel = new ViewModelProvider(requireActivity()).get(TimeViewModel.class);

        timeViewModel.getCurrentDate().observe(getViewLifecycleOwner(), date -> {
            SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Log.d("ExpenseFragment", "Global date changed to " + fmt.format(date));
            reloadExpensesFor(date);
        });

        return view;
    }

    private void reloadExpensesFor(Date date) {
        // Use this date to filter or validate expenses
    }
}
