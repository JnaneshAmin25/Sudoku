package com.example.sudoku;

public class GameData {
    private String timerMode;
    private int score;
    private String dateTime;
    private int mistakesCount;
    private String completionStatus;

    // Default constructor required for calls to DataSnapshot.getValue(GameData.class)
    public GameData() {}

    public String getTimerMode() {
        return timerMode;
    }

    public int getScore() {
        return score;
    }

    public String getDateTime() {
        return dateTime;
    }

    public int getMistakesCount() {
        return mistakesCount;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }
}

