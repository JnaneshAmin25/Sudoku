package com.example.sudoku.GameBoard;

public class GameScore {
    private String gameId;
    private String dateTime;
    private int mistakesCount;
    private boolean isSolved;
    private String timerMode;
    private int undoCount;
    private int pauseCount;
    private int eraseCount;
    private long timeLeft;
    private String completionStatus;
    private String reasonForFailure;
    private boolean gameAbandoned;

    // Default constructor
    public GameScore() {
        // Required for Firebase
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getMistakesCount() {
        return mistakesCount;
    }

    public void setMistakesCount(int mistakesCount) {
        this.mistakesCount = mistakesCount;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public String getTimerMode() {
        return timerMode;
    }

    public void setTimerMode(String timerMode) {
        this.timerMode = timerMode;
    }

    public int getUndoCount() {
        return undoCount;
    }

    public void setUndoCount(int undoCount) {
        this.undoCount = undoCount;
    }

    public int getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(int pauseCount) {
        this.pauseCount = pauseCount;
    }

    public int getEraseCount() {
        return eraseCount;
    }

    public void setEraseCount(int eraseCount) {
        this.eraseCount = eraseCount;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(long timeLeft) {
        this.timeLeft = timeLeft;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public String getReasonForFailure() {
        return reasonForFailure;
    }

    public void setReasonForFailure(String reasonForFailure) {
        this.reasonForFailure = reasonForFailure;
    }

    public boolean isGameAbandoned() {
        return gameAbandoned;
    }

    public void setGameAbandoned(boolean gameAbandoned) {
        this.gameAbandoned = gameAbandoned;
    }
}

