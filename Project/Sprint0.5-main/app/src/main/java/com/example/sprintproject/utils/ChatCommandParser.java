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
        
        CommandType command = checkSummarizeSpending(lowerMessage);
        if (command != CommandType.UNKNOWN) {
            return command;
        }
        
        command = checkCostCutSuggestions(lowerMessage);
        if (command != CommandType.UNKNOWN) {
            return command;
        }
        
        command = checkMonthlyComparison(lowerMessage);
        if (command != CommandType.UNKNOWN) {
            return command;
        }
        
        command = checkBudgetStatus(lowerMessage);
        if (command != CommandType.UNKNOWN) {
            return command;
        }
        
        return checkSavingsGoals(lowerMessage);
    }

    private static CommandType checkSummarizeSpending(String lowerMessage) {
        List<String> keywords = Arrays.asList(
            "summarize my spending",
            "spending summary",
            "show my expenses",
            "what did i spend",
            "spending breakdown",
            "expense summary"
        );
        return checkKeywords(lowerMessage, keywords, CommandType.SUMMARIZE_SPENDING);
    }

    private static CommandType checkCostCutSuggestions(String lowerMessage) {
        List<String> keywords = Arrays.asList(
            "suggest ways to save",
            "how can i save money",
            "cost cutting",
            "save money",
            "reduce spending",
            "cut costs",
            "saving suggestions"
        );
        return checkKeywords(lowerMessage, keywords, CommandType.COST_CUT_SUGGESTIONS);
    }

    private static CommandType checkMonthlyComparison(String lowerMessage) {
        List<String> keywords = Arrays.asList(
            "compare this month to last month",
            "monthly comparison",
            "compare months",
            "this month vs last month",
            "spending comparison"
        );
        return checkKeywords(lowerMessage, keywords, CommandType.MONTHLY_COMPARISON);
    }

    private static CommandType checkBudgetStatus(String lowerMessage) {
        List<String> keywords = Arrays.asList(
            "budget status",
            "show my budgets",
            "how am i doing on my budgets",
            "budget progress",
            "budget overview"
        );
        return checkKeywords(lowerMessage, keywords, CommandType.BUDGET_STATUS);
    }

    private static CommandType checkSavingsGoals(String lowerMessage) {
        List<String> keywords = Arrays.asList(
            "savings goals",
            "show my savings goals",
            "savings progress",
            "savings circles"
        );
        return checkKeywords(lowerMessage, keywords, CommandType.SAVINGS_GOALS);
    }

    private static CommandType checkKeywords(String message, List<String> keywords, CommandType commandType) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return commandType;
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

