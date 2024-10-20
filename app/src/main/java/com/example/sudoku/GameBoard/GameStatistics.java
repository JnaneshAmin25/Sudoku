package com.example.sudoku.GameBoard;

public class GameStatistics {
    private int totalWins;
    private long bestCompletionTime; // In milliseconds
    private int totalGames;
    private long averageCompletionTime; // In milliseconds
    private int averageMistakes;
    private long totalHoursPlayed; // In milliseconds

    public GameStatistics() {
        this.totalWins = 0;
        this.bestCompletionTime = Long.MAX_VALUE; // Initialize to a large value
        this.totalGames = 0;
        this.averageCompletionTime = 0;
        this.averageMistakes = 0;
        this.totalHoursPlayed = 0;
    }

    // Getters and Setters
    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public long getBestCompletionTime() {
        return bestCompletionTime;
    }

    public void setBestCompletionTime(long bestCompletionTime) {
        this.bestCompletionTime = bestCompletionTime;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(int totalGames) {
        this.totalGames = totalGames;
    }

    public long getAverageCompletionTime() {
        return averageCompletionTime;
    }

    public void setAverageCompletionTime(long averageCompletionTime) {
        this.averageCompletionTime = averageCompletionTime;
    }

    public int getAverageMistakes() {
        return averageMistakes;
    }

    public void setAverageMistakes(int averageMistakes) {
        this.averageMistakes = averageMistakes;
    }

    public long getTotalHoursPlayed() {
        return totalHoursPlayed;
    }

    public void setTotalHoursPlayed(long totalHoursPlayed) {
        this.totalHoursPlayed = totalHoursPlayed;
    }
}
