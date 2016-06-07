package com.github.mikephil.charting.charts;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.github.mikephil.charting.data.DecartData;
import com.github.mikephil.charting.data.DecartDataSet;
import com.github.mikephil.charting.data.DecartEntry;
import com.github.mikephil.charting.interfaces.DecartChartInterface;
import com.github.mikephil.charting.interfaces.OnChartGestureListener;
import com.github.mikephil.charting.interfaces.OnDecartGraphValueSelectedListener;
import com.github.mikephil.charting.interfaces.OnDrawListener;
import com.github.mikephil.charting.interfaces.OnZoomChangedListener;
import com.github.mikephil.charting.listener.DecartGraphTouchListener;
import com.github.mikephil.charting.renderer.DecartTransformer;
import com.github.mikephil.charting.utils.DecartHighlight;
import com.github.mikephil.charting.utils.DecartMarkerView;
import com.github.mikephil.charting.utils.FXLabels;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Legend.LegendPosition;
import com.github.mikephil.charting.utils.LimitLine;
import com.github.mikephil.charting.utils.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.utils.PointD;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.github.mikephil.charting.utils.XLabels.XLabelPosition;
import com.github.mikephil.charting.utils.YLabels;
import com.github.mikephil.charting.utils.YLabels.YLabelPosition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Base-class of DecartGraph.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("RtlHardcoded")
public abstract class DecartGraphBase<T extends DecartData> extends
        ViewGroup
        implements ValueAnimator.AnimatorUpdateListener, DecartChartInterface {
    public static final String LOG_TAG = "MPChart";

    /**
     * custom formatter that is used instead of the auto-formatter if set
     */
    protected ValueFormatter mValueFormatter = null;

    /**
     * flag that indicates if the default formatter should be used or if a
     * custom one is set
     */
    private boolean mUseDefaultFormatter = true;

    /**
     * flag that indicates if logging is enabled or not
     */
    protected boolean mLogEnabled = false;

    /**
     * chart offset to the left
     */
    protected float mOffsetLeft = 12;

    /**
     * chart toffset to the top
     */
    protected float mOffsetTop = 12;

    /**
     * chart offset to the right
     */
    protected float mOffsetRight = 12;

    /**
     * chart offset to the bottom
     */
    protected float mOffsetBottom = 12;

    /**
     * object that holds all data that was originally set for the chart, before
     * it was modified or any filtering algorithms had been applied
     */
    protected T mData = null;

    /**
     * the canvas that is used for drawing on the bitmap
     */
    protected Canvas mDrawCanvas;

    /**
     * the lowest y-value the chart can display
     */
    protected float mYChartMin = 0.0f;

    /**
     * the highest y-value the chart can display
     */
    protected float mYChartMax = 0.0f;

    /**
     * the lowest x-value the chart can display
     */
    protected float mXChartMin = 0.0f;

    /**
     * the highest x-value the chart can display
     */
    protected float mXChartMax = 0.0f;

    /**
     * paint for the x-label values
     */
    protected Paint mXLabelPaint;

    /**
     * paint for the y-label values
     */
    protected Paint mYLabelPaint;

    /**
     * paint used for highlighting values
     */
    protected Paint mHighlightPaint;

    /**
     * paint object used for drawing the description text in the bottom right
     * corner of the chart
     */
    protected Paint mDescPaint;

    /**
     * paint object for drawing the information text when there are no values in
     * the chart
     */
    protected Paint mInfoPaint;

    /**
     * paint object for drawing values (text representing values of chart
     * entries)
     */
    protected Paint mValuePaint;

    /**
     * this is the paint object used for drawing the data onto the chart
     */
    protected Paint mRenderPaint;

    /**
     * paint for the legend labels
     */
    protected Paint mLegendLabelPaint;

    /**
     * paint used for the legend forms
     */
    protected Paint mLegendFormPaint;

    /**
     * paint used for the limit lines
     */
    protected Paint mLimitLinePaint;

    /**
     * description text that appears in the bottom right corner of the chart
     */
    protected String mDescription = "Description";

    /**
     * flag that indicates if the chart has been fed with data yet
     */
    protected boolean mDataNotSet = true;

    /**
     * if true, units are drawn next to the values in the chart
     */
    protected boolean mDrawUnitInChart = false;

    /**
     * the range of y-values the chart displays
     */
    protected float mDeltaY = 1f;

    /**
     * the range of x-values the chart displays
     */
    protected float mDeltaX = 1f;

    /**
     * the offset of x-values that allow touch
     */
    protected float mTouchOffset = 1f;

    /**
     * the offset in dp that allow touch
     */
    protected float mTouchOffsetDp = 75;

    /**
     * the offset in pixels that allow touch
     */
    protected float mTouchOffsetPixels = 1f;

    /**
     * if true, touch gestures are enabled on the chart
     */
    protected boolean mTouchEnabled = true;

    /**
     * if true, y-values are drawn on the chart
     */
    protected boolean mDrawYValues = true;

    /**
     * if true, value highlightning is enabled
     */
    protected boolean mHighlightEnabled = true;

    /**
     * flag indicating if the legend is drawn of not
     */
    protected boolean mDrawLegend = true;

    /**
     * this rectangle defines the area in which graph values can be drawn
     */
    protected RectF mContentRect = new RectF();

    /**
     * the legend object containing all data associated with the legend
     */
    protected Legend mLegend;

    /**
     * Transformer object used to transform values to pixels and the other way
     * around
     */
    protected DecartTransformer mTrans;

    /**
     * listener that is called when a value on the chart is selected
     */
    protected OnDecartGraphValueSelectedListener mSelectionListener;

    /**
     * listener that is called when a scale value is changed
     */
    protected OnZoomChangedListener mOnZoomChangedListener;

    /**
     * text that is displayed when the chart is empty
     */
    private String mNoDataText = "No chart data available.";

    /**
     * Gesture listener for custom callbacks when making gestures on the chart.
     */
    private OnChartGestureListener mGestureListener;

    /**
     * text that is displayed when the chart is empty that describes why the
     * chart is empty
     */
    private String mNoDataTextDescription;


    /**
     * the maximum number of entried to which values will be drawn
     */
    protected int mMaxVisibleCount = 1000;

    /**
     * the width of the grid lines
     */
    protected float mGridWidth = 1f;

    /**
     * flag that indicates if pinch-zoom is enabled. if true, both x and y axis
     * can be scaled with 2 fingers, if false, x and y axis can be scaled
     * separately
     */
    protected boolean mPinchZoomEnabled = true;

    /**
     * flat that indicates if double tap zoom is enabled or not
     */
    protected boolean mDoubleTapToZoomEnabled = true;

    /**
     * if true, dragging is enabled for the chart
     */
    private boolean mDragEnabled = true;

    /**
     * if true, scaling is enabled for the chart
     */
    private boolean mScaleEnabled = true;

    /**
     * if true, the y range is predefined
     */
    protected boolean mFixedYValues = false;

    /**
     * if true, the y-label entries will always start at zero
     */
    protected boolean mStartAtZero = true;

    /**
     * paint object for the grid lines
     */
    protected Paint mGridPaint;

    /**
     * paint object for the (by default) lightgrey background of the grid
     */
    protected Paint mGridBackgroundPaint;

    /**
     * paint for the line surrounding the chart
     */
    protected Paint mBorderPaint;

    /**
     * if set to true, the highlight indicator (lines for linechart, dark bar
     * for barchart) will be drawn upon selecting values.
     */
    protected boolean mHighLightIndicatorEnabled = true;

    /**
     * flag indicating if the vertical grid should be drawn or not
     */
    protected boolean mDrawVerticalGrid = true;

    /**
     * flag indicating if the horizontal grid should be drawn or not
     */
    protected boolean mDrawHorizontalGrid = true;

    /**
     * flag indicating if the y-labels should be drawn or not
     */
    protected boolean mDrawYLabels = true;

    /**
     * flag indicating if the x-labels should be drawn or not
     */
    protected boolean mDrawXLabels = true;

    /**
     * flag indicating if the chart border rectangle should be drawn or not
     */
    protected boolean mDrawBorder = true;

    /**
     * flag indicating if the grid background should be drawn or not
     */
    protected boolean mDrawGridBackground = true;

    /**
     * the listener for user drawing on the chart
     */
    protected OnDrawListener mDrawListener;

    /**
     * the object representing the labels on thexy-axis, this object is prepared
     * in the pepareXLabels() method
     */
    protected FXLabels mXLabels = new FXLabels();

    /**
     * the object representing the labels on the y-axis, this object is prepared
     * in the pepareYLabels() method
     */
    protected YLabels mYLabels = new YLabels();

    /**
     * flag that indicates if offsets calculation has already been done or not
     */
    private boolean mOffsetsCalculated = false;

    /**
     * Bitmap object used for drawing. This is necessary because hardware
     * acceleration uses OpenGL which only allows a specific texture size to be
     * drawn on the canvas directly.
     */
    protected Bitmap mDrawBitmap;

    /**
     * paint object used for drawing the bitmap
     */
    protected Paint mDrawPaint;

    /**
     * default constructor for initialization in code
     */
    public DecartGraphBase(Context context) {
        super(context);
        init();
    }

    /**
     * constructor for initialization in xml
     */
    public DecartGraphBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * even more awesome constructor
     */
    public DecartGraphBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize all paints and stuff
     */
    protected void init() {
        Utils.init(getContext().getResources());
        setWillNotDraw(false);

        mTrans = new DecartTransformer();
        mListener = new DecartGraphTouchListener(this, mTrans.getTouchMatrix());
        // initialize the utils
        // do screen density conversions
        mOffsetBottom = (int) Utils.convertDpToPixel(mOffsetBottom);
        mOffsetLeft = (int) Utils.convertDpToPixel(mOffsetLeft);
        mOffsetRight = (int) Utils.convertDpToPixel(mOffsetRight);
        mOffsetTop = (int) Utils.convertDpToPixel(mOffsetTop);

        mRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRenderPaint.setStyle(Style.FILL);

        mDescPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDescPaint.setColor(Color.BLACK);
        mDescPaint.setTextAlign(Align.RIGHT);
        mDescPaint.setTextSize(Utils.convertDpToPixel(9f));

        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(Color.rgb(247, 189, 51)); // orange
        mInfoPaint.setTextAlign(Align.CENTER);
        mInfoPaint.setTextSize(Utils.convertDpToPixel(12f));

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setColor(Color.rgb(63, 63, 63));
        mValuePaint.setTextAlign(Align.CENTER);
        mValuePaint.setTextSize(Utils.convertDpToPixel(9f));

        mLegendFormPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendFormPaint.setStyle(Paint.Style.FILL);
        mLegendFormPaint.setStrokeWidth(3f);

        mLegendLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendLabelPaint.setTextSize(Utils.convertDpToPixel(9f));

        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setColor(Color.rgb(255, 187, 115));

        mXLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLabelPaint.setColor(Color.BLACK);
        mXLabelPaint.setTextAlign(Align.CENTER);
        mXLabelPaint.setTextSize(Utils.convertDpToPixel(10f));

        mYLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mYLabelPaint.setColor(Color.BLACK);
        mYLabelPaint.setTextSize(Utils.convertDpToPixel(10f));

        mLimitLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLimitLinePaint.setStyle(Paint.Style.STROKE);

        mDrawPaint = new Paint(Paint.DITHER_FLAG);

        mGridPaint = new Paint();
        mGridPaint.setColor(Color.GRAY);
        mGridPaint.setStrokeWidth(mGridWidth);
        mGridPaint.setStyle(Style.STROKE);
        mGridPaint.setAlpha(90);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.BLACK);
        mBorderPaint.setStrokeWidth(mGridWidth * 2f);
        mBorderPaint.setStyle(Style.STROKE);

        mGridBackgroundPaint = new Paint();
        mGridBackgroundPaint.setStyle(Style.FILL);
        mGridBackgroundPaint.setColor(Color.rgb(240, 240, 240));

        mTouchOffsetPixels = Utils.convertDpToPixel(mTouchOffsetDp);
    }

    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     *
     * @param data
     */
    public void setData(T data) {


        if (data == null) {
            Log.e(LOG_TAG,
                    "Cannot set data for chart. Provided data object is null.");
            return;
        }

        mDataNotSet = false;
        mOffsetsCalculated = false;
        mData = data;
        mData = data;
        mIndicesToHightlight = null;

        prepare();

        // calculate how many digits are needed
        calcFormats();

        Log.i(LOG_TAG, "Data is set.");
    }

    public T getmData() {
        return mData;
    }

    /**
     * Clears the chart from all data and refreshes it (by calling
     * invalidate()).
     */
    public void clear() {
        mData = null;
        mData = null;
        mDataNotSet = true;
        invalidate();
    }

    /**
     * Returns true if the chart is empty (meaning it's data object is either
     * null or contains no entries).
     *
     * @return
     */
    public boolean isEmpty() {

        if (mData == null)
            return true;
        else {

            if (mData.getEntriesCount() <= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * calcualtes the y-min and y-max value and the y-delta and x-delta value
     */
    protected void calcMinMax(boolean fixedValues) {
        // only calculate values if not fixed values
        if (!fixedValues) {
            mYChartMin = mData.getYMin();
            mYChartMax = mData.getYMax();
            mXChartMin = mData.getXMin();
            mXChartMax = mData.getXMax();
        }

        // calc delta
        mDeltaY = Math.abs(mYChartMax - mYChartMin);
        mDeltaX = Math.abs(mXChartMax - mXChartMin);

        if (!fixedValues) {

            // additional handling for space (default 15% space)
            // float space = Math.abs(mDeltaY / 100f * 15f);
            float space = Math
                    .abs(Math.abs(Math.max(Math.abs(mYChartMax), Math.abs(mYChartMin))) / 100f * 20f);

            if (Math.abs(mYChartMax - mYChartMin) < 0.00001f) {
                if (Math.abs(mYChartMax) < 10f)
                    space = 1f;
                else
                    space = Math.abs(mYChartMax / 100f * 20f);
            }

            if (mStartAtZero) {

                if (mYChartMax < 0) {
                    mYChartMax = 0;
                    // calc delta
                    mYChartMin = mYChartMin - space;
                } else {
                    mYChartMin = 0;
                    // calc delta
                    mYChartMax = mYChartMax + space;
                }
            } else {

                mYChartMin = mYChartMin - space / 2f;
                mYChartMax = mYChartMax + space / 2f;
            }

            space = Math
                    .abs(Math.abs(Math.max(Math.abs(mXChartMax), Math.abs(mXChartMin))) / 100f * 50f);

            if (Math.abs(mXChartMax - mXChartMin) < 0.00001f) {
                if (Math.abs(mXChartMax) < 10f)
                    space = 1f;
                else
                    space = Math.abs(mXChartMax / 100f * 20f);
            }

            if (mStartAtZero) {

                if (mXChartMax < 0) {
                    mXChartMax = 0;
                    // calc delta
                    mXChartMin = mXChartMin - space;
                } else {
                    mXChartMin = 0;
                    // calc delta
                    mXChartMax = mXChartMax + space;
                }
            } else {

                mXChartMin = mXChartMin - space / 2f;
                mXChartMax = mXChartMax + space / 2f;
            }
        }

        mDeltaY = Math.abs(mYChartMax - mYChartMin);
        mDeltaX = Math.abs(mXChartMax - mXChartMin);
        mTouchOffset = mDeltaX * mTouchOffsetPixels / (getMeasuredWidth() - mOffsetLeft - mOffsetRight);

    }

    /**
     * calculates the required number of digits for the values that might be
     * drawn in the chart (if enabled)
     */
    protected void calcFormats() {

        // check if a custom formatter is set or not
        if (mUseDefaultFormatter) {

            float reference = 0f;

            if (mData == null || mData.getEntriesCount() < 2) {

                reference = Math.max(Math.abs(mYChartMin), Math.abs(mYChartMax));
            } else {
                reference = mDeltaY;
            }

            int digits = Utils.getDecimals(reference);

            StringBuffer b = new StringBuffer();
            for (int i = 0; i < digits; i++) {
                if (i == 0)
                    b.append(".");
                b.append("0");
            }

            DecimalFormat formatter = new DecimalFormat("###,###,###,##0" + b.toString());
            mValueFormatter = new DefaultValueFormatter(formatter);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataNotSet) { // check if there is data

            canvas.drawText(mNoDataText, getWidth() / 2, getHeight() / 2, mInfoPaint);

            if (!TextUtils.isEmpty(mNoDataTextDescription)) {
                float textOffset = -mInfoPaint.ascent() + mInfoPaint.descent();
                canvas.drawText(mNoDataTextDescription, getWidth() / 2, (getHeight() / 2)
                        + textOffset, mInfoPaint);
            }
            return;
        }

        if (!mOffsetsCalculated) {

            calculateOffsets();
            mOffsetsCalculated = true;
        }

        if (mDrawBitmap == null || mDrawCanvas == null) {

            mDrawBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                    Bitmap.Config.ARGB_4444);
            mDrawCanvas = new Canvas(mDrawBitmap);
        }

        // clear everything
        mDrawBitmap.eraseColor(Color.TRANSPARENT);

        if (mDataNotSet)
            return;

        long starttime = System.currentTimeMillis();

        mData = getData();

        // execute all drawing commands
        drawGridBackground();

        prepareYLabels();
        prepareXLabels();

        // make sure the graph values and grid cannot be drawn outside the
        // content-rect
        int clipRestoreCount = mDrawCanvas.save();
        mDrawCanvas.clipRect(mContentRect);

        drawHorizontalGrid();

        drawVerticalGrid();

        // if highlighting is enabled
        if (mHighlightEnabled && mHighLightIndicatorEnabled && valuesToHighlight())
            drawHighlights();

        drawData();

        drawLimitLines();

        // Removes clipping rectangle
        mDrawCanvas.restoreToCount(clipRestoreCount);

        drawAdditional();

        drawXLabels();

        drawYLabels();

        drawValues();

        drawLegend();

        drawBorder();

        drawMarkers();

        drawDescription();

        canvas.drawBitmap(mDrawBitmap, 0, 0, mDrawPaint);

        if (mLogEnabled)
            Log.i(LOG_TAG, "DrawTime: " + (System.currentTimeMillis() - starttime) + " ms");
    }

    /**
     * sets up the content rect that restricts the chart surface
     */
    protected void prepareContentRect() {

        mContentRect.set(mOffsetLeft,
                mOffsetTop,
                getWidth() - mOffsetRight,
                getHeight() - mOffsetBottom);
    }

    /**
     * Generates an automatically prepared legend depending on the DataSets in
     * the chart and their colors.
     */
    public void prepareLegend() {

        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Integer> colors = new ArrayList<Integer>();

        // loop for building up the colors and labels used in the legend
        for (int i = 0; i < mData.getDataSetCount(); i++) {

            DecartDataSet<? extends DecartEntry> dataSet = mData.getDataSetByIndex(i);

            ArrayList<Integer> clrs = dataSet.getColors();
            int entryCount = dataSet.getEntryCount();

            // if we have a barchart with stacked bars
            for (int j = 0; j < clrs.size() && j < entryCount; j++) {

                // if multiple colors are set for a DataSet, group them
                if (j < clrs.size() - 1 && j < entryCount - 1) {

                    labels.add(null);
                } else { // add label to the last entry

                    String label = mData.getDataSetByIndex(i).getLabel();
                    labels.add(label);
                }

                colors.add(clrs.get(j));
            }
        }

        Legend l = new Legend(colors, labels);

        if (mLegend != null) {
            // apply the old legend settings to a potential new legend
            l.apply(mLegend);
        }

        mLegend = l;
    }

    /**
     * draws the legend
     */
    protected void drawLegend() {

        if (!mDrawLegend || mLegend == null || mLegend.getPosition() == LegendPosition.NONE)
            return;

        String[] labels = mLegend.getLegendLabels();
        Typeface tf = mLegend.getTypeface();

        if (tf != null)
            mLegendLabelPaint.setTypeface(tf);

        mLegendLabelPaint.setTextSize(mLegend.getTextSize());
        mLegendLabelPaint.setColor(mLegend.getTextColor());

        float formSize = mLegend.getFormSize();

        // space between text and shape/form of entry
        float formTextSpaceAndForm = mLegend.getFormToTextSpace() + formSize;

        // space between the entries
        float stackSpace = mLegend.getStackSpace();

        float textSize = mLegend.getTextSize();

        // the amount of pixels the text needs to be set down to be on the same
        // height as the form
        float textDrop = (Utils.calcTextHeight(mLegendLabelPaint, "AQJ") + formSize) / 2f;

        float posX, posY;

        // contains the stacked legend size in pixels
        float stack = 0f;

        boolean wasStacked = false;

        switch (mLegend.getPosition()) {
            case BELOW_CHART_LEFT:

                posX = mLegend.getOffsetLeft();
                posY = getHeight() - mLegend.getOffsetBottom() / 2f - formSize / 2f;

                for (int i = 0; i < labels.length; i++) {

                    mLegend.drawForm(mDrawCanvas, posX, posY, mLegendFormPaint, i);

                    // grouped forms have null labels
                    if (labels[i] != null) {

                        // make a step to the left
                        if (mLegend.getColors()[i] != -2)
                            posX += formTextSpaceAndForm;

                        mLegend.drawLabel(mDrawCanvas, posX, posY + textDrop, mLegendLabelPaint, i);
                        posX += Utils.calcTextWidth(mLegendLabelPaint, labels[i])
                                + mLegend.getXEntrySpace();
                    } else {
                        posX += formSize + stackSpace;
                    }
                }

                break;
            case BELOW_CHART_RIGHT:

                posX = getWidth() - getOffsetRight();
                posY = getHeight() - mLegend.getOffsetBottom() / 2f - formSize / 2f;

                for (int i = labels.length - 1; i >= 0; i--) {

                    if (labels[i] != null) {

                        posX -= Utils.calcTextWidth(mLegendLabelPaint, labels[i])
                                + mLegend.getXEntrySpace();
                        mLegend.drawLabel(mDrawCanvas, posX, posY + textDrop, mLegendLabelPaint, i);
                        if (mLegend.getColors()[i] != -2)
                            posX -= formTextSpaceAndForm;
                    } else {
                        posX -= stackSpace + formSize;
                    }

                    mLegend.drawForm(mDrawCanvas, posX, posY, mLegendFormPaint, i);
                }

                break;
            case RIGHT_OF_CHART:

                posX = getWidth() - mLegend.getMaximumEntryLength(mLegendLabelPaint)
                        - formTextSpaceAndForm;
                posY = mLegend.getOffsetTop();

                for (int i = 0; i < labels.length; i++) {

                    mLegend.drawForm(mDrawCanvas, posX + stack, posY, mLegendFormPaint, i);

                    if (labels[i] != null) {

                        if (!wasStacked) {

                            float x = posX;

                            if (mLegend.getColors()[i] != -2)
                                x += formTextSpaceAndForm;

                            posY += textDrop;

                            mLegend.drawLabel(mDrawCanvas, x, posY,
                                    mLegendLabelPaint, i);
                        } else {

                            posY += textSize * 1.2f + formSize;

                            mLegend.drawLabel(mDrawCanvas, posX, posY,
                                    mLegendLabelPaint, i);

                        }

                        // make a step down
                        posY += mLegend.getYEntrySpace();
                        stack = 0f;
                    } else {
                        stack += formSize + stackSpace;
                        wasStacked = true;
                    }
                }
                break;
            case RIGHT_OF_CHART_CENTER:
                posX = getWidth() - mLegend.getMaximumEntryLength(mLegendLabelPaint)
                        - formTextSpaceAndForm;
                posY = getHeight() / 2f - mLegend.getFullHeight(mLegendLabelPaint) / 2f;

                for (int i = 0; i < labels.length; i++) {

                    mLegend.drawForm(mDrawCanvas, posX + stack, posY, mLegendFormPaint, i);

                    if (labels[i] != null) {

                        if (!wasStacked) {

                            float x = posX;

                            if (mLegend.getColors()[i] != -2)
                                x += formTextSpaceAndForm;

                            posY += textDrop;

                            mLegend.drawLabel(mDrawCanvas, x, posY,
                                    mLegendLabelPaint, i);
                        } else {

                            posY += textSize * 1.2f + formSize;

                            mLegend.drawLabel(mDrawCanvas, posX, posY,
                                    mLegendLabelPaint, i);

                        }

                        // make a step down
                        posY += mLegend.getYEntrySpace();
                        stack = 0f;
                    } else {
                        stack += formSize + stackSpace;
                        wasStacked = true;
                    }
                }

                break;
            case BELOW_CHART_CENTER:

                float fullSize = mLegend.getFullWidth(mLegendLabelPaint);

                posX = getWidth() / 2f - fullSize / 2f;
                posY = getHeight() - mLegend.getOffsetBottom() / 2f - formSize / 2f;

                for (int i = 0; i < labels.length; i++) {

                    mLegend.drawForm(mDrawCanvas, posX, posY, mLegendFormPaint, i);

                    // grouped forms have null labels
                    if (labels[i] != null) {

                        // make a step to the left
                        if (mLegend.getColors()[i] != -2)
                            posX += formTextSpaceAndForm;

                        mLegend.drawLabel(mDrawCanvas, posX, posY + textDrop, mLegendLabelPaint, i);
                        posX += Utils.calcTextWidth(mLegendLabelPaint, labels[i])
                                + mLegend.getXEntrySpace();
                    } else {
                        posX += formSize + stackSpace;
                    }
                }

                Log.i(LOG_TAG, "content bottom: " + mContentRect.bottom + ", height: "
                        + getHeight() + ", posY: " + posY + ", formSize: " + formSize);

                break;
            case PIECHART_CENTER:

                posX = getWidth()
                        / 2f
                        - (mLegend.getMaximumEntryLength(mLegendLabelPaint) + mLegend
                        .getXEntrySpace())
                        / 2f;
                posY = getHeight() / 2f - mLegend.getFullHeight(mLegendLabelPaint) / 2f;

                for (int i = 0; i < labels.length; i++) {

                    mLegend.drawForm(mDrawCanvas, posX + stack, posY, mLegendFormPaint, i);

                    if (labels[i] != null) {

                        if (!wasStacked) {

                            float x = posX;

                            if (mLegend.getColors()[i] != -2)
                                x += formTextSpaceAndForm;

                            posY += textDrop;

                            mLegend.drawLabel(mDrawCanvas, x, posY,
                                    mLegendLabelPaint, i);
                        } else {

                            posY += textSize * 1.2f + formSize;

                            mLegend.drawLabel(mDrawCanvas, posX, posY,
                                    mLegendLabelPaint, i);

                        }

                        // make a step down
                        posY += mLegend.getYEntrySpace();
                        stack = 0f;
                    } else {
                        stack += formSize + stackSpace;
                        wasStacked = true;
                    }
                }

                break;
            case RIGHT_OF_CHART_INSIDE:

                posX = getWidth() - mLegend.getMaximumEntryLength(mLegendLabelPaint)
                        - formTextSpaceAndForm;
                posY = mLegend.getOffsetTop();

                for (int i = 0; i < labels.length; i++) {

                    mLegend.drawForm(mDrawCanvas, posX + stack, posY, mLegendFormPaint, i);

                    if (labels[i] != null) {

                        if (!wasStacked) {

                            float x = posX;

                            if (mLegend.getColors()[i] != -2)
                                x += formTextSpaceAndForm;

                            posY += textDrop;

                            mLegend.drawLabel(mDrawCanvas, x, posY,
                                    mLegendLabelPaint, i);
                        } else {

                            posY += textSize * 1.2f + formSize;

                            mLegend.drawLabel(mDrawCanvas, posX, posY,
                                    mLegendLabelPaint, i);

                        }

                        // make a step down
                        posY += mLegend.getYEntrySpace();
                        stack = 0f;
                    } else {
                        stack += formSize + stackSpace;
                        wasStacked = true;
                    }
                }
                break;
            case NONE:
                break;
        }
    }

    /**
     * draws the description text in the bottom right corner of the chart
     */
    protected void drawDescription() {

        mDrawCanvas
                .drawText(mDescription, getWidth() - mOffsetRight - 10, getHeight() - mOffsetBottom
                        - 10, mDescPaint);
    }

    /**
     * draws all the text-values to the chart
     */
    protected abstract void drawValues();

    /**
     * draws the actual data
     */
    protected abstract void drawData();

    /**
     * draws additional stuff, whatever that might be
     */
    protected abstract void drawAdditional();

    /**
     * draws the values of the chart that need highlightning
     */
    protected abstract void drawHighlights();

    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS CODE FOR HIGHLIGHTING */

    /**
     * array of Highlight objects that reference the highlighted slices in the
     * chart
     */
    protected DecartHighlight[] mIndicesToHightlight = new DecartHighlight[0];

    /**
     * Returns true if there are values to highlight, false if there are no
     * values to highlight. Checks if the highlight array is null, has a length
     * of zero or if the first object is null.
     *
     * @return
     */
    public boolean valuesToHighlight() {
        return mIndicesToHightlight == null || mIndicesToHightlight.length <= 0
                || mIndicesToHightlight[0] == null ? false
                : true;
    }

    /**
     * Highlights the values at the given indices in the given DataSets. Provide
     * null or an empty array to undo all highlighting. This should be used to
     * programmatically highlight values. This DOES NOT generate a callback to
     * the OnChartValueSelectedListener.
     *
     * @param highs
     */
    public void highlightValues(DecartHighlight[] highs) {

        // set the indices to highlight
        mIndicesToHightlight = highs;

        // redraw the chart
        invalidate();
    }

    /**
     * Highlights the value at the given x-index in the given DataSet. Provide
     * -1 as the x-index to undo all highlighting.
     *
     * @param xIndex
     * @param dataSetIndex
     */
    public void highlightValue(int xIndex, int dataSetIndex) {
        throw new RuntimeException("todo");
//        if (xIndex < 0 || dataSetIndex < 0 || xIndex >= mData.getXValCount()
//                || dataSetIndex >= mData.getDataSetCount()) {
//
//            highlightValues(null);
//        } else {
//            highlightValues(new Highlight[]{
//                    new Highlight(xIndex, dataSetIndex)
//            });
//        }
    }

    /**
     * Highlights the value selected by touch gesture. Unlike
     * highlightValues(...), this generates a callback to the
     * OnChartValueSelectedListener.
     *
     * @param highs
     */
    public void highlightTouch(DecartHighlight highlight) {
        if (highlight == null)
            mIndicesToHightlight = null;
        else {

            // set the indices to highlight
            mIndicesToHightlight = new DecartHighlight[]{
                    highlight
            };
        }

        // redraw the chart
        invalidate();

        if (mSelectionListener != null) {

            if (!valuesToHighlight())
                mSelectionListener.onNothingSelected();
            else {

                // notify the listener
                mSelectionListener.onValueSelected(highlight.getDecartEntry());
            }
        }
    }

    public void highlightLongTap(DecartHighlight highlight) {
        if (highlight == null)
            mIndicesToHightlight = null;
        else {

            // set the indices to highlight
            mIndicesToHightlight = new DecartHighlight[]{
                    highlight
            };
        }

        // redraw the chart
        invalidate();

        if (mSelectionListener != null) {

            if (!valuesToHighlight())
                mSelectionListener.onNothingSelected();
            else {

                // notify the listener
                mSelectionListener.onValueLongPress(highlight.getDecartEntry());
            }
        }
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW CODE IS FOR THE MARKER VIEW */

    /**
     * if set to true, the marker view is drawn when a value is clicked
     */
    protected boolean mDrawMarkerViews = true;

    /**
     * the view that represents the marker
     */
    protected DecartMarkerView mMarkerView;

    /**
     * draws all MarkerViews on the highlighted positions
     */
    protected void drawMarkers() {
        if (mMarkerView == null || !mDrawMarkerViews || !valuesToHighlight())
            return;

        for (int i = 0; i < mIndicesToHightlight.length; i++) {

            DecartEntry decartEntry = mIndicesToHightlight[i].getDecartEntry();

            // make sure entry not null
            if (decartEntry == null)
                continue;

            float[] pos = getMarkerPosition(decartEntry);

            // check bounds
            if (pos[0] < mOffsetLeft || pos[0] > getWidth() - mOffsetRight
                    || pos[1] < mOffsetTop || pos[1] > getHeight() - mOffsetBottom)
                continue;

            // callbacks to update the content
            mMarkerView.refreshContent(decartEntry);

            mMarkerView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mMarkerView.layout(0, 0, mMarkerView.getMeasuredWidth(),
                    mMarkerView.getMeasuredHeight());

            if (pos[1] - mMarkerView.getHeight() <= 0) {
                float y = mMarkerView.getHeight() - pos[1];
                mMarkerView.draw(mDrawCanvas, pos[0], pos[1] + y);
            } else {
                mMarkerView.draw(mDrawCanvas, pos[0], pos[1]);
            }
        }
    }

    /**
     * Returns the actual position in pixels of the MarkerView for the given
     * Entry in the given DataSet.
     *
     * @param xIndex
     * @param dataSetIndex
     * @return
     */
    private float[] getMarkerPosition(DecartEntry entry) {

        float[] pts = new float[]{
                entry.getXVal(), entry.getYVal() * mPhaseY
        };

        mTrans.pointValuesToPixel(pts);

        return pts;
    }

    /**
     * ################ ################ ################ ################
     * Animation support below Honeycomb thanks to Jake Wharton's awesome
     * nineoldandroids library: https://github.com/JakeWharton/NineOldAndroids
     */
    /** CODE BELOW THIS RELATED TO ANIMATION */

    /**
     * the phase that is animated and influences the drawn values on the y-axis
     */
    protected float mPhaseY = 1f;

    /**
     * the phase that is animated and influences the drawn values on the x-axis
     */
    protected float mPhaseX = 1f;

    /**
     * objectanimator used for animating values on y-axis
     */
    private ObjectAnimator mAnimatorY;

    /**
     * objectanimator used for animating values on x-axis
     */
    private ObjectAnimator mAnimatorX;

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart.
     *
     * @param durationMillisX
     * @param durationMillisY
     */
    public void animateXY(int durationMillisX, int durationMillisY) {

        mAnimatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f);
        mAnimatorY.setDuration(
                durationMillisY);
        mAnimatorX = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f);
        mAnimatorX.setDuration(
                durationMillisX);

        // make sure only one animator produces update-callbacks (which then
        // call invalidate())
        if (durationMillisX > durationMillisY) {
            mAnimatorX.addUpdateListener(this);
        } else {
            mAnimatorY.addUpdateListener(this);
        }

        mAnimatorX.start();
        mAnimatorY.start();
    }

    /**
     * Animates the rendering of the chart on the x-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart.
     *
     * @param durationMillis
     */
    public void animateX(int durationMillis) {

        mAnimatorX = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f);
        mAnimatorX.setDuration(durationMillis);
        mAnimatorX.addUpdateListener(this);
        mAnimatorX.start();
    }

    /**
     * Animates the rendering of the chart on the y-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart.
     *
     * @param durationMillis
     */
    public void animateY(int durationMillis) {

        mAnimatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f);
        mAnimatorY.setDuration(durationMillis);
        mAnimatorY.addUpdateListener(this);
        mAnimatorY.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator va) {

        // redraw everything after animation value change
        invalidate();

        // Log.i(LOG_TAG, "UPDATING, x: " + mPhaseX + ", y: " + mPhaseY);
    }

    /**
     * This gets the y-phase that is used to animate the values.
     *
     * @return
     */
    public float getPhaseY() {
        return mPhaseY;
    }

    /**
     * This modifys the y-phase that is used to animate the values.
     *
     * @param phase
     */
    public void setPhaseY(float phase) {
        mPhaseY = phase;
    }

    /**
     * This gets the x-phase that is used to animate the values.
     *
     * @return
     */
    public float getPhaseX() {
        return mPhaseX;
    }

    /**
     * This modifys the x-phase that is used to animate the values.
     *
     * @param phase
     */
    public void setPhaseX(float phase) {
        mPhaseX = phase;
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS ONLY GETTERS AND SETTERS */

    /**
     * Returns the canvas object the chart uses for drawing.
     *
     * @return
     */
    public Canvas getCanvas() {
        return mDrawCanvas;
    }

    /**
     * set a selection listener for the chart
     *
     * @param l
     */
    public void setOnChartValueSelectedListener(OnDecartGraphValueSelectedListener l) {
        this.mSelectionListener = l;
    }


    /**
     * set a zoom listener for the chart
     *
     * @param l
     */
    public void setOnZoomChangedListener(OnZoomChangedListener l) {
        this.mOnZoomChangedListener = l;
    }


    /**
     * Sets a gesture-listener for the chart for custom callbacks when executing
     * gestures on the chart surface.
     *
     * @param l
     */
    public void setOnChartGestureListener(OnChartGestureListener l) {
        this.mGestureListener = l;
    }

    /**
     * Returns the custom gesture listener.
     *
     * @return
     */
    public OnChartGestureListener getOnChartGestureListener() {
        return mGestureListener;
    }

    /**
     * If set to true, value highlighting is enabled which means that values can
     * be highlighted programmatically or by touch gesture.
     *
     * @param enabled
     */
    public void setHighlightEnabled(boolean enabled) {
        mHighlightEnabled = enabled;
    }

    /**
     * returns true if highlighting of values is enabled, false if not
     *
     * @return
     */
    public boolean isHighlightEnabled() {
        return mHighlightEnabled;
    }

    /**
     * returns the total value (sum) of all y-values across all DataSets
     *
     * @return
     */
    public float getYValueSum() {
        return mData.getYValueSum();
    }

    /**
     * returns the current y-max value across all DataSets
     *
     * @return
     */
    public float getYMax() {
        return mData.getYMax();
    }

    /**
     * returns the lowest value the chart can display
     *
     * @return
     */
    @Override
    public float getYChartMin() {
        return mYChartMin;
    }

    /**
     * returns the highest value the chart can display
     *
     * @return
     */
    @Override
    public float getYChartMax() {
        return mYChartMax;
    }

    /**
     * returns the lowest value the chart can display
     *
     * @return
     */
    public float getXChartMin() {
        return mXChartMin;
    }

    /**
     * returns the highest value the chart can display
     *
     * @return
     */
    public float getXChartMax() {
        return mXChartMax;
    }


    /**
     * returns the current y-min value across all DataSets
     *
     * @return
     */
    public float getYMin() {
        return mData.getYMin();
    }

    /**
     * Get the total number of X-values.
     *
     * @return
     */
    @Override
    public float getDeltaX() {
        return mDeltaX;
    }

    /**
     * Returns the total range of values (on y-axis) the chart displays.
     *
     * @return
     */
    @Override
    public float getDeltaY() {
        return mDeltaY;
    }

    /**
     * returns the average value of all values the chart holds
     *
     * @return
     */
    public float getAverage() {
        return getYValueSum() / mData.getEntriesCount();
    }

    /**
     * returns the average value for a specific DataSet (with a specific label)
     * in the chart
     *
     * @param dataSetLabel
     * @return
     */
    public float getAverage(String dataSetLabel) {

        DecartDataSet<? extends DecartEntry> ds = mData.getDataSetByLabel(dataSetLabel, true);

        return ds.getYValueSum()
                / ds.getEntryCount();
    }

    /**
     * returns the total number of values the chart holds (across all DataSets)
     *
     * @return
     */
    public int getEntriesCount() {
        return mData.getEntriesCount();
    }

    /**
     * Returns the center point of the chart (the whole View) in pixels.
     *
     * @return
     */
    public PointF getCenter() {
        return new PointF(getWidth() / 2f, getHeight() / 2f);
    }

    /**
     * Returns the center of the chart taking offsets under consideration.
     * (returns the center of the content rectangle)
     *
     * @return
     */
    public PointF getCenterOffsets() {
        return new PointF(mContentRect.centerX(), mContentRect.centerY());
    }

    /**
     * sets the size of the description text in pixels, min 6f, max 16f
     *
     * @param size
     */
    public void setDescriptionTextSize(float size) {

        if (size > 16f)
            size = 16f;
        if (size < 6f)
            size = 6f;

        mDescPaint.setTextSize(Utils.convertDpToPixel(size));
    }

    /**
     * Set this to true to enable logcat outputs for the chart. Default:
     * disabled
     *
     * @param enabled
     */
    public void setLogEnabled(boolean enabled) {
        mLogEnabled = enabled;
    }

    /**
     * set a description text that appears in the bottom right corner of the
     * chart, size = Y-legend text size
     *
     * @param desc
     */
    public void setDescription(String desc) {
        if (desc == null)
            desc = "";
        this.mDescription = desc;
    }

    /**
     * Sets the text that informs the user that there is no data available with
     * which to draw the chart.
     *
     * @param text
     */
    public void setNoDataText(String text) {
        mNoDataText = text;
    }

    /**
     * Sets descriptive text to explain to the user why there is no chart
     * available Defaults to empty if not set
     *
     * @param text
     */
    public void setNoDataTextDescription(String text) {
        mNoDataTextDescription = text;
    }

    /**
     * Sets the offsets from the border of the view to the actual chart in every
     * direction manually. Provide density pixels -> they are then rendered to
     * pixels inside the chart
     *
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setOffsets(float left, float top, float right, float bottom) {

        mOffsetBottom = Utils.convertDpToPixel(bottom);
        mOffsetLeft = Utils.convertDpToPixel(left);
        mOffsetRight = Utils.convertDpToPixel(right);
        mOffsetTop = Utils.convertDpToPixel(top);
    }

    @Override
    public float getOffsetLeft() {
        return mOffsetLeft;
    }

    @Override
    public float getOffsetBottom() {
        return mOffsetBottom;
    }

    @Override
    public float getOffsetRight() {
        return mOffsetRight;
    }

    @Override
    public float getOffsetTop() {
        return mOffsetTop;
    }

    /**
     * Set this to false to disable all gestures and touches on the chart,
     * default: true
     *
     * @param enabled
     */
    public void setTouchEnabled(boolean enabled) {
        this.mTouchEnabled = enabled;
    }

    /**
     * set this to true to draw y-values on the chart NOTE (for bar and
     * linechart): if "maxvisiblecount" is reached, no values will be drawn even
     * if this is enabled
     *
     * @param enabled
     */
    public void setDrawYValues(boolean enabled) {
        this.mDrawYValues = enabled;
    }

    /**
     * sets the view that is displayed when a value is clicked on the chart
     *
     * @param v
     */
    public void setMarkerView(DecartMarkerView v) {
        mMarkerView = v;
    }

    /**
     * returns the view that is set as a marker view for the chart
     *
     * @return
     */
    public DecartMarkerView getMarkerView() {
        return mMarkerView;
    }

    /**
     * if set to true, units are drawn next to values in the chart, default:
     * false
     *
     * @param enabled
     */
    public void setDrawUnitsInChart(boolean enabled) {
        mDrawUnitInChart = enabled;
    }

    /**
     * set this to true to draw the legend, false if not
     *
     * @param enabled
     */
    public void setDrawLegend(boolean enabled) {
        mDrawLegend = enabled;
    }

    /**
     * returns true if drawing the legend is enabled, false if not
     *
     * @return
     */
    public boolean isDrawLegendEnabled() {
        return mDrawLegend;
    }

    /**
     * Returns the legend object of the chart. This method can be used to
     * customize the automatically generated legend. IMPORTANT: this will return
     * null if no data has been set for the chart when calling this method
     *
     * @return
     */
    public Legend getLegend() {
        return mLegend;
    }

    /**
     * Returns the rectangle that defines the borders of the chart-value surface
     * (into which the actual values are drawn).
     *
     * @return
     */
    @Override
    public RectF getContentRect() {
        return mContentRect;
    }

    /**
     * Returns the Transformer class that contains all matrices and is
     * responsible for transforming values into pixels on the screen and
     * backwards.
     *
     * @return
     */
    public DecartTransformer getTransformer() {
        return mTrans;
    }

    /**
     * disables intercept touchevents
     */
    public void disableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }

    /**
     * enables intercept touchevents
     */
    public void enableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(false);
    }

    /**
     * paint for the grid lines (only line and barchart)
     */
    public static final int PAINT_GRID = 3;

    /**
     * paint for the grid background (only line and barchart)
     */
    public static final int PAINT_GRID_BACKGROUND = 4;

    /**
     * paint for the y-legend values (only line and barchart)
     */
    public static final int PAINT_YLABEL = 5;

    /**
     * paint for the x-legend values (only line and barchart)
     */
    public static final int PAINT_XLABEL = 6;

    /**
     * paint for the info text that is displayed when there are no values in the
     * chart
     */
    public static final int PAINT_INFO = 7;

    /**
     * paint for the value text
     */
    public static final int PAINT_VALUES = 8;

    /**
     * paint for the inner circle (linechart)
     */
    public static final int PAINT_CIRCLES_INNER = 10;

    /**
     * paint for the description text in the bottom right corner
     */
    public static final int PAINT_DESCRIPTION = 11;

    /**
     * paint for the line surrounding the chart (only line and barchart)
     */
    public static final int PAINT_BORDER = 12;

    /**
     * paint for the hole in the middle of the pie chart
     */
    public static final int PAINT_HOLE = 13;

    /**
     * paint for the text in the middle of the pie chart
     */
    public static final int PAINT_CENTER_TEXT = 14;

    /**
     * paint for highlightning the values of a linechart
     */
    public static final int PAINT_HIGHLIGHT = 15;

    /**
     * paint object used for the limit lines
     */
    public static final int PAINT_RADAR_WEB = 16;

    /**
     * paint used for all rendering processes
     */
    public static final int PAINT_RENDER = 17;

    /**
     * paint used for the legend
     */
    public static final int PAINT_LEGEND_LABEL = 18;

    /**
     * paint object used for the limit lines
     */
    public static final int PAINT_LIMIT_LINE = 19;

    /**
     * set a new paint object for the specified parameter in the chart e.g.
     * Chart.PAINT_VALUES
     *
     * @param p     the new paint object
     * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES,
     *              ...
     */
    public void setPaint(Paint p, int which) {

        switch (which) {
            case PAINT_INFO:
                mInfoPaint = p;
                break;
            case PAINT_DESCRIPTION:
                mDescPaint = p;
                break;
            case PAINT_VALUES:
                mValuePaint = p;
                break;
            case PAINT_RENDER:
                mRenderPaint = p;
                break;
            case PAINT_LEGEND_LABEL:
                mLegendLabelPaint = p;
                break;
            case PAINT_XLABEL:
                mXLabelPaint = p;
                break;
            case PAINT_YLABEL:
                mYLabelPaint = p;
                break;
            case PAINT_HIGHLIGHT:
                mHighlightPaint = p;
                break;
            case PAINT_LIMIT_LINE:
                mLimitLinePaint = p;
                break;
            case PAINT_GRID:
                mGridPaint = p;
                break;
            case PAINT_GRID_BACKGROUND:
                mGridBackgroundPaint = p;
                break;
            case PAINT_BORDER:
                mBorderPaint = p;
                break;
        }
    }

    /**
     * Returns the paint object associated with the provided constant.
     *
     * @param which e.g. Chart.PAINT_LEGEND_LABEL
     * @return
     */
    public Paint getPaint(int which) {
        switch (which) {
            case PAINT_INFO:
                return mInfoPaint;
            case PAINT_DESCRIPTION:
                return mDescPaint;
            case PAINT_VALUES:
                return mValuePaint;
            case PAINT_RENDER:
                return mRenderPaint;
            case PAINT_LEGEND_LABEL:
                return mLegendLabelPaint;
            case PAINT_XLABEL:
                return mXLabelPaint;
            case PAINT_YLABEL:
                return mYLabelPaint;
            case PAINT_HIGHLIGHT:
                return mHighlightPaint;
            case PAINT_LIMIT_LINE:
                return mLimitLinePaint;
            case PAINT_GRID:
                return mGridPaint;
            case PAINT_GRID_BACKGROUND:
                return mGridBackgroundPaint;
            case PAINT_BORDER:
                return mBorderPaint;
        }

        return null;
    }

    /**
     * returns true if drawing the marker-view is enabled when tapping on values
     * (use the setMarkerView(View v) method to specify a marker view)
     *
     * @return
     */
    public boolean isDrawMarkerViewEnabled() {
        return mDrawMarkerViews;
    }

    /**
     * Set this to true to draw a user specified marker-view when tapping on
     * chart values (use the setMarkerView(MarkerView mv) method to specify a
     * marker view). Default: true
     *
     * @param enabled
     */
    public void setDrawMarkerViews(boolean enabled) {
        mDrawMarkerViews = enabled;
    }

    /**
     * Sets the formatter to be used for drawing the values inside the chart. If
     * no formatter is set, the chart will automatically determine a reasonable
     * formatting (concerning decimals) for all the values that are drawn inside
     * the chart. Set this to NULL to re-enable auto formatting.
     *
     * @param f
     */
    public void setValueFormatter(ValueFormatter f) {
        mValueFormatter = f;

        if (f == null)
            mUseDefaultFormatter = true;
        else
            mUseDefaultFormatter = false;
    }

    /**
     * Returns the formatter used for drawing the values inside the chart.
     *
     * @return
     */
    public ValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    /**
     * sets the draw color for the value paint object
     *
     * @param color
     */
    public void setValueTextColor(int color) {
        mValuePaint.setColor(color);
    }

    /**
     * Sets the font size of the values that are drawn inside the chart.
     *
     * @param size
     */
    public void setValueTextSize(float size) {
        mValuePaint.setTextSize(Utils.convertDpToPixel(size));
    }

    /**
     * returns true if y-value drawing is enabled, false if not
     *
     * @return
     */
    public boolean isDrawYValuesEnabled() {
        return mDrawYValues;
    }

    /**
     * Returns the ChartData object that ORIGINALLY has been set for the chart.
     * It contains all data in an unaltered state, before any filtering
     * algorithms have been applied.
     *
     * @return
     */
    public T getData() {
        return mData;
    }

    /**
     * returns the percentage the given value has of the total y-value sum
     *
     * @param val
     * @return
     */
    public float getPercentOfTotal(float val) {
        return val / mData.getYValueSum() * 100f;
    }

    /**
     * sets a typeface for the value-paint
     *
     * @param t
     */
    public void setValueTypeface(Typeface t) {
        mValuePaint.setTypeface(t);
    }

    /**
     * sets the typeface for the description paint
     *
     * @param t
     */
    public void setDescriptionTypeface(Typeface t) {
        mDescPaint.setTypeface(t);
    }

    /**
     * Returns the bitmap that represents the chart.
     *
     * @return
     */
    public Bitmap getChartBitmap() {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on
            // the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        draw(canvas);
        // return the bitmap
        return returnedBitmap;
    }

    /**
     * Saves the current chart state with the given name to the given path on
     * the sdcard leaving the path empty "" will put the saved file directly on
     * the SD card chart is saved as a PNG image, example:
     * saveToPath("myfilename", "foldername1/foldername2");
     *
     * @param title
     * @param pathOnSD e.g. "folder1/folder2/folder3"
     * @return returns true on success, false on error
     */
    public boolean saveToPath(String title, String pathOnSD) {

        Bitmap b = getChartBitmap();

        OutputStream stream = null;
        try {
            stream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
                    + pathOnSD + "/" + title
                    + ".png");

            /*
             * Write bitmap to file using JPEG or PNG and 40% quality hint for
             * JPEG.
             */
            b.compress(Bitmap.CompressFormat.PNG, 40, stream);

            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Saves the current state of the chart to the gallery as a JPEG image. The
     * filename and compression can be set. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName e.g. "my_image"
     * @param quality  e.g. 50, min = 0, max = 100
     * @return returns true if saving was successfull, false if not
     */
    public boolean saveToGallery(String fileName, int quality) {

        // restrain quality
        if (quality < 0 || quality > 100)
            quality = 50;

        long currentTime = System.currentTimeMillis();

        File extBaseDir = Environment.getExternalStorageDirectory();
        File file = new File(extBaseDir.getAbsolutePath() + "/DCIM");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false;
            }
        }

        String filePath = file.getAbsolutePath() + "/" + fileName;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);

            Bitmap b = getChartBitmap();

            b.compress(Bitmap.CompressFormat.JPEG, quality, out); // control
            // the jpeg
            // quality

            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        long size = new File(filePath).length();

        ContentValues values = new ContentValues(8);

        // store the details
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.DATE_ADDED, currentTime);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DESCRIPTION, "MPAndroidChart-Library Save");
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.DATA, filePath);
        values.put(MediaStore.Images.Media.SIZE, size);

        return getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) == null
                ? false : true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        prepareContentRect();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w > 0 && h > 0 && h < 5000) {
            // create a new bitmap with the new dimensions
            try {
                mDrawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
                mDrawCanvas = new Canvas(mDrawBitmap);
            } catch (OutOfMemoryError e) {
                // try to create a new bitmap after System.gc() 
                try {
                    System.gc();
                    mDrawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
                    mDrawCanvas = new Canvas(mDrawBitmap);
                } catch (OutOfMemoryError e1) {
                    System.gc();
                }
            }
        }

        // prepare content rect and matrices
        prepareContentRect();
        prepare();

        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Default formatter used for formatting values. Uses a DecimalFormat with
     * pre-calculated number of digits (depending on max and min value).
     *
     * @author Philipp Jahoda
     */
    private class DefaultValueFormatter implements ValueFormatter {

        /**
         * decimalformat for formatting
         */
        private DecimalFormat mFormat;

        public DefaultValueFormatter(DecimalFormat f) {
            mFormat = f;
        }

        @Override
        public String getFormattedValue(float value) {
            // avoid memory allocations here (for performance)
            return mFormat.format(value);
        }
    }

    @Override
    public View getChartView() {
        return this;
    }

    /**
     * does all necessary preparations, needed when data is changed or flags
     * that effect the data are changed
     */
    public void prepare() {

        if (mDataNotSet)
            return;

        calcMinMax(mFixedYValues);

        prepareYLabels();

        prepareXLabels();

        prepareLegend();

        calculateOffsets();
    }

    /**
     * Sets up all the matrices that will be used for scaling the coordinates to
     * the display. Offset and Value-px.
     */
    private void prepareMatrix() {

        mTrans.prepareMatrixValuePx(this);

        mTrans.prepareMatrixOffset(this);

        if (mLogEnabled)
            Log.i(LOG_TAG, "Matrices prepared.");
    }

    public void notifyDataSetChanged() {
        if (!mFixedYValues) {
            prepare();
            // prepareContentRect();
            mTrans.prepareMatrixValuePx(this);
        } else {
            calcMinMax(mFixedYValues);
        }
    }

    protected void calculateOffsets() {

        float legendRight = 0f, legendBottom = 0f;

        // setup offsets for legend
        if (mDrawLegend && mLegend != null && mLegend.getPosition() != LegendPosition.NONE) {

            if (mLegend.getPosition() == LegendPosition.RIGHT_OF_CHART
                    || mLegend.getPosition() == LegendPosition.RIGHT_OF_CHART_CENTER) {

                // this is the space between the legend and the chart
                float spacing = Utils.convertDpToPixel(12f);

                legendRight = mLegend.getMaximumEntryLength(mLegendLabelPaint)
                        + mLegend.getFormSize() + mLegend.getFormToTextSpace() + spacing;

                mLegendLabelPaint.setTextAlign(Align.LEFT);

            } else if (mLegend.getPosition() == LegendPosition.BELOW_CHART_LEFT
                    || mLegend.getPosition() == LegendPosition.BELOW_CHART_RIGHT
                    || mLegend.getPosition() == LegendPosition.BELOW_CHART_CENTER) {

                if (mXLabels.getPosition() == XLabelPosition.TOP)
                    legendBottom = mLegendLabelPaint.getTextSize() * 3.5f;
                else {
                    legendBottom = mLegendLabelPaint.getTextSize() * 2.5f;
                }
            }

            mLegend.setOffsetBottom(legendBottom);
            mLegend.setOffsetRight(legendRight);
        }

        float yleft = 0f, yright = 0f;

        // String label = mYLabels.getFormattedLabel(mYLabels.mEntryCount - 1);
        String label = mYLabels.getLongestLabel();

        // calculate the maximum y-label width (including eventual offsets)
        float ylabelwidth = Utils.calcTextWidth(mYLabelPaint,
                label + (mYChartMin < 0 ? "------" : "+++++")); // offsets

        if (mDrawYLabels) {

            // offsets for y-labels
            if (mYLabels.getPosition() == YLabelPosition.LEFT) {

                yleft = ylabelwidth;
                mYLabelPaint.setTextAlign(Align.RIGHT);

            } else if (mYLabels.getPosition() == YLabelPosition.RIGHT) {

                yright = ylabelwidth;
                mYLabelPaint.setTextAlign(Align.LEFT);

            } else if (mYLabels.getPosition() == YLabelPosition.BOTH_SIDED) {

                yright = ylabelwidth;
                yleft = ylabelwidth;
            }
        }

        float xtop = 0f, xbottom = 0f;

        float xlabelheight = Utils.calcTextHeight(mXLabelPaint, "Q") * 2f;

        if (mDrawXLabels) {

            // offsets for x-labels
            if (mXLabels.getPosition() == XLabelPosition.BOTTOM) {

                xbottom = xlabelheight;

            } else if (mXLabels.getPosition() == XLabelPosition.TOP) {

                xtop = xlabelheight;

            } else if (mXLabels.getPosition() == XLabelPosition.BOTH_SIDED) {

                xbottom = xlabelheight;
                xtop = xlabelheight;
            }
        }

        // all required offsets are calculated, now find largest and apply
        float min = Utils.convertDpToPixel(11f);

        mOffsetBottom = Math.max(min, xbottom + legendBottom);
        mOffsetTop = Math.max(min, xtop);

        mOffsetLeft = Math.max(min, yleft);
        mOffsetRight = Math.max(min, yright + legendRight);

        if (mLegend != null) {

            // those offsets are equal for legend and other chart, just apply
            // them
            mLegend.setOffsetTop(mOffsetTop + min / 3f);
            mLegend.setOffsetLeft(mOffsetLeft);
        }

        prepareContentRect();

        prepareMatrix();
    }

    /**
     * Calculates the offsets that belong to the legend, this method is only
     * relevant when drawing into the chart. It can be used to refresh the
     * legend.
     */
    public void calculateLegendOffsets() {

        // setup offsets for legend
        if (mLegend.getPosition() == LegendPosition.RIGHT_OF_CHART) {

            mLegend.setOffsetRight(mLegend.getMaximumEntryLength(mLegendLabelPaint));
            mLegendLabelPaint.setTextAlign(Align.LEFT);

        } else if (mLegend.getPosition() == LegendPosition.BELOW_CHART_LEFT
                || mLegend.getPosition() == LegendPosition.BELOW_CHART_RIGHT) {

            if (mXLabels.getPosition() == XLabelPosition.TOP)
                mLegend.setOffsetBottom(mLegendLabelPaint.getTextSize() * 3.5f);
            else {
                mLegend.setOffsetBottom(mLegendLabelPaint.getTextSize() * 2.5f);
            }
        }
    }

    /**
     * setup the x-axis labels
     */
    protected void prepareXLabels() {

        StringBuffer a = new StringBuffer();

        int max = (int) Math.round(mData.getXValAverageLength()
                + mXLabels.getSpaceBetweenLabels());

        for (int i = 0; i < max; i++) {
            a.append("h");
        }

        mXLabels.mLabelWidth = Utils.calcTextWidth(mXLabelPaint, a.toString());
        mXLabels.mLabelHeight = Utils.calcTextHeight(mXLabelPaint, "Q");


        float xMin = 0f;
        float xMax = 0f;

        // calculate the starting and entry point of the x-labels (depending on
        // zoom / contentrect bounds)
        if (mContentRect.height() > 10 && !mTrans.isFullyZoomedOutX()) {

            PointD p1 = getValuesByTouchPoint(mContentRect.left, mContentRect.bottom);
            PointD p2 = getValuesByTouchPoint(mContentRect.right, mContentRect.bottom);

            if (!mTrans.isInvertXAxisEnabled()) {
                xMin = (float) p1.x;
                xMax = (float) p2.x;
            } else {

                if (!mStartAtZero)
                    xMin = (float) Math.min(p1.x, p2.x);
                else
                    xMin = 0;
                xMax = (float) Math.max(p1.x, p2.x);
            }
        } else {

            if (!mTrans.isInvertXAxisEnabled()) {
                xMin = mXChartMin;
                xMax = mXChartMax;
            } else {

                if (!mStartAtZero)
                    xMin = (float) Math.min(mXChartMax, mXChartMin);
                else
                    xMin = 0;
                xMax = (float) Math.max(mXChartMax, mXChartMin);
            }
        }

        int labelCount = mXLabels.getLabelCount();
        double range = Math.abs(xMax - xMin);

        if (labelCount == 0 || range <= 0) {
            mXLabels.mEntries = new float[]{};
            return;
        }

        double rawInterval = range / labelCount;
        double interval = Utils.roundToNextSignificant(rawInterval);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or
            // 90
            interval = Math.floor(10 * intervalMagnitude);
        }

        // if the labels should only show min and max
        if (mXLabels.isShowOnlyMinMaxEnabled()) {

            mXLabels.mEntries = new float[2];
            mXLabels.mEntries[0] = mXChartMin;
            mXLabels.mEntries[1] = mXChartMax;

        } else {
            double first;
            if (xMax - xMin > 5) {
                first = Math.round(xMin);
            } else {
                first = Math.ceil(xMin / interval) * interval;
            }
            double last = Utils.nextUp(Math.floor(xMax / interval) * interval);

            double f;
            int n = 0;
            for (f = first; f <= last; f += interval) {
                ++n;
            }

            int j;
            mXLabels.mEntries = new float[n];

            for (f = first, j = 0; j < n; f += interval, ++j) {
                mXLabels.mEntries[j] = (float) f;
            }
        }

        if (interval < 1) {
            mXLabels.mDecimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            mXLabels.mDecimals = 0;
        }
    }

    /**
     * Sets up the y-axis labels. Computes the desired number of labels between
     * the two given extremes. Unlike the papareXLabels() method, this method
     * needs to be called upon every refresh of the view.
     *
     * @return
     */
    protected void prepareYLabels() {

        float yMin = 0f;
        float yMax = 0f;

        // calculate the starting and entry point of the y-labels (depending on
        // zoom / contentrect bounds)
        if (mContentRect.width() > 10 && !mTrans.isFullyZoomedOutY()) {

            PointD p1 = getValuesByTouchPoint(mContentRect.left, mContentRect.top);
            PointD p2 = getValuesByTouchPoint(mContentRect.left, mContentRect.bottom);

            if (!mTrans.isInvertYAxisEnabled()) {
                yMin = (float) p2.y;
                yMax = (float) p1.y;
            } else {

                if (!mStartAtZero)
                    yMin = (float) Math.min(p1.y, p2.y);
                else
                    yMin = 0;
                yMax = (float) Math.max(p1.y, p2.y);
            }

        } else {

            if (!mTrans.isInvertYAxisEnabled()) {
                yMin = mYChartMin;
                yMax = mYChartMax;
            } else {

                if (!mStartAtZero)
                    yMin = (float) Math.min(mYChartMax, mYChartMin);
                else
                    yMin = 0;
                yMax = (float) Math.max(mYChartMax, mYChartMin);
            }
        }

        int labelCount = mYLabels.getLabelCount();
        double range = Math.abs(yMax - yMin);

        if (labelCount == 0 || range <= 0) {
            mYLabels.mEntries = new float[]{};
            mYLabels.mEntryCount = 0;
            return;
        }

        double rawInterval = range / labelCount;
        double interval = Utils.roundToNextSignificant(rawInterval);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or
            // 90
            interval = Math.floor(10 * intervalMagnitude);
        }

        // if the labels should only show min and max
        if (mYLabels.isShowOnlyMinMaxEnabled()) {

            mYLabels.mEntryCount = 2;
            mYLabels.mEntries = new float[2];
            mYLabels.mEntries[0] = mYChartMin;
            mYLabels.mEntries[1] = mYChartMax;

        } else {

            double first = Math.ceil(yMin / interval) * interval;
            double last = Utils.nextUp(Math.floor(yMax / interval) * interval);

            double f;
            int i;
            int n = 0;
            for (f = first; f <= last; f += interval) {
                ++n;
            }

            mYLabels.mEntryCount = n;

            if (mYLabels.mEntries.length < n) {
                // Ensure stops contains at least numStops elements.
                mYLabels.mEntries = new float[n];
            }

            for (f = first, i = 0; i < n; f += interval, ++i) {
                mYLabels.mEntries[i] = (float) f;
            }
        }

        if (interval < 1) {
            mYLabels.mDecimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            mYLabels.mDecimals = 0;
        }
    }

    /**
     * draws the x-axis labels to the screen depending on their position
     */
    private void drawXLabels() {

        if (!mDrawXLabels)
            return;

        float yoffset = Utils.convertDpToPixel(4f);

        mXLabelPaint.setTypeface(mXLabels.getTypeface());
        mXLabelPaint.setTextSize(mXLabels.getTextSize());
        mXLabelPaint.setColor(mXLabels.getTextColor());

        if (mXLabels.getPosition() == XLabelPosition.TOP) {

            drawXLabels(getOffsetTop() - yoffset);

        } else if (mXLabels.getPosition() == XLabelPosition.BOTTOM) {

            drawXLabels(getHeight() - mOffsetBottom + mXLabels.mLabelHeight + yoffset * 1.5f);

        } else if (mXLabels.getPosition() == XLabelPosition.BOTTOM_INSIDE) {

            drawXLabels(getHeight() - getOffsetBottom() - yoffset);

        } else if (mXLabels.getPosition() == XLabelPosition.TOP_INSIDE) {

            drawXLabels(getOffsetTop() + yoffset + mXLabels.mLabelHeight);

        } else { // BOTH SIDED

            drawXLabels(getOffsetTop() - 7);
            drawXLabels(getHeight() - mOffsetBottom + mXLabels.mLabelHeight + yoffset * 1.6f);
        }
    }

    /**
     * draws the x-labels on the specified y-position
     *
     * @param yPos
     */
    protected void drawXLabels(float yPos) {

        // pre allocate to save performance (dont allocate in loop)
        float[] position = new float[]{
                0f, 0f
        };

        for (int i = 0; i < mXLabels.mEntries.length; i++) {
            position[0] = mXLabels.mEntries[i];

            // center the text
            if (mXLabels.isCenterXLabelsEnabled())
                position[0] += 0.5f;

            mTrans.pointValuesToPixel(position);

            if (position[0] >= mOffsetLeft && position[0] <= getWidth() - mOffsetRight) {

                String label = mXLabels.getFormattedLabel(i);

                if (mXLabels.isAvoidFirstLastClippingEnabled()) {

                    // avoid clipping of the last
                    if (i == mXLabels.getLabelCount() - 1) {
                        float width = Utils.calcTextWidth(mXLabelPaint, label);

                        if (width > getOffsetRight() * 2 && position[0] + width > getWidth())
                            position[0] -= width / 2;

                        // avoid clipping of the first
                    } else if (i == 0) {

                        float width = Utils.calcTextWidth(mXLabelPaint, label);
                        position[0] += width / 2;
                    }
                }

                if (needToDrawXValue(label)) {
                    mDrawCanvas.drawText(label, position[0],
                            yPos,
                            mXLabelPaint);
                }
            }
        }
    }

    protected boolean needToDrawXValue(String label) {
        return true;
    }

    /**
     * draws the y-axis labels to the screen
     */
    private void drawYLabels() {

        if (!mDrawYLabels)
            return;

        float[] positions = new float[mYLabels.mEntryCount * 2];

        for (int i = 0; i < positions.length; i += 2) {
            // only fill y values, x values are not needed since the y-labels
            // are
            // static on the x-axis
            positions[i + 1] = mYLabels.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);

        mYLabelPaint.setTypeface(mYLabels.getTypeface());
        mYLabelPaint.setTextSize(mYLabels.getTextSize());
        mYLabelPaint.setColor(mYLabels.getTextColor());

        float xoffset = Utils.convertDpToPixel(5f);
        float yoffset = Utils.calcTextHeight(mYLabelPaint, "A") / 2.5f;

        // determine position and draw adequately
        if (mYLabels.getPosition() == YLabelPosition.LEFT) {

            mYLabelPaint.setTextAlign(Align.RIGHT);
            drawYLabels(mOffsetLeft - xoffset, positions, yoffset);

        } else if (mYLabels.getPosition() == YLabelPosition.RIGHT) {

            mYLabelPaint.setTextAlign(Align.LEFT);
            drawYLabels(getWidth() - mOffsetRight + xoffset, positions, yoffset);

        } else if (mYLabels.getPosition() == YLabelPosition.RIGHT_INSIDE) {

            mYLabelPaint.setTextAlign(Align.RIGHT);
            drawYLabels(getWidth() - mOffsetRight - xoffset, positions, yoffset);

        } else if (mYLabels.getPosition() == YLabelPosition.LEFT_INSIDE) {

            mYLabelPaint.setTextAlign(Align.LEFT);
            drawYLabels(mOffsetLeft + xoffset, positions, yoffset);

        } else { // BOTH SIDED Y-AXIS LABELS

            // draw left legend
            mYLabelPaint.setTextAlign(Align.RIGHT);
            drawYLabels(mOffsetLeft - xoffset, positions, yoffset);

            // draw right legend
            mYLabelPaint.setTextAlign(Align.LEFT);
            drawYLabels(getWidth() - mOffsetRight + xoffset, positions, yoffset);
        }
    }

    /**
     * draws the y-labels on the specified x-position
     *
     * @param xPos
     * @param positions
     */
    private void drawYLabels(float xPos, float[] positions, float yOffset) {

        // draw
        for (int i = 0; i < mYLabels.mEntryCount; i++) {

            String text = mYLabels.getFormattedLabel(i);

            if (!mYLabels.isDrawTopYLabelEntryEnabled() && i >= mYLabels.mEntryCount - 1)
                return;

            if (mYLabels.isDrawUnitsInYLabelEnabled()) {
                mDrawCanvas.drawText(text, xPos, positions[i * 2 + 1] + yOffset,
                        mYLabelPaint);
            } else {
                mDrawCanvas.drawText(text, xPos, positions[i * 2 + 1] + yOffset, mYLabelPaint);
            }
        }
    }

    /**
     * enums for all different border styles
     */
    public enum BorderPosition {
        LEFT, RIGHT, TOP, BOTTOM
    }

    /**
     * array that holds positions where to draw the chart border lines
     */
    private BorderPosition[] mBorderPositions = new BorderPosition[]{
            BorderPosition.BOTTOM,
            BorderPosition.LEFT
    };

    /**
     * draws a line that surrounds the chart
     */
    protected void drawBorder() {

        if (!mDrawBorder || mBorderPositions == null)
            return;

        for (int i = 0; i < mBorderPositions.length; i++) {

            if (mBorderPositions[i] == null)
                continue;

            switch (mBorderPositions[i]) {
                case LEFT:
                    mDrawCanvas.drawLine(mOffsetLeft, mOffsetTop, mOffsetLeft, getHeight()
                            - mOffsetBottom, mBorderPaint);
                    break;
                case RIGHT:
                    mDrawCanvas.drawLine(getWidth() - mOffsetRight, mOffsetTop, getWidth()
                            - mOffsetRight, getHeight()
                            - mOffsetBottom, mBorderPaint);
                    break;
                case TOP:
                    mDrawCanvas.drawLine(mOffsetLeft, mOffsetTop, getWidth() - mOffsetRight,
                            mOffsetTop, mBorderPaint);
                    break;
                case BOTTOM:
                    mDrawCanvas.drawLine(mOffsetLeft, getHeight()
                            - mOffsetBottom, getWidth() - mOffsetRight, getHeight()
                            - mOffsetBottom, mBorderPaint);
                    break;
            }
        }
    }

    /**
     * draws the grid background
     */
    protected void drawGridBackground() {

        if (!mDrawGridBackground)
            return;

        Rect gridBackground = new Rect((int) mOffsetLeft + 1, (int) mOffsetTop + 1, getWidth()
                - (int) mOffsetRight,
                getHeight() - (int) mOffsetBottom);

        // draw the grid background
        mDrawCanvas.drawRect(gridBackground, mGridBackgroundPaint);
    }

    /**
     * draws the horizontal grid
     */
    protected void drawHorizontalGrid() {

        if (!mDrawHorizontalGrid)
            return;

        // pre alloc
        float[] position = new float[2];

        // draw the horizontal grid
        for (int i = 0; i < mYLabels.mEntryCount; i++) {

            position[1] = mYLabels.mEntries[i];
            mTrans.pointValuesToPixel(position);

            mDrawCanvas.drawLine(mOffsetLeft, position[1], getWidth() - mOffsetRight, position[1],
                    mGridPaint);
        }
    }

    /**
     * draws the vertical grid
     */
    protected void drawVerticalGrid() {

        if (!mDrawVerticalGrid || mData == null)
            return;

        float[] position = new float[]{
                0f, 0f
        };

        for (int i = 0; i < mXLabels.mEntries.length; i++) {

            position[0] = mXLabels.mEntries[i];
            mTrans.pointValuesToPixel(position);
            mDrawCanvas.drawLine(position[0], mOffsetTop, position[0], getHeight()
                    - mOffsetBottom, mGridPaint);
        }
    }

    /**
     * Draws the limit lines if there are one.
     */
    private void drawLimitLines() {

        ArrayList<LimitLine> limitLines = mData.getLimitLines();

        if (limitLines == null)
            return;

        float[] pts = new float[4];

        for (int i = 0; i < limitLines.size(); i++) {

            LimitLine l = limitLines.get(i);

            pts[1] = l.getLimit();
            pts[3] = l.getLimit();

            mTrans.pointValuesToPixel(pts);

            pts[0] = 0;
            pts[2] = getWidth();

            mLimitLinePaint.setColor(l.getLineColor());
            mLimitLinePaint.setPathEffect(l.getDashPathEffect());
            mLimitLinePaint.setStrokeWidth(l.getLineWidth());

            mDrawCanvas.drawLines(pts, mLimitLinePaint);

            // if drawing the limit-value is enabled
            if (l.isDrawValueEnabled()) {

                PointF pos = getPosition(new DecartEntry(l.getLimit(), 0));

                // save text align
                Align align = mValuePaint.getTextAlign();

                float xOffset = Utils.convertDpToPixel(4f);
                float yOffset = l.getLineWidth() + xOffset;
                String label = mValueFormatter.getFormattedValue(l.getLimit());

                if (l.getLabelPosition() == LimitLabelPosition.RIGHT) {

                    mValuePaint.setTextAlign(Align.RIGHT);
                    mDrawCanvas.drawText(label, getWidth() - mOffsetRight
                                    - xOffset,
                            pos.y - yOffset, mValuePaint);

                } else {
                    mValuePaint.setTextAlign(Align.LEFT);
                    mDrawCanvas.drawText(label, mOffsetLeft
                                    + xOffset,
                            pos.y - yOffset, mValuePaint);
                }

                mValuePaint.setTextAlign(align);
            }
        }
    }

    protected boolean isOffContentRect(RectF rectF) {
        return !mContentRect.contains(rectF);
    }

    /**
     * returns true if the specified point (x-axis) exceeds the limits of what
     * is visible to the right side
     *
     * @param v
     * @return
     */
    protected boolean isOffContentRight(float p) {
        if (p > mContentRect.right)
            return true;
        else
            return false;
    }

    /**
     * returns true if the specified point (x-axis) exceeds the limits of what
     * is visible to the left side
     *
     * @param v
     * @return
     */
    protected boolean isOffContentLeft(float p) {
        if (p < mContentRect.left)
            return true;
        else
            return false;
    }

    /**
     * returns true if the specified point (y-axis) exceeds the limits of what
     * is visible on the top
     *
     * @param v
     * @return
     */
    protected boolean isOffContentTop(float p) {
        if (p < mContentRect.top)
            return true;
        else
            return false;
    }

    /**
     * returns true if the specified point (y-naxis) exceeds the limits of what
     * is visible on the bottom
     *
     * @param v
     * @return
     */
    protected boolean isOffContentBottom(float p) {
        if (p > mContentRect.bottom)
            return true;
        else
            return false;
    }

    /**
     * touchlistener that handles touches and gestures on the chart
     */
    protected OnTouchListener mListener;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (mListener == null || mDataNotSet)
            return false;

        // check if touch gestures are enabled
        if (!mTouchEnabled)
            return false;
        else
            return mListener.onTouch(this, event);
    }

    /**
     * ################ ################ ################ ################
     */
    /** CODE BELOW THIS RELATED TO SCALING AND GESTURES */

    /**
     * refresh method
     */

    public Matrix refresh(Matrix save) {
        Matrix result = mTrans.refresh(save, this);
        if (mOnZoomChangedListener != null) {
            mOnZoomChangedListener.onZoomChanged(this);
        }
        return result;
    }

    /**
     * Zooms in by 1.4f, into the charts center. center.
     */
    public void zoomIn() {
        Matrix save = mTrans.zoomIn(getWidth() / 2f, -(getHeight() / 2f));
        refresh(save);
    }

    /**
     * Zooms out by 0.7f, from the charts center. center.
     */
    public void zoomOut() {
        Matrix save = mTrans.zoomOut(getWidth() / 2f, -(getHeight() / 2f));
        refresh(save);
    }

    /**
     * Zooms in or out by the given scale factor. x and y are the coordinates
     * (in pixels) of the zoom center.
     *
     * @param scaleX if < 1f --> zoom out, if > 1f --> zoom in
     * @param scaleY if < 1f --> zoom out, if > 1f --> zoom in
     * @param x
     * @param y
     */
    public void zoom(float scaleX, float scaleY, float x, float y) {
        Matrix save = mTrans.zoom(scaleX, scaleY, x, -y);
        refresh(save);
    }

    /**
     * Resets all zooming and dragging and makes the chart fit exactly it's
     * bounds.
     */
    public void fitScreen() {
        Matrix save = mTrans.fitScreen();
        refresh(save);
    }

    /**
     * If this is set to true, the y-axis is inverted which means that low
     * values are on top of the chart, high values on bottom.
     *
     * @param enabled
     */
    public void setInvertYAxisEnabled(boolean enabled) {
        mTrans.setInvertYAxisEnabled(enabled);
    }

    /**
     * If this returns true, the y-axis is inverted.
     *
     * @return
     */
    public boolean isInvertYAxisEnabled() {
        return mTrans.isInvertYAxisEnabled();
    }

    /**
     * Centers the viewport around the specified x-index and the specified
     * y-value in the chart. Centering the viewport outside the bounds of the
     * chart is not possible. Makes most sense in combination with the
     * setScaleMinima(...) method. First set the scale minima, then center the
     * viewport. SHOULD BE CALLED AFTER setting data for the chart.
     *
     * @param xIndex the index on the x-axis to center to
     * @param yVal   the value ont he y-axis to center to
     */
    public synchronized void centerViewPort(final float xIndex, final float yVal) {

        float indicesInView = mDeltaX / mTrans.getScaleX();
        float valsInView = mDeltaY / mTrans.getScaleY();

        Log.i(LOG_TAG, "indices: " + indicesInView + ", vals: " +
                valsInView);

        float[] pts = new float[]{
                xIndex - indicesInView / 2f, yVal + valsInView / 2f
        };

        mTrans.centerViewPort(pts, this);
    }

    /**
     * ################ ################ ################ ################
     */
    /** CODE BELOW IS GETTERS AND SETTERS */

    /**
     * set a new (e.g. custom) charttouchlistener NOTE: make sure to
     * setTouchEnabled(true); if you need touch gestures on the chart
     *
     * @param l
     */
    public void setOnTouchListener(OnTouchListener l) {
        this.mListener = l;
    }

    /**
     * Sets the OnDrawListener
     *
     * @param drawListener
     */
    public void setOnDrawListener(OnDrawListener drawListener) {
        this.mDrawListener = drawListener;
    }

    /**
     * Gets the OnDrawListener. May be null.
     *
     * @return
     */
    public OnDrawListener getDrawListener() {
        return mDrawListener;
    }

    /**
     * Sets the minimum scale values for both axes. This limits the extent to
     * which the user can zoom-out. Scale 2f means the user cannot zoom out
     * further than 2x zoom, ... Min = 1f
     *
     * @param scaleXmin
     * @param scaleYmin
     */
    public void setScaleMinima(float scaleXmin, float scaleYmin) {
        mTrans.setScaleMinima(scaleXmin, scaleYmin, this);
    }

    /**
     * Sets the effective range of y-values the chart can display. If this is
     * set, the y-range is fixed and cannot be changed. This means, no
     * recalculation of the bounds of the chart concerning the y-axis will be
     * done when adding new data. To disable this, provide Float.NaN as a
     * parameter or call resetYRange();
     *
     * @param minY
     * @param maxY
     * @param invalidate if set to true, the chart will redraw itself after
     *                   calling this method
     */
    public void setYRange(float minY, float maxY, boolean invalidate) {

        if (Float.isNaN(minY) || Float.isNaN(maxY)) {
            resetYRange(invalidate);
            return;
        }

        mFixedYValues = true;

        mYChartMin = minY;
        mYChartMax = maxY;
        if (minY < 0) {
            mStartAtZero = false;
        }
        mDeltaY = mYChartMax - mYChartMin;

        calcFormats();
        prepareMatrix();
        if (invalidate)
            invalidate();
    }

    /**
     * Resets the previously set y range. If new data is added, the y-range will
     * be recalculated.
     *
     * @param invalidate if set to true, the chart will redraw itself after
     *                   calling this method
     */
    public void resetYRange(boolean invalidate) {
        mFixedYValues = false;
        calcMinMax(mFixedYValues);

        prepareMatrix();
        if (invalidate)
            invalidate();
    }

    /**
     * if this returns true, the chart has a fixed range on the y-axis that is
     * not dependant on the actual data in the chart
     *
     * @return
     */
    public boolean hasFixedYValues() {
        return mFixedYValues;
    }

    /**
     * Returns the position (in pixels) the provided Entry has inside the chart
     * view or null, if the provided Entry is null.
     *
     * @param e
     * @return
     */
    public PointF getPosition(DecartEntry e) {
        throw new RuntimeException("todo");
//
//        if (e == null)
//            return null;
//
//        float[] vals = new float[]{
//                e.getXIndex(), e.getVal()
//        };
//
//        if (this instanceof DecartGraph) {
//
//            DecartDataSet set = (DecartDataSet) mData.getDataSetForEntry(e);
//            if (set != null)
//                vals[0] += set.getBarSpace() / 2f;
//        }
//
//        mTrans.pointValuesToPixel(vals);
//
//        return new PointF(vals[0], vals[1]);
    }

    /**
     * sets the color for the grid lines
     *
     * @param color
     */
    public void setGridColor(int color) {
        mGridPaint.setColor(color);
    }

    /**
     * sets the number of maximum visible drawn values on the chart only active
     * when setDrawValues() is enabled
     *
     * @param count
     */
    public void setMaxVisibleValueCount(int count) {
        this.mMaxVisibleCount = count;
    }

    /**
     * If set to true, the highlight indicators (cross of two lines for
     * LineChart and ScatterChart, dark bar overlay for BarChart) that give
     * visual indication that an Entry has been selected will be drawn upon
     * selecting values. This does not depend on the MarkerView. Default: true
     *
     * @param enabled
     */
    public void setHighlightIndicatorEnabled(boolean enabled) {
        mHighLightIndicatorEnabled = enabled;
    }

    /**
     * enable this to force the y-axis labels to always start at zero
     *
     * @param enabled
     */
    public void setStartAtZero(boolean enabled) {
        this.mStartAtZero = enabled;
        prepare();
        prepareMatrix();
    }

    /**
     * returns true if the chart is set to start at zero, false otherwise
     *
     * @return
     */
    public boolean isStartAtZeroEnabled() {
        return mStartAtZero;
    }

    /**
     * sets the width of the grid lines (min 0.1f, max = 3f)
     *
     * @param width
     */
    public void setGridWidth(float width) {

        if (width < 0.1f)
            width = 0.1f;
        if (width > 3.0f)
            width = 3.0f;
        mGridWidth = width;
    }

    /**
     * Set this to true to enable dragging (moving the chart with the finger)
     * for the chart (this does not effect scaling).
     *
     * @param enabled
     */
    public void setDragEnabled(boolean enabled) {
        this.mDragEnabled = enabled;
    }

    /**
     * Returns true if dragging is enabled for the chart, false if not.
     *
     * @return
     */
    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    /**
     * Set this to true to enable scaling (zooming in and out by gesture) for
     * the chart (this does not effect dragging).
     *
     * @param enabled
     */
    public void setScaleEnabled(boolean enabled) {
        this.mScaleEnabled = enabled;
    }

    /**
     * Returns true if scaling (zooming in and out by gesture) is enabled for
     * the chart, false if not.
     *
     * @return
     */
    public boolean isScaleEnabled() {
        return mScaleEnabled;
    }

    /**
     * Set this to true to enable zooming in by double-tap on the chart.
     * Default: enabled
     *
     * @param enabled
     */
    public void setDoubleTapToZoomEnabled(boolean enabled) {
        mDoubleTapToZoomEnabled = enabled;
    }

    /**
     * Returns true if zooming via double-tap is enabled false if not.
     *
     * @return
     */
    public boolean isDoubleTapToZoomEnabled() {
        return mDoubleTapToZoomEnabled;
    }

    /**
     * if set to true, the vertical grid will be drawn, default: true
     *
     * @param enabled
     */
    public void setDrawVerticalGrid(boolean enabled) {
        mDrawVerticalGrid = enabled;
    }

    /**
     * if set to true, the horizontal grid will be drawn, default: true
     *
     * @param enabled
     */
    public void setDrawHorizontalGrid(boolean enabled) {
        mDrawHorizontalGrid = enabled;
    }

    /**
     * returns true if drawing the vertical grid is enabled, false if not
     *
     * @return
     */
    public boolean isDrawVerticalGridEnabled() {
        return mDrawVerticalGrid;
    }

    /**
     * returns true if drawing the horizontal grid is enabled, false if not
     *
     * @return
     */
    public boolean isDrawHorizontalGridEnabled() {
        return mDrawHorizontalGrid;
    }

    /**
     * set this to true to draw the border surrounding the chart, default: true
     *
     * @param enabled
     */
    public void setDrawBorder(boolean enabled) {
        mDrawBorder = enabled;
    }

    /**
     * set this to true to draw the grid background, false if not
     *
     * @param enabled
     */
    public void setDrawGridBackground(boolean enabled) {
        mDrawGridBackground = enabled;
    }

    /**
     * set this to true to enable drawing the x-labels, false if not
     *
     * @param enabled
     */
    public void setDrawXLabels(boolean enabled) {
        mDrawXLabels = enabled;
    }

    /**
     * set this to true to enable drawing the y-labels, false if not
     *
     * @param enabled
     */
    public void setDrawYLabels(boolean enabled) {
        mDrawYLabels = enabled;
    }

    /**
     * Returns true if drawing y-labels is enabled, false if not.
     *
     * @return
     */
    public boolean isDrawYLabelsEnabled() {
        return mDrawYLabels;
    }

    /**
     * Returns true if drawing x-labels is enabled, false if not.
     *
     * @return
     */
    public boolean isDrawXLabelsEnabled() {
        return mDrawXLabels;
    }

    /**
     * Sets an array of positions where to draw the chart border lines (e.g. new
     * BorderStyle[] { BorderStyle.BOTTOM })
     *
     * @param styles
     */
    public void setBorderPositions(BorderPosition[] styles) {
        mBorderPositions = styles;
    }

    /**
     * Returns the array of positions where the chart-border is drawn.
     *
     * @return
     */
    public BorderPosition[] getBorderPositions() {
        return mBorderPositions;
    }

    /**
     * Sets the width of the border surrounding the chart in dp.
     *
     * @param width
     */
    public void setBorderWidth(int width) {
        mBorderPaint.setStrokeWidth(Utils.convertDpToPixel(width));
    }

    /**
     * Sets the color of the border surrounding the chart.
     *
     * @param color
     */
    public void setBorderColor(int color) {
        mBorderPaint.setColor(color);
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the
     * selected value at the given touch point inside the Graph.
     *
     * @param x
     * @param y
     * @return
     */
    public DecartHighlight getHighlightByTouchPoint(float x, float y) {

        if (mDataNotSet || mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.");
            return null;
        }

        // create an array of the touch-point
        float[] pts = new float[2];
        pts[0] = x;
        pts[1] = y;

        mTrans.pixelsToValue(pts);

        double xTouchVal = pts[0];
        double yTouchVal = pts[1];
        double base = Math.floor(xTouchVal);


        // touch out of chart
        if (xTouchVal < -mTouchOffset || (xTouchVal > mXChartMax))
            return null;

        ArrayList<DecartHighlight> valsAtIndex = getYValsNearXValue((float) xTouchVal, (float) yTouchVal, mTouchOffset);

        DecartHighlight selectedEntry = Utils.getClosestDataSetIndex(valsAtIndex, (float) xTouchVal, (float) yTouchVal);

        if (selectedEntry == null)
            return null;

        return selectedEntry;
    }

    /**
     * Returns an array of DecarEntry objects for the given x-value.
     *
     * @param xValue
     * @param round
     * @return
     */
    public ArrayList<DecartHighlight> getYValsNearXValue(float xValue, float yValue, float round) {

        ArrayList<DecartHighlight> vals = new ArrayList<>();

        for (int i = 0; i < mData.getDataSetCount(); i++) {

            // extract all y-values from all DataSets at the given x-index
            List<DecartEntry> decartEntries = mData.getDataSetByIndex(i).getEntriesInRange(xValue, yValue, round, 0);

            for (int j = 0; j < decartEntries.size(); j++) {
                DecartEntry decartEntry = decartEntries.get(j);
                vals.add(new DecartHighlight(decartEntry, i, mData.getDataSetByIndex(i).getEntries().indexOf(decartEntry)));
            }
        }

        return vals;
    }


    /**
     * Returns the x and y values in the chart at the given touch point
     * (encapsulated in a PointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelsForValues(...).
     *
     * @param x
     * @param y
     * @return
     */
    public PointD getValuesByTouchPoint(float x, float y) {

        // create an array of the touch-point
        float[] pts = new float[2];
        pts[0] = x;
        pts[1] = y;

        mTrans.pixelsToValue(pts);

        double xTouchVal = pts[0];
        double yTouchVal = pts[1];

        return new PointD(xTouchVal, yTouchVal);
    }

    /**
     * Transforms the given chart values into pixels. This is the opposite
     * method to getValuesByTouchPoint(...).
     *
     * @param x
     * @param y
     * @return
     */
    public PointD getPixelsForValues(float x, float y) {

        float[] pts = new float[]{
                x, y
        };

        mTrans.pointValuesToPixel(pts);

        return new PointD(pts[0], pts[1]);
    }

    /**
     * returns the y-value at the given touch position (must not necessarily be
     * a value contained in one of the datasets)
     *
     * @param x
     * @param y
     * @return
     */
    public float getYValueByTouchPoint(float x, float y) {
        return (float) getValuesByTouchPoint(x, y).y;
    }

    /**
     * returns the Entry object displayed at the touched position of the chart
     *
     * @param x
     * @param y
     * @return
     */
    public DecartEntry getEntryByTouchPoint(float x, float y) {
        DecartHighlight h = getHighlightByTouchPoint(x, y);
        if (h != null) {
            return h.getDecartEntry();
        }
        return null;
    }

    /**
     * returns the current x-scale factor
     */
    public float getScaleX() {
        return mTrans.getScaleX();
    }

    /**
     * returns the current y-scale factor
     */
    public float getScaleY() {
        return mTrans.getScaleY();
    }

    /**
     * if the chart is fully zoomed out, return true
     *
     * @return
     */
    public boolean isFullyZoomedOut() {
        return mTrans.isFullyZoomedOut();
    }

    /**
     * returns the object representing all y-labels, this method can be used to
     * acquire the YLabels object and modify it (e.g. change the position of the
     * labels)
     *
     * @return
     */
    public YLabels getYLabels() {
        return mYLabels;
    }

    /**
     * returns the object representing all x-labels, this method can be used to
     * acquire the XLabels object and modify it (e.g. change the position of the
     * labels)
     *
     * @return
     */
    public FXLabels getXLabels() {
        return mXLabels;
    }

    /**
     * if set to true, both x and y axis can be scaled with 2 fingers, if false,
     * x and y axis can be scaled separately. default: false
     *
     * @param enabled
     */
    public void setPinchZoom(boolean enabled) {
        mPinchZoomEnabled = enabled;
    }

    /**
     * returns true if pinch-zoom is enabled, false if not
     *
     * @return
     */
    public boolean isPinchZoomEnabled() {
        return mPinchZoomEnabled;
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the x-axis.
     *
     * @param offset
     */
    public void setDragOffsetX(float offset) {
        mTrans.setDragOffsetX(offset);
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the y-axis.
     *
     * @param offset
     */
    public void setDragOffsetY(float offset) {
        mTrans.setDragOffsetY(offset);
    }

    /**
     * Returns true if both drag offsets (x and y) are zero or smaller.
     *
     * @return
     */
    public boolean hasNoDragOffset() {
        return mTrans.hasNoDragOffset();
    }

}
