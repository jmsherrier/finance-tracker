package com.example.sprintproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.BarData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardChartsTest {
    private DashboardViewModel viewModel;

    @Before
    public void setup() {
        viewModel = new DashboardViewModel();
    }
    @Test
    public void testChartsUpdateWithValidData() {
        Map<String, Double> categories = new HashMap<>();
        categories.put("Food", 40.0);
        categories.put("Travel", 30.0);
        categories.put("Entertainment", 30.0);

        List<Budget> budgets = new ArrayList<>();
        Budget b1 = new Budget();
        b1.setTitle("Essentials");
        b1.setTotalAmount(500.0);
        b1.setSpentAmount(200.0);
        budgets.add(b1);

        Budget b2 = new Budget();
        b2.setTitle("Leisure");
        b2.setTotalAmount(300.0);
        b2.setSpentAmount(150.0);
        budgets.add(b2);

        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("budgets", budgets);
        viewModel.updateCharts(data);

        PieData pieData = viewModel.getPieData().getValue();
        assertNotNull("PieData should not be null", pieData);
        PieDataSet pieSet = (PieDataSet) pieData.getDataSetByIndex(0);
        assertEquals("Should have 3 pie slices", 3, pieSet.getEntryCount());

        BarData barData = viewModel.getBarData().getValue();
        assertNotNull("BarData should not be null", barData);
        BarDataSet barSet = (BarDataSet) barData.getDataSetByIndex(0);
        assertEquals("Should have 2 bar entries", 2, barSet.getEntryCount());
    }

    @Test
    public void testChartsUpdateWithValidDataAgain() {
        Map<String, Double> categories = new HashMap<>();
        categories.put("Travel", 40.0);
        categories.put("Food", 30.0);
        categories.put("Movies", 30.0);

        List<Budget> budgets = new ArrayList<>();
        Budget b1 = new Budget();
        b1.setTitle("Necessities");
        b1.setTotalAmount(500.0);
        b1.setSpentAmount(200.0);
        budgets.add(b1);

        Budget b2 = new Budget();
        b2.setTitle("Fun");
        b2.setTotalAmount(300.0);
        b2.setSpentAmount(150.0);
        budgets.add(b2);

        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("budgets", budgets);
        viewModel.updateCharts(data);

        PieData pieData = viewModel.getPieData().getValue();
        assertNotNull("PieData should not be null", pieData);
        PieDataSet pieSet = (PieDataSet) pieData.getDataSetByIndex(0);
        assertEquals("Should have 3 pie slices", 3, pieSet.getEntryCount());

        BarData barData = viewModel.getBarData().getValue();
        assertNotNull("BarData should not be null", barData);
        BarDataSet barSet = (BarDataSet) barData.getDataSetByIndex(0);
        assertEquals("Should have 2 bar entries", 2, barSet.getEntryCount());
    }
    @Test
    public void testChartsUpdateWithNullData() {
        viewModel.updateCharts(null);
        PieData pieData = viewModel.getPieData().getValue();
        assertNotNull("Placeholder PieData should not be null", pieData);
        PieDataSet pieSet = (PieDataSet) pieData.getDataSetByIndex(0);
        assertEquals("Should have 1 pie slice", 1, pieSet.getEntryCount());

        BarData barData = viewModel.getBarData().getValue();
        assertNotNull("Placeholder BarData should not be null", barData);
        BarDataSet barSet = (BarDataSet) barData.getDataSetByIndex(0);
        assertEquals("Should have 1 bar entry", 1, barSet.getEntryCount());

    }
}
