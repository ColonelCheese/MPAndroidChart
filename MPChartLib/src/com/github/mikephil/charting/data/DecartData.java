package com.github.mikephil.charting.data;

import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.LimitLine;

import java.util.ArrayList;

public class DecartData<T extends DecartDataSet<? extends DecartEntry>> {
    /**
     * maximum y-value in the y-value array
     */
    protected float mYMax = 0.0f;

    /**
     * the minimum y-value in the y-value array
     */
    protected float mYMin = 0.0f;

    /**
     * the total sum of all y-values
     */
    private float mYValueSum = 0f;

    /**
     * total number of y-values across all DataSet objects
     */
    private int mYValCount = 0;

    /**
     * maximum x-value in the x-value array
     */
    protected float mXMax = 0.0f;

    /**
     * the minimum x-value in the x-value array
     */
    protected float mXMin = 0.0f;

    /**
     * the total sum of all x-values
     */
    private float mXValueSum = 0f;

    /**
     * total number of x-values across all DataSet objects
     */
    private int mXValCount = 0;

    /**
     * array that holds all DataSets the ChartData object represents
     */
    protected ArrayList<T> mDataSets;
    /**
     * array of limit-lines that are set for this data object
     */
    private ArrayList<LimitLine> mLimitLines;
    /**
     * contains the average length (in characters) an entry in the x-vals array
     * has
     */
    private float mXValAverageLength;


    private static ArrayList<DecartDataSet> toArrayList(DecartDataSet dataSet) {
        ArrayList<DecartDataSet> sets = new ArrayList<DecartDataSet>();
        sets.add(dataSet);
        return sets;
    }

    /**
     * Returns the maximum shape-size across all DataSets.
     *
     * @return
     */
    public float getGreatestShapeSize() {

        float max = 0f;

        for (DecartDataSet set : mDataSets) {
            float size = set.getScatterShapeSize();

            if (size > max)
                max = size;
        }

        return max;
    }

    /**
     * Adds a new LimitLine to the data.
     *
     * @param limitLine
     */
    public void addLimitLine(LimitLine limitLine) {
        if (mLimitLines == null)
            mLimitLines = new ArrayList<LimitLine>();
        mLimitLines.add(limitLine);
        updateMinMax();
    }

    /**
     * Adds a new array of LimitLines.
     *
     * @param lines
     */
    public void addLimitLines(ArrayList<LimitLine> lines) {
        mLimitLines = lines;
        updateMinMax();
    }

    /**
     * Resets the limit lines array to null. Causes no more limit lines to be
     * set for this data object.
     */
    public void resetLimitLines() {
        mLimitLines = null;
        calcMinMax(mDataSets);
    }

    /**
     * Returns the LimitLine array of this data object.
     *
     * @return
     */
    public ArrayList<LimitLine> getLimitLines() {
        return mLimitLines;
    }

    /**
     * Returns the LimitLine from the limitlines array at the specified index.
     *
     * @param index
     * @return
     */
    public LimitLine getLimitLine(int index) {
        if (mLimitLines == null || mLimitLines.size() <= index)
            return null;
        else
            return mLimitLines.get(index);
    }

    /**
     * Updates the min and max y-value according to the set limits.
     */
    private void updateMinMax() {

        if (mLimitLines == null)
            return;

        for (int i = 0; i < mLimitLines.size(); i++) {

            LimitLine l = mLimitLines.get(i);

            if (l.getLimit() > mYMax)
                mYMax = l.getLimit();

            if (l.getLimit() < mYMin)
                mYMin = l.getLimit();
        }
    }


    /**
     * constructor for chart data
     *
     * @param sets the dataset array
     */
    public DecartData(ArrayList<T> sets) {
        this.mDataSets = sets;

        init();
    }

    /**
     * Turns an array of strings into an arraylist of strings.
     *
     * @param array
     * @return
     */
    private ArrayList<String> arrayToArrayList(String[] array) {

        ArrayList<String> arraylist = new ArrayList<String>();
        for (int i = 0; i < array.length; i++) {
            arraylist.add(array[i]);
        }

        return arraylist;
    }

    /**
     * performs all kinds of initialization calculations, such as min-max and
     * value count and sum
     */
    private void init() {

        calcMinMax(mDataSets);
        calcYValueSum(mDataSets);
        calcYValueCount(mDataSets);

        calcXValAverageLength();
    }

    /**
     * calculates the average length (in characters) across all x-value strings
     */
    private void calcXValAverageLength() {

        if (mDataSets.size() == 0) {
            mXValAverageLength = 1;
            return;
        }

        float sum = 1f;

        for (int i = 0; i < mDataSets.size(); i++) {
            sum += String.valueOf(mDataSets.get(i).getXMax()).length();
        }

        mXValAverageLength = sum / (float) mDataSets.size();
    }

