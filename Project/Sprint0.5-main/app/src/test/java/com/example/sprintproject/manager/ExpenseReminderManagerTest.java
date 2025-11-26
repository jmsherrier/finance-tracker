package com.example.sprintproject.manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.sprintproject.model.ExpenseReminder;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExpenseReminderManager class.
 */
public class ExpenseReminderManagerTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private Observer<ExpenseReminder> mockObserver;

    private ExpenseReminderManager manager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup shared preferences mock
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);

        manager = ExpenseReminderManager.getInstance();
        manager.initialize(mockContext, mockFirestore);
    }

    @Test
    public void testSingletonInstance() {
        ExpenseReminderManager instance1 = ExpenseReminderManager.getInstance();
        ExpenseReminderManager instance2 = ExpenseReminderManager.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    public void testInitialization() {
        assertNotNull(manager);
        assertNotNull(manager.getCurrentReminder());
    }

    @Test
    public void testGetCurrentReminderReturnsLiveData() {
        assertNotNull(manager.getCurrentReminder());
    }

    @Test
    public void testDismissCurrentReminder() {
        manager.getCurrentReminder().observeForever(mockObserver);
        manager.dismissCurrentReminder();

        verify(mockObserver).onChanged(null);
        manager.getCurrentReminder().removeObserver(mockObserver);
    }

    @Test
    public void testClearReminderHistory() {
        manager.clearReminderHistory();

        verify(mockPrefs).edit();
        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }

    @Test
    public void testCheckMissedExpensesWithNullUserId() {
        // Should not throw exception with null userId
        manager.checkMissedExpenses(null);
    }

    @Test
    public void testCheckMissedExpensesWithValidUserId() {
        // Should not throw exception with valid userId
        manager.checkMissedExpenses("user123");
    }

    @Test
    public void testMultipleDismissals() {
        manager.dismissCurrentReminder();
        manager.dismissCurrentReminder();
        manager.dismissCurrentReminder();

        // Should handle multiple dismissals without error
    }
}
