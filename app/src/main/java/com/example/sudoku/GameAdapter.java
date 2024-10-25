package com.example.sudoku;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final List<GameData> gameDataList;

    public GameAdapter(List<GameData> gameDataList) {
        this.gameDataList = gameDataList;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameData gameData = gameDataList.get(position);

        // Set data to TextViews
        holder.timerValue.setText(gameData.getTimerMode());
        holder.scoreValue.setText(String.valueOf(gameData.getScore()));
        holder.dateTimeValue.setText(gameData.getDateTime());
        holder.mistakesValue.setText(String.valueOf(gameData.getMistakesCount()));
        holder.completionText.setText(gameData.getCompletionStatus());
    }

    @Override
    public int getItemCount() {
        return gameDataList.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView timerValue, scoreValue, dateTimeValue, mistakesValue, completionText;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            timerValue = itemView.findViewById(R.id.timer_value);
            scoreValue = itemView.findViewById(R.id.scoreValue);
            dateTimeValue = itemView.findViewById(R.id.dateTimeValue);
            mistakesValue = itemView.findViewById(R.id.mistakesValue);
            completionText = itemView.findViewById(R.id.completionText);
        }
    }
}