    /**
     * Call this method to let the CartData know that the underlying data has
     * changed.
     */
    public void notifyDataChanged() {
        init();
    }

    /**
     * calc minimum and maximum y value over all datasets
     */
    protected void calcMinMax(ArrayList<T> dataSets) {

        if (dataSets == null || dataSets.size() < 1) {

            mYMax = 0f;
            mYMin = 0f;
            mXMax = 0f;
            mXMin = 0f;
        } else {

            mYMin = dataSets.get(0).getYMin();
            mYMax = dataSets.get(0).getYMax();
            mXMin = dataSets.get(0).getXMin();
            mXMax = dataSets.get(0).getXMax();

            for (int i = 0; i < dataSets.size(); i++) {
                if (dataSets.get(i).getYMin() < mYMin)
                    mYMin = dataSets.get(i).getYMin();

                if (dataSets.get(i).getYMax() > mYMax)
                    mYMax = dataSets.get(i).getYMax();

                if (dataSets.get(i).getXMin() < mXMin)
                    mXMin = dataSets.get(i).getXMin();

                if (dataSets.get(i).getXMax() > mXMax)
                    mXMax = dataSets.get(i).getXMax();
            }
        }
    }

    /**
     * calculates the sum of all y-values in all datasets
     */
    protected void calcYValueSum(ArrayList<T> dataSets) {

        mYValueSum = 0;

        if (dataSets == null)
            return;

        for (int i = 0; i < dataSets.size(); i++) {
            mYValueSum += Math.abs(dataSets.get(i).getYValueSum());
        }
    }

    /**
     * Calculates the total number of y-values across all DataSets the ChartData
     * represents.
     *
     * @return
     */
    protected void calcYValueCount(ArrayList<T> dataSets) {

        mYValCount = 0;

        if (dataSets == null)
            return;

        int count = 0;

        for (int i = 0; i < dataSets.size(); i++) {
            count += dataSets.get(i).getEntryCount();
        }

        mYValCount = count;
    }

    /** ONLY GETTERS AND SETTERS BELOW THIS */

    /**
     * returns the number of LineDataSets this object contains
     *
     * @return
     */
    public int getDataSetCount() {
        if (mDataSets == null)
            return 0;
        return mDataSets.size();
    }

    /**
     * Returns the smallest y-value the data object contains.
     *
     * @return
     */
    public float getYMin() {
        return mYMin;
    }

    /**
     * Returns the greatest y-value the data object contains.
     *
     * @return
     */
    public float getYMax() {
        return mYMax;
    }

    /**
     * returns the average length (in characters) across all values in the
     * x-vals array
     *
     * @return
     */
    public float getXValAverageLength() {
        return mXValAverageLength;
    }

    /**
     * Returns the total y-value sum across all DataSet objects the this object
     * represents.
     *
     * @return
     */
    public float getYValueSum() {
        return mYValueSum;
    }

    /**
     * Returns the total number of y-values across all DataSet objects the this
     * object represents.
     *
     * @return
     */
    public int getYValCount() {
        return mYValCount;
    }


    /**
     * Returns an the array of DataSets this object holds.
     *
     * @return
     */
    public ArrayList<T> getDataSets() {
        return mDataSets;
    }

    // /**
    // * returns the Entries array from the DataSet at the given index. If a
    // * filter is set, the filtered Entries are returned
    // *
    // * @param index
    // * @return
    // */
    // public ArrayList<Entry> getYVals(int index) {
    // return mDataSets.get(index).getYVals();
    // }

    /**
     * Retrieve the index of a DataSet with a specific label from the ChartData.
     * Search can be case sensitive or not. IMPORTANT: This method does
     * calculations at runtime, do not over-use in performance critical
     * situations.
     *
     * @param dataSets   the DataSet array to search
     * @param type
     * @param ignorecase if true, the search is not case-sensitive
     * @return
     */
    protected int getDataSetIndexByLabel(ArrayList<T> dataSets, String label,
                                         boolean ignorecase) {

        if (ignorecase) {
            for (int i = 0; i < dataSets.size(); i++)
                if (label.equalsIgnoreCase(dataSets.get(i).getLabel()))
                    return i;
        } else {
            for (int i = 0; i < dataSets.size(); i++)
                if (label.equals(dataSets.get(i).getLabel()))
                    return i;
        }

        return -1;
    }

    /**
     * Returns the labels of all DataSets as a string array.
     *
     * @return
     */
    protected String[] getDataSetLabels() {

        String[] types = new String[mDataSets.size()];

        for (int i = 0; i < mDataSets.size(); i++) {
            types[i] = mDataSets.get(i).getLabel();
        }

        return types;
    }

    /**
     * Get the Entry for a corresponding highlight object
     *
     * @param highlight
     * @return the entry that is highlighted
     */
    public Entry getEntryForHighlight(Highlight highlight) {
        throw new RuntimeException("todo! ");
//        return mDataSets.get(highlight.getDataSetIndex()).getEntryForXIndex(
//                highlight.getXIndex());
    }

