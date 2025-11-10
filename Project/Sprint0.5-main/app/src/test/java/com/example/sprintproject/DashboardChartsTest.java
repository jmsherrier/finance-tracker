package com.example.sprintproject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.sprintproject.model.Budget;
import com.example.sprintproject.viewmodel.DashboardViewModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {33}, manifest = Config.NONE)
public class DashboardChartsTest {

    private DashboardViewModel viewModel;
    private MockedStatic<FirebaseApp> firebaseAppMock;
    private MockedStatic<FirebaseFirestore> firestoreMock;

    @Before
    public void setup() {
        // Mock Firebase so no real Android/Firebase init occurs
        firebaseAppMock = Mockito.mockStatic(FirebaseApp.class);
        firestoreMock = Mockito.mockStatic(FirebaseFirestore.class);

        FirebaseApp fakeApp = Mockito.mock(FirebaseApp.class);
        FirebaseFirestore fakeFirestore = Mockito.mock(FirebaseFirestore.class);

        firebaseAppMock.when(FirebaseApp::getInstance).thenReturn(fakeApp);
        firestoreMock.when(FirebaseFirestore::getInstance).thenReturn(fakeFirestore);

        viewModel = new DashboardViewModel();
    }

    @After
    public void tearDown() {
        // Close static mocks so they don’t leak between tests
        if (firebaseAppMock != null) {
            firebaseAppMock.close();
        }
        if (firestoreMock != null) {
            firestoreMock.close();
        }
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

        // Ensure LiveData posts are executed
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        PieData pieData = viewModel.getPieData().getValue();
        assertNotNull(pieData);
        assertEquals(3, ((PieDataSet) pieData.getDataSetByIndex(0)).getEntryCount());

        BarData barData = viewModel.getBarData().getValue();
        assertNotNull(barData);
        assertEquals(2, ((BarDataSet) barData.getDataSetByIndex(0)).getEntryCount());
    }

    @Test
    public void testChartsUpdateWithNullData() {
        viewModel.updateCharts(null);

        // Wait for LiveData updates to propagate
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        PieData pieData = viewModel.getPieData().getValue();
        assertNotNull(pieData);
        assertEquals(1, ((PieDataSet) pieData.getDataSetByIndex(0)).getEntryCount());

        BarData barData = viewModel.getBarData().getValue();
        assertNotNull(barData);
        assertEquals(1, ((BarDataSet) barData.getDataSetByIndex(0)).getEntryCount());
    }
}

