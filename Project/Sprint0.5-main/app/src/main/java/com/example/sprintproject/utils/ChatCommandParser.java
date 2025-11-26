package com.example.sprintproject.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing and detecting custom chat commands.
 * Handles command detection and provides command types.
 */
public class ChatCommandParser {
    
    public enum CommandType {
        SUMMARIZE_SPENDING,
        COST_CUT_SUGGESTIONS,
        MONTHLY_COMPARISON,
        BUDGET_STATUS,
        SAVINGS_GOALS,
        UNKNOWN
    }
    
    /**
     * Detects if a message contains a command.
     *
     * @param message the user's message
     * @return the detected command type, or UNKNOWN if no command detected
     */
    public static CommandType detectCommand(String message) {
        if (message == null || message.trim().isEmpty()) {
            return CommandType.UNKNOWN;
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Summarize spending commands
        List<String> summarizeKeywords = Arrays.asList(
            "summarize my spending",
            "spending summary",
            "show my expenses",
            "what did i spend",
            "spending breakdown",
            "expense summary"
        );
        for (String keyword : summarizeKeywords) {
            if (lowerMessage.contains(keyword)) {
                return CommandType.SUMMARIZE_SPENDING;
            }
        }
        
        // Cost-cutting suggestions commands
        List<String> costCutKeywords = Arrays.asList(
            "suggest ways to save",
            "how can i save money",
            "cost cutting",
            "save money",
            "reduce spending",
            "cut costs",
            "saving suggestions"
        );
        for (String keyword : costCutKeywords) {
            if (lowerMessage.contains(keyword)) {
                return CommandType.COST_CUT_SUGGESTIONS;
            }
        }
        
        // Monthly comparison commands
        List<String> comparisonKeywords = Arrays.asList(
            "compare this month to last month",
            "monthly comparison",
            "compare months",
            "this month vs last month",
            "spending comparison"
        );
        for (String keyword : comparisonKeywords) {
            if (lowerMessage.contains(keyword)) {
                return CommandType.MONTHLY_COMPARISON;
            }
        }
        
        // Budget status commands
        List<String> budgetKeywords = Arrays.asList(
            "budget status",
            "show my budgets",
            "how am i doing on my budgets",
            "budget progress",
            "budget overview"
        );
        for (String keyword : budgetKeywords) {
            if (lowerMessage.contains(keyword)) {
                return CommandType.BUDGET_STATUS;
            }
        }
        
        // Savings goals commands
        List<String> savingsKeywords = Arrays.asList(
            "savings goals",
            "show my savings goals",
            "savings progress",
            "savings circles"
        );
        for (String keyword : savingsKeywords) {
            if (lowerMessage.contains(keyword)) {
                return CommandType.SAVINGS_GOALS;
            }
        }
        
        return CommandType.UNKNOWN;
    }
    
    /**
     * Checks if a message is a command.
     *
     * @param message the user's message
     * @return true if the message contains a command
     */
    public static boolean isCommand(String message) {
        return detectCommand(message) != CommandType.UNKNOWN;
    }
}

