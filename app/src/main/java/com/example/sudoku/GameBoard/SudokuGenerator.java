package com.example.sudoku.GameBoard;

import static com.example.sudoku.GameBoard.GameBoard.fb_eraseCount;
import static com.example.sudoku.GameBoard.GameBoard.gameId;
import static com.example.sudoku.GameBoard.GameBoard.gameScoreRef;
import static com.example.sudoku.GameBoard.GameBoard.fb_mistakesCount;
import static com.example.sudoku.GameBoard.GameBoard.moveStack;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import com.google.android.material.button.MaterialButton;
import android.widget.GridLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SudokuGenerator {

    public static final int GRID_SIZE = 9;

    public static int[][] grid;

    public static TextView selectedCell = null;

    public static Context context;

    public static void generateSudoku(GridLayout sudokuBoard, Context context) {
        grid = new int[GRID_SIZE][GRID_SIZE];
        fillValues(grid);

        GameData.originalGrid = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(grid[i], 0, GameData.originalGrid[i], 0, GRID_SIZE);
        }
        Log.d("Original Grid", ":" + Arrays.deepToString(GameData.originalGrid));

        removeNumbers(grid);

        sudokuBoard.removeAllViews();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                TextView textView = new TextView(context);
                textView.setText(grid[i][j] == 0 ? "" : String.valueOf(grid[i][j]));

                textView.setTextColor(Color.BLACK);
                textView.setTextSize(24);
                textView.setGravity(Gravity.CENTER);

                // Set background for 3x3 matrix alternating color
                int gridRow = i / 3;
                int gridCol = j / 3;
                if ((gridRow + gridCol) % 2 == 0) {
                    textView.setBackgroundColor(Color.parseColor("#EEEEEE"));
                } else {
                    textView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                }

                // Set LayoutParams to position the TextView in the grid
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                params.width = 0;
                params.height = 0;
                params.setMargins(2, 2, 2, 2);
                textView.setLayoutParams(params);

                textView.setTag(new Cell(i, j));

                if (grid[i][j] == 0) {
                    textView.setOnClickListener(v -> {
                        if (selectedCell != null) {
                            selectedCell.setBackgroundColor(Color.TRANSPARENT); // Reset the background of the previously selected cell
                        }
                        selectedCell = textView;
                        textView.setBackgroundColor(Color.YELLOW); // Highlight selected cell
                    });
                } else {
                    textView.setClickable(false);
                    textView.setFocusable(false);
                    textView.setBackgroundColor(Color.LTGRAY); // Change background for fixed cells
                }

                sudokuBoard.addView(textView, params);
            }
        }
    }

    public static void setNumberInSelectedCell(TextView selectedCell, int number) {
        if (selectedCell != null) {
            Cell cellTag = (Cell) selectedCell.getTag();
            int row = cellTag.row;
            int col = cellTag.col;
            int correctValue = GameData.originalGrid[row][col];

            if (number != correctValue && GameBoard.isGameActive) {
                GameBoard.mistakes++;
                Log.d("SudokuGame", "Mistakes incremented to: " + GameBoard.mistakes);
                GameBoard.updateMistakeCounter();
            }

            if (number != correctValue) {
                fb_mistakesCount++;
                gameScoreRef.child(gameId).child("mistakesCount").setValue(fb_mistakesCount);
            }

            if (moveStack.size() == 2) {
                moveStack.remove(0); // Keep only the last two states
            }
            moveStack.push(cloneGrid(grid)); // Push a copy of the current grid state

            grid[row][col] = number;
            selectedCell.setText(String.valueOf(number)); // Set the text to the selected cell
            selectedCell.setBackgroundColor(Color.TRANSPARENT);

            if (GameBoard.isSudokuSolved()) {
                GameBoard.fb_isSolved = true; // Mark as solved
                GameBoard.endGame(context,true, "");
                GameBoard.showCongratulationsDialog(); // Call the dialog from GameBoard
            }

            SudokuGenerator.selectedCell = null; // Deselect the cell
        } else {
            Log.d("SudokuGame", "No cell is selected.");
        }
    }

    private static int[][] cloneGrid(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    public static void setupNumberButtons(MaterialButton[] numberButtons, GameBoard activity) {
        for (int i = 0; i < numberButtons.length; i++) {
            int number = i + 1; // Buttons are 1 to 9

            numberButtons[i].setOnClickListener(v -> {
                if (selectedCell != null) {
                    Log.d("Button Click", "Button clicked: " + number);
                    Cell cellTag = (Cell) selectedCell.getTag(); // Get row and column from the tag
                    int row = cellTag.row;
                    int col = cellTag.col;

                    // Log the row and column
                    Log.d("Selected Cell", "Row: " + row + ", Column: " + col);

                    // Call the method to set the number
                    setNumberInSelectedCell(selectedCell, number);

                } else {
                    Log.d("SudokuGame", "No cell is selected.");
                }
            });
        }
    }

    private static boolean fillValues(int[][] grid) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] == 0) {
                    List<Integer> numbers = new ArrayList<>();
                    for (int i = 1; i <= GRID_SIZE; i++) {
                        numbers.add(i);
                    }
                    Collections.shuffle(numbers);

                    for (int num : numbers) {
                        if (isSafe(grid, row, col, num)) {
                            grid[row][col] = num;

                            if (fillValues(grid)) {
                                return true;
                            } else {
                                grid[row][col] = 0;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isSafe(int[][] grid, int row, int col, int num) {
        // Check if 'num' is not present in the current row, current column, and current 3x3 subgrid
        for (int x = 0; x < 9; x++) {
            if (grid[row][x] == num || grid[x][col] == num) {
                return false;
            }
        }
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void removeNumbers(int[][] grid) {
        int count = 1; // Adjust this to set how many numbers you want to remove
        while (count > 0) {
            int row = (int) (Math.random() * GRID_SIZE);
            int col = (int) (Math.random() * GRID_SIZE);
            if (grid[row][col] != 0) {
                grid[row][col] = 0; // Set the value to 0 to make it empty
                count--;
            }
        }
    }

    public static void eraseSelectedCell() {
        if (selectedCell != null) {
            selectedCell.setText(""); // Set the text to the selected cell
            selectedCell.setBackgroundColor(Color.TRANSPARENT);
            fb_eraseCount++;
            gameScoreRef.child(gameId).child("eraseCount").setValue(fb_eraseCount);// Remove highlight after setting number
            SudokuGenerator.selectedCell = null;
        } else {
            Log.d("SudokuGame", "No cell is selected.");
        }
    }


}
