
package com.github.mikephil.charting.utils;

/**
 * Class representing the x-axis labels settings.
 */
public class FXLabels extends LabelBase {

    /** the actual array of entries */
    public float[] mEntries = new float[] {};

    /**
     * width of the x-axis labels in pixels - this is calculated by the
     * calcTextWidth() method of the utils
     */
    public int mLabelWidth = 1;

    /**
     * height of the x-axis labels in pixels - this is calculated by the
     * calcTextHeight() method of the utils
     */
    public int mLabelHeight = 1;

    /** the number of entries the legend contains */
    public int mEntryCount;

    /** the number of decimal digits to use */
    public int mDecimals;

    /** the number of y-label entries the y-labels should have, default 6 */
    private int mLabelCount = 6;

    /**
     * if true, units are drawn next to the values of the y-axis labels
     */
    private boolean mDrawUnitsInLabels = true;

    /** if true, thousands ylabel values are separated by a dot */
    protected boolean mSeparateTousands = true;

    /** if true, the x-labels show only the minimum and maximum value */
    protected boolean mShowOnlyMinMax = false;

    /** the formatter used to customly format the y-labels */
    private ValueFormatter mFormatter = null;

    /**
     * sets the number of label entries for the y-axis max = 15, min = 2,
     * default: 6, be aware that this number is not fixed and can only be
     * approximated
     *
     * @param yCount
     */
    public void setLabelCount(int yCount) {

        if (yCount > 15)
            yCount = 15;
        if (yCount < 2)
            yCount = 2;

        mLabelCount = yCount;
    }

    /**
     * Returns the number of label entries the y-axis should have
     *
     * @return
     */
    public int getLabelCount() {
        return mLabelCount;
    }
    /**
     * the space that should be left out (in characters) between the x-axis
     * labels
     */
    private int mSpaceBetweenLabels = 4;
    /**
     * Set this to true to enable values above 1000 to be separated by a dot.
     *
     * @param enabled
     */
    public void setSeparateThousands(boolean enabled) {
        mSeparateTousands = enabled;
    }

    /**
     * Returns true if separating thousands is enabled, false if not.
     *
     * @return
     */
    public boolean isSeparateThousandsEnabled() {
        return mSeparateTousands;
    }

    /**
     * Returns the custom formatter used to format the YLabels.
     *
     * @return
     */
    public ValueFormatter getFormatter() {
        return mFormatter;
    }

    /**
     * Sets a custom formatter that will be used to format the YLabels.
     *
     * @param f
     */
    public void setFormatter(ValueFormatter f) {
        this.mFormatter = f;
    }

    /**
     * If enabled, the YLabels will only show the minimum and maximum value of
     * the chart. This will ignore/override the set label count.
     *
     * @param enabled
     */
    public void setShowOnlyMinMax(boolean enabled) {
        mShowOnlyMinMax = enabled;
    }

    /**
     * Returns true if showing only min and max value is enabled.
     *
     * @return
     */
    public boolean isShowOnlyMinMaxEnabled() {
        return mShowOnlyMinMax;
    }
    /**
     * Sets the space (in characters) that should be left out between the x-axis
     * labels, default 4
     *
     * @param space
     */
    public void setSpaceBetweenLabels(int space) {
        mSpaceBetweenLabels = space;
    }

    /**
     * Returns the space (in characters) that should be left out between the
     * x-axis labels
     *
     * @param space
     */
    public int getSpaceBetweenLabels() {
        return mSpaceBetweenLabels;
    }
    /**
     * Returns the longest formatted label (in terms of characters) the y-labels
     * contain.
     *
     * @return
     */
    public String getLongestLabel() {

        String longest = "";

        for (int i = 0; i < mEntries.length; i++) {
            String text = getFormattedLabel(i);

            if (longest.length() < text.length())
                longest = text;
        }

        return longest;
    }

    /**
     * Returns the formatted y-label at the specified index. This will either
     * use the auto-formatter or the custom formatter (if one is set).
     *
     * @param index
     * @return
     */
    public String getFormattedLabel(int index) {

        if (index < 0)
            return "";

        String text = null;

        // if there is no formatter
        if (getFormatter() == null)
            text = Utils.formatNumber(mEntries[index], mDecimals,
                    isSeparateThousandsEnabled());
        else
            text = getFormatter().getFormattedValue(mEntries[index]);

        return text;
    }

    /** the position of the x-labels relative to the chart */
    private XLabels.XLabelPosition mPosition = XLabels.XLabelPosition.TOP;

    public XLabels.XLabelPosition getPosition() {
        return mPosition;
    }

    /**
     * sets the position of the x-labels
     * 
     * @param pos
     */
    public void setPosition(XLabels.XLabelPosition pos) {
        mPosition = pos;
    }

}
