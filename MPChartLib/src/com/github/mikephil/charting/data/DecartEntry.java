package com.github.mikephil.charting.data;

/**
 * Class representing one entry in the decart chart.
 *
 * @author Philipp Jahoda
 */
public class DecartEntry {

    /**
     * the y value
     */
    private float mYVal = 0f;

    /**
     * the x value
     */
    private float mXVal = 0f;

    /**
     * optional spot for additional data this Entry represents
     */
    private Object mData = null;

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param val    the y value (the actual value of the entry)
     * @param xIndex the corresponding index in the x value array (index on the
     *               x-axis of the chart, must NOT be higher than the length of the
     *               x-values String array)
     */
    public DecartEntry(float xCoord, float yCoord) {
        mXVal = xCoord;
        mYVal = yCoord;
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param val    the y value (the actual value of the entry)
     * @param xIndex the corresponding index in the x value array (index on the
     *               x-axis of the chart, must NOT be higher than the length of the
     *               x-values String array)
     * @param data   Spot for additional data this Entry represents.
     */
    public DecartEntry(float xCoord, float yCoord, Object data) {
        this(xCoord, yCoord);

        this.mData = data;
    }

    /**
     * Returns the total value the entry represents.
     *
     * @return
     */
    public float getYVal() {
        return mYVal;
    }

    /**
     * Sets the value for the entry.
     *
     * @param val
     */
    public void setYVal(float val) {
        this.mYVal = val;
    }

    /**
     * Returns the total value the entry represents.
     *
     * @return
     */
    public float getXVal() {
        return mXVal;
    }

    /**
     * Sets the value for the entry.
     *
     * @param val
     */
    public void setXVal(float val) {
        this.mXVal = val;
    }

    /**
     * Returns the data, additional information that this Entry represents, or
     * null, if no data has been specified.
     *
     * @return
     */
    public Object getData() {
        return mData;
    }

    /**
     * Sets additional data this Entry should represents.
     *
     * @param data
     */
    public void setData(Object data) {
        this.mData = data;
    }

    /**
     * returns an exact copy of the entry
     *
     * @return
     */
    public DecartEntry copy() {
        DecartEntry e = new DecartEntry(mXVal, mYVal, mData);
        return e;
    }

    /**
     * Compares value, xIndex and data of the entries. Returns true if entries
     * are equal, false if not.
     *
     * @param e
     * @return
     */
    public boolean equalTo(DecartEntry e) {

        if (e == null)
            return false;

        if (e.mData != this.mData)
            return false;

        if (Math.abs(e.mYVal - this.mYVal) > 0.00001f)
            return false;

        if (Math.abs(e.mXVal - this.mXVal) > 0.00001f)
            return false;

        return true;
    }

    public float distanceSq(DecartEntry otherDecartEntry) {
        float x1 = getXVal();
        float y1 = getYVal();
        float x2 = otherDecartEntry.getXVal();
        float y2 = otherDecartEntry.getYVal();
        return distanceSq(x1, y1, x2, y2);
    }

    public float distanceSq(float x2, float y2) {
        float x1 = getXVal();
        float y1 = getYVal();
        return distanceSq(x1, y1, x2, y2);
    }

    private float distanceSq(float x1, float y1, float x2, float y2) {
        x1 -= x2;
        y1 -= y2;
        return (x1 * x1 + y1 * y1);
    }

    /**
     * returns a string representation of the entry containing x-index and value
     */
    @Override
    public String toString() {
        return "Entry, xValue: " + mXVal + " yValue : " + getYVal();
    }
}
