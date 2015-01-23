package com.github.mikephil.charting.data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;

import com.github.mikephil.charting.charts.DecartGraph;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DecartDataSet<T extends DecartEntry> {

    /**
     * arraylist representing all colors that are used for this DataSet
     */
    protected ArrayList<Integer> mColors = null;

    /**
     * the entries that this dataset represents / holds together
     */
    protected ArrayList<T> mEntries = null;

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
     * label that describes the DataSet or the data the DataSet represents
     */
    private String mLabel = "DataSet";

    /**
     * default highlight color
     */
    protected int mHighLightColor = Color.rgb(255, 187, 115);
    /**
     * the size the scattershape will have, in screen pixels
     */
    private float mShapeSize = 12f;

    /**
     * the type of shape that is set to be drawn where the values are at,
     * default ScatterShape.SQUARE
     */
    private DecartGraph.GraphShape mGraphShape = DecartGraph.GraphShape.SQUARE;

    /**
     * Custom path object the user can provide that is drawn where the values
     * are at. This is used when ScatterShape.CUSTOM is set for a DataSet.
     */
    private Path mCustomScatterPath = null;

    /**
     * Creates a new DataSet object with the given values it represents. Also, a
     * label that describes the DataSet can be specified. The label can also be
     * used to retrieve the DataSet from a ChartData object.
     *
     * @param yVals
     * @param label
     */
    public DecartDataSet(ArrayList<T> entries, String label) {

        this.mLabel = label;
        this.mEntries = entries;

        if (mEntries == null)
            mEntries = new ArrayList<T>();

        mColors = new ArrayList<Integer>();

        mColors.add(Color.rgb(140, 234, 255));

        calcMinMax();
        calcYValueSum();
    }

    /**
     * Use this method to tell the data set that the underlying data has changed
     */
    public void notifyDataSetChanged() {
        calcMinMax();
        calcYValueSum();
    }

    /**
     * calc minimum and maximum y value
     */
    protected void calcMinMax() {
        if (mEntries.size() == 0) {
            return;
        }

        mYMin = mEntries.get(0).getYVal();
        mYMax = mEntries.get(0).getYVal();
        mXMin = mEntries.get(0).getXVal();
        mXMax = mEntries.get(0).getXVal();

        for (int i = 0; i < mEntries.size(); i++) {

            DecartEntry e = mEntries.get(i);

            if (e != null) {

                if (e.getYVal() < mYMin)
                    mYMin = e.getYVal();

                if (e.getYVal() > mYMax)
                    mYMax = e.getYVal();

                if (e.getXVal() < mXMin)
                    mXMin = e.getXVal();

                if (e.getXVal() > mXMax)
                    mXMax = e.getXVal();
            }
        }
    }

    /**
     * calculates the sum of all y-values
     */
    private void calcYValueSum() {

        mYValueSum = 0;

        for (int i = 0; i < mEntries.size(); i++) {
            DecartEntry e = mEntries.get(i);
            if (e != null)
                mYValueSum += Math.abs(e.getYVal());
        }
    }

    /**
     * returns the number of y-values this DataSet represents
     *
     * @return
     */
    public int getEntryCount() {
        return mEntries.size();
    }

    /**
     * returns the DataSets Entry array
     *
     * @return
     */
    public ArrayList<T> getEntries() {
        return mEntries;
    }

    /**
     * gets the sum of all y-values
     *
     * @return
     */
    public float getYValueSum() {
        return mYValueSum;
    }

    /**
     * returns the minimum y-value this DataSet holds
     *
     * @return
     */
    public float getYMin() {
        return mYMin;
    }

    /**
     * returns the maximum y-value this DataSet holds
     *
     * @return
     */
    public float getYMax() {
        return mYMax;
    }

    /**
     * gets the sum of all x-values
     *
     * @return
     */
    public float getXValueSum() {
        return mXValueSum;
    }

    /**
     * returns the minimum x-value this DataSet holds
     *
     * @return
     */
    public float getXMin() {
        return mXMin;
    }

    /**
     * returns the maximum x-value this DataSet holds
     *
     * @return
     */
    public float getXMax() {
        return mXMax;
    }

    /**
     * returns the type of the DataSet, specified via constructor
     *
     * @return
     */
    // public int getType() {
    // return mType;
    // }
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(toSimpleString());
        for (int i = 0; i < mEntries.size(); i++) {
            buffer.append(mEntries.get(i).toString() + " ");
        }
        return buffer.toString();
    }

    /**
     * Returns a simple string representation of the DataSet with the type and
     * the number of Entries.
     *
     * @return
     */
    public String toSimpleString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DataSet, label: " + mLabel + ", entries: " + mEntries.size() + "\n");
        return buffer.toString();
    }

    /**
     * Returns the label string that describes the DataSet.
     *
     * @return
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * Adds an Entry to the DataSet dynamically. This will also recalculate the
     * current minimum and maximum values of the DataSet and the value-sum.
     *
     * @param d
     */
    public void addEntry(DecartEntry e) {

        if (e == null)
            return;

        float yval = e.getYVal();
        float xval = e.getYVal();

        if (mEntries == null || mEntries.size() <= 0) {

            mEntries = new ArrayList<T>();
            mYMax = yval;
            mYMin = yval;
            mXMax = xval;
            mXMin = xval;
        } else {

            if (mYMax < yval)
                mYMax = yval;
            if (mYMin > yval)
                mYMin = yval;
            if (mXMax < xval)
                mXMax = xval;
            if (mXMin > xval)
                mXMin = xval;
        }

        mYValueSum += yval;
        mXValueSum += xval;

        // add the entry
        mEntries.add((T) e);
    }

    /**
     * Removes an Entry from the DataSets entries array. This will also
     * recalculate the current minimum and maximum values of the DataSet and the
     * value-sum. Returns true if an Entry was removed, false if no Entry could
     * be removed.
     *
     * @param e
     */
    public boolean removeEntry(DecartEntry e) {

        if (e == null)
            return false;

        // remove the entry
        boolean removed = mEntries.remove(e);

        if (removed) {

            float yval = e.getYVal();
            mYValueSum -= yval;
            float xval = e.getXVal();
            mXValueSum -= xval;

            calcMinMax();
        }

        return removed;
    }


    /** BELOW THIS COLOR HANDLING */

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. If you are using colors from the resources,
     * make sure that the colors are already prepared (by calling
     * getResources().getColor(...)) before adding them to the DataSet.
     *
     * @param colors
     */
    public void setColors(ArrayList<Integer> colors) {
        this.mColors = colors;
    }

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. If you are using colors from the resources,
     * make sure that the colors are already prepared (by calling
     * getResources().getColor(...)) before adding them to the DataSet.
     *
     * @param colors
     */
    public void setColors(int[] colors) {
        this.mColors = ColorTemplate.createColors(colors);
    }

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. You can use
     * "new int[] { R.color.red, R.color.green, ... }" to provide colors for
     * this method. Internally, the colors are resolved using
     * getResources().getColor(...)
     *
     * @param colors
     */
    public void setColors(int[] colors, Context c) {

        ArrayList<Integer> clrs = new ArrayList<Integer>();

        for (int color : colors) {
            clrs.add(c.getResources().getColor(color));
        }

        mColors = clrs;
    }

    /**
     * Adds a new color to the colors array of the DataSet.
     *
     * @param color
     */
    public void addColor(int color) {
        if (mColors == null)
            mColors = new ArrayList<Integer>();
        mColors.add(color);
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet.
     * Internally, this recreates the colors array and adds the specified color.
     *
     * @param color
     */
    public void setColor(int color) {
        resetColors();
        mColors.add(color);
    }

    /**
     * returns all the colors that are set for this DataSet
     *
     * @return
     */
    public ArrayList<Integer> getColors() {
        return mColors;
    }

    /**
     * Returns the color at the given index of the DataSet's color array.
     * Performs a IndexOutOfBounds check by modulus.
     *
     * @param index
     * @return
     */
    public int getColor(int index) {
        return mColors.get(index % mColors.size());
    }

    /**
     * Returns the first color (index 0) of the colors-array this DataSet
     * contains.
     *
     * @return
     */
    public int getColor() {
        return mColors.get(0);
    }


    public boolean containsEntry(DecartEntry entry) {
        return mEntries.contains(entry);
    }

    /**
     * Resets all colors of this DataSet and recreates the colors array.
     */
    public void resetColors() {
        mColors = new ArrayList<Integer>();
    }

    public DecartDataSet<DecartEntry> copy() {

        for (int i = 0; i < mEntries.size(); i++) {
            DecartEntry copiedEntry = mEntries.get(i).copy();
            mEntries.add((T) copiedEntry);
        }

        DecartDataSet copied = new DecartDataSet(mEntries, getLabel());
        copied.mColors = mColors;
        copied.mShapeSize = mShapeSize;
        copied.mGraphShape = mGraphShape;
        copied.mCustomScatterPath = mCustomScatterPath;
        copied.mHighLightColor = mHighLightColor;

        return copied;
    }

    /**
     * Sets the size in density pixels the drawn scattershape will have. This
     * only applies for non custom shapes.
     *
     * @param size
     */
    public void setScatterShapeSize(float size) {
        mShapeSize = Utils.convertDpToPixel(size);
    }

    /**
     * returns the currently set scatter shape size
     *
     * @return
     */
    public float getScatterShapeSize() {
        return mShapeSize;
    }

    /**
     * Sets the shape that is drawn on the position where the values are at. If
     * "CUSTOM" is chosen, you need to call setCustomScatterShape(...) and
     * provide a path object that is drawn as the custom scattershape.
     *
     * @param shape
     */
    public void setScatterShape(DecartGraph.GraphShape shape) {
        mGraphShape = shape;
    }

    /**
     * returns all the different scattershapes the chart uses
     *
     * @return
     */
    public DecartGraph.GraphShape getScatterShape() {
        return mGraphShape;
    }

    /**
     * Sets a path object as the shape to be drawn where the values are at. Do
     * not forget to call setScatterShape(...) and set the shape to
     * ScatterShape.CUSTOM.
     *
     * @param shape
     */
    public void setCustomScatterShape(Path shape) {
        mCustomScatterPath = shape;
    }

    /**
     * returns the custom path / shape that is specified to be drawn where the
     * values are at
     *
     * @return
     */
    public Path getCustomScatterShape() {
        return mCustomScatterPath;
    }

    /**
     * Sets the color that is used for drawing the highlight indicators. Dont
     * forget to resolve the color using getResources().getColor(...) or
     * Color.rgb(...).
     *
     * @param color
     */
    public void setHighLightColor(int color) {
        mHighLightColor = color;
    }

    /**
     * Returns the color that is used for drawing the highlight indicators.
     *
     * @return
     */
    public int getHighLightColor() {
        return mHighLightColor;
    }


    public List<DecartEntry> getEntriesInRange(float x, float y, float round) {

        List<DecartEntry> entries = new ArrayList<DecartEntry>();

        for (DecartEntry entry : mEntries) {
            if (entry.getXVal() >= x - round && entry.getXVal() <= x + round
                    && entry.getYVal() >= y - round && entry.getYVal() <= y + round) {
                entries.add(entry);
            }
        }

        return entries;
    }

}
