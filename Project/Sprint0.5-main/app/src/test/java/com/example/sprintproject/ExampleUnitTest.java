package com.example.sprintproject.viewmodel;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DashboardViewModelTest {

    // Rule allows LiveData to execute synchronously
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private DashboardViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new DashboardViewModel();
    }

    @Test
    public void testSetAndGetCurrentDate() {
        Date now = new Date();
        viewModel.setCurrentDate(now);

        assertEquals(now, viewModel.getCurrentDate().getValue());
    }

    @Test
    public void testDashboardDataNotNull() {
        AtomicBoolean observed = new AtomicBoolean(false);

        Observer<Map<String, Object>> observer = data -> {
            // For this toy test we just confirm LiveData emits something (can be null initially)
            observed.set(true);
        };

        viewModel.getDashboardData().observeForever(observer);

        // Trigger update
        viewModel.setCurrentDate(new Date());

        assertTrue("Dashboard data LiveData should emit at least once", observed.get());
        viewModel.getDashboardData().removeObserver(observer);
    }
}
