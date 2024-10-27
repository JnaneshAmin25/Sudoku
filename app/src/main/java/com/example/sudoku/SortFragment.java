package com.example.sudoku;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SortFragment extends DialogFragment {

    public interface SortOptionListener {
        void onSortOptionSelected(String option);
    }

    private SortOptionListener listener;

    public SortFragment(SortOptionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sort_frame_layout, container, false);

        // Setting click listeners for each sort option
        view.findViewById(R.id.sortByRecent).setOnClickListener(v -> {
            listener.onSortOptionSelected("Recent");
            dismiss();
        });

        view.findViewById(R.id.sortByHighestScore).setOnClickListener(v -> {
            listener.onSortOptionSelected("Highest Score");
            dismiss();
        });

        view.findViewById(R.id.sortByLowestScore).setOnClickListener(v -> {
            listener.onSortOptionSelected("Lowest Score");
            dismiss();
        });

        return view;
    }
}