    /**
     * Returns the DataSet object with the given label. Search can be case
     * sensitive or not. IMPORTANT: This method does calculations at runtime.
     * Use with care in performance critical situations.
     *
     * @param label
     * @param ignorecase
     * @return
     */
    public T getDataSetByLabel(String label, boolean ignorecase) {

        int index = getDataSetIndexByLabel(mDataSets, label, ignorecase);

        if (index < 0 || index >= mDataSets.size())
            return null;
        else
            return mDataSets.get(index);
    }

    /**
     * Returns the DataSet object at the given index.
     *
     * @param index
     * @return
     */
    public T getDataSetByIndex(int index) {

        if (mDataSets == null || index < 0 || index >= mDataSets.size())
            return null;

        return mDataSets.get(index);
    }

    /**
     * Adds a DataSet dynamically.
     *
     * @param d
     */
    public void addDataSet(T d) {
        if (mDataSets == null)
            mDataSets = new ArrayList<T>();
        mDataSets.add(d);

        mYValCount += d.getEntryCount();
        mYValueSum += d.getYValueSum();
        mXValCount += d.getEntryCount();
        mXValueSum += d.getXValueSum();

        if (mYMax < d.getYMax())
            mYMax = d.getYMax();
        if (mYMin > d.getYMin())
            mYMin = d.getYMin();
        if (mXMax < d.getXMax())
            mXMax = d.getXMax();
        if (mXMin > d.getXMin())
            mXMin = d.getXMin();
    }

    /**
     * Removes the given DataSet from this data object. Also recalculates all
     * minimum and maximum values. Returns true if a DataSet was removed, false
     * if no DataSet could be removed.
     *
     * @param d
     */
    public boolean removeDataSet(T d) {

        if (mDataSets == null || d == null)
            return false;

        boolean removed = mDataSets.remove(d);

        // if a DataSet was removed
        if (removed) {

            mYValCount -= d.getEntryCount();
            mYValueSum -= d.getYValueSum();
            mXValCount -= d.getEntryCount();
            mXValueSum -= d.getXValueSum();

            calcMinMax(mDataSets);
        }

        return removed;
    }

    /**
     * Removes the DataSet at the given index in the DataSet array from the data
     * object. Also recalculates all minimum and maximum values. Returns true if
     * a DataSet was removed, false if no DataSet could be removed.
     *
     * @param index
     */
    public boolean removeDataSet(int index) {

        if (mDataSets == null || index >= mDataSets.size() || index < 0)
            return false;

        T set = mDataSets.get(index);
        return removeDataSet(set);
    }

    /**
     * Removes the given Entry object from the DataSet at the specified index.
     *
     * @param e
     * @param dataSetIndex
     */
    public boolean removeEntry(DecartEntry e, int dataSetIndex) {

        // entry null, outofbounds
        if (e == null || dataSetIndex >= mDataSets.size())
            return false;

        // remove the entry from the dataset
        boolean removed = mDataSets.get(dataSetIndex).removeEntry(e);

        if (removed) {

            float yval = e.getYVal();

            mYValCount -= 1;
            mYValueSum -= yval;

            float xval = e.getYVal();

            mXValCount -= 1;
            mXValueSum -= xval;

            calcMinMax(mDataSets);
        }

        return removed;
    }

    /**
     * Returns the DataSet that contains the provided Entry, or null, if no
     * DataSet contains this Entry.
     *
     * @param e
     * @return
     */
    public T getDataSetForEntry(DecartEntry e) {

        if (e == null)
            return null;

        for (int i = 0; i < mDataSets.size(); i++) {

            T set = mDataSets.get(i);

            for (int j = 0; j < set.getEntryCount(); j++) {
                if (set.containsEntry(e))
                    return set;
            }
        }

        return null;
    }

    /**
     * Returns all colors used across all DataSet objects this object
     * represents.
     *
     * @return
     */
    public int[] getColors() {

        if (mDataSets == null)
            return null;

        int clrcnt = 0;

        for (int i = 0; i < mDataSets.size(); i++) {
            clrcnt += mDataSets.get(i).getColors().size();
        }

        int[] colors = new int[clrcnt];
        int cnt = 0;

        for (int i = 0; i < mDataSets.size(); i++) {

            ArrayList<Integer> clrs = mDataSets.get(i).getColors();

            for (Integer clr : clrs) {
                colors[cnt] = clr;
                cnt++;
            }
        }

        return colors;
    }

    /**
     * Generates an x-values array filled with numbers in range specified by the
     * parameters. Can be used for convenience.
     *
     * @return
     */
    public static ArrayList<String> generateXVals(int from, int to) {

        ArrayList<String> xvals = new ArrayList<String>();

        for (int i = from; i < to; i++) {
            xvals.add("" + i);
        }

        return xvals;
    }
}
