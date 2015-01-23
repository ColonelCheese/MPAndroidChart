package com.github.mikephil.charting.interfaces;

import com.github.mikephil.charting.data.DecartEntry;

/**
 * Listener for callbacks when selecting values inside the chart by
 * touch-gesture.
 *
 * @author Philipp Jahoda
 */
public interface OnDecartGraphValueSelectedListener {

    /**
     * Called when a value has been selected inside the chart.
     *
     * @param e            The selected Entry.
     */
    public void onValueSelected(DecartEntry e);

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    public void onNothingSelected();
}
