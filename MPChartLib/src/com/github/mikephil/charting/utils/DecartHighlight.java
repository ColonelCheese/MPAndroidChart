package com.github.mikephil.charting.utils;

import com.github.mikephil.charting.data.DecartEntry;

/**
 * Contains information needed to determine the highlighted value.
 *
 * @author Philipp Jahoda
 */
public class DecartHighlight {

    /**
     * the selected entry
     */
    private DecartEntry mDecartEntry;

    private int mDataSetIndex;

    private int mEntryIndex;

    /**
     * constructor
     *
     * @param entryIndex   the index of the highlighted entry in dataset
     * @param dataSetIndex the index of the DataSet the highlighted value belongs to
     */
    public DecartHighlight(DecartEntry decartEntry, int dataSetIndex, int entryIndex) {
        this.mDecartEntry = decartEntry;
        this.mDataSetIndex = dataSetIndex;
        this.mEntryIndex = entryIndex;
    }

    public DecartEntry getDecartEntry() {
        return mDecartEntry;
    }

    public int getDataSetIndex() {
        return mDataSetIndex;
    }

    public int getmEntryIndex() {
        return mEntryIndex;
    }

    public void setmEntryIndex(int mEntryIndex) {
        this.mEntryIndex = mEntryIndex;
    }

    /**
     * returns true if this highlight object is equal to the other (compares
     * xIndex and dataSetIndex)
     *
     * @param h
     * @return
     */
    public boolean equalTo(DecartHighlight h) {

        if (h == null)
            return false;
        else {
            if (this.mDecartEntry == h.mDecartEntry && mDataSetIndex == h.mDataSetIndex)
                return true;
            else
                return false;
        }
    }
}
