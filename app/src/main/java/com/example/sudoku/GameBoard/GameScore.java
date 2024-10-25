package com.example.sudoku.GameBoard;

public class GameScore {
    public String gameId;
    public String dateTime;
    public int mistakesCount;
    public boolean isSolved;
    public String timerMode;
    public int undoCount;
    public int pauseCount;
    public int eraseCount;
    public String completionStatus;
    public long score;

    // Default constructor
    public GameScore() {
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setMistakesCount(int mistakesCount) {
        this.mistakesCount = mistakesCount;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

    public void setTimerMode(String timerMode) {
        this.timerMode = timerMode;
    }

    public void setUndoCount(int undoCount) {
        this.undoCount = undoCount;
    }

    public void setPauseCount(int pauseCount) {
        this.pauseCount = pauseCount;
    }


    public void setEraseCount(int eraseCount) {
        this.eraseCount = eraseCount;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public void setScore(long score) {
        this.score = score;
    }
}

