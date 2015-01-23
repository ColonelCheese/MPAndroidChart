package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;

import com.github.mikephil.charting.data.DecartData;
import com.github.mikephil.charting.data.DecartDataSet;
import com.github.mikephil.charting.data.DecartEntry;

import java.util.ArrayList;

/**
 * The Decart's orthogonal graph. Draws dots, triangles, squares and custom shapes into the
 * chartview.
 */
public class DecartGraph extends DecartGraphBase<DecartData> {

    /**
     * enum that defines the shape that is drawn where the values are
     */
    public enum GraphShape {
        CROSS, TRIANGLE, CIRCLE, SQUARE, CUSTOM
    }

    public DecartGraph(Context context) {
        super(context);
    }

    public DecartGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DecartGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void prepareContentRect() {
        if (isEmpty()) {
            super.prepareContentRect();
        } else {

            float offset = mData.getGreatestShapeSize() / 2f;

            mContentRect.set(mOffsetLeft - offset,
                    mOffsetTop,
                    getWidth() - mOffsetRight + offset,
                    getHeight() - mOffsetBottom);
        }
    }

    @Override
    protected void calcMinMax(boolean fixedValues) {
        super.calcMinMax(fixedValues);

        if (mDeltaX == 0 && mData.getEntriesCount() > 0)
            mDeltaX = 1;
    }

    @Override
    protected void drawData() {

        ArrayList<DecartDataSet> dataSets = mData.getDataSets();

        for (int i = 0; i < mData.getDataSetCount(); i++) {

            DecartDataSet dataSet = dataSets.get(i);
            ArrayList<DecartEntry> entries = dataSet.getEntries();

            float shapeHalf = dataSet.getScatterShapeSize() / 2f;

            float[] valuePoints = mTrans.generateTransformedValuesDecart(entries, mPhaseY);

            GraphShape shape = dataSet.getScatterShape();

            for (int j = 0; j < valuePoints.length * mPhaseX; j += 2) {

                if (isOffContentRight(valuePoints[j]))
                    break;

                // make sure the lines don't do shitty things outside bounds
                if (j != 0 && isOffContentLeft(valuePoints[j - 1])
                        && isOffContentTop(valuePoints[j + 1])
                        && isOffContentBottom(valuePoints[j + 1]))
                    continue;

                // Set the color for the currently drawn value. If the index is
                // out of bounds, reuse colors.
                mRenderPaint.setColor(dataSet.getColor(j));

                if (shape == GraphShape.SQUARE) {

                    mDrawCanvas.drawRect(valuePoints[j] - shapeHalf,
                            valuePoints[j + 1] - shapeHalf, valuePoints[j]
                                    + shapeHalf, valuePoints[j + 1]
                                    + shapeHalf, mRenderPaint);

                } else if (shape == GraphShape.CIRCLE) {

                    mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf,
                            mRenderPaint);

                } else if (shape == GraphShape.CROSS) {

                    mDrawCanvas.drawLine(valuePoints[j] - shapeHalf, valuePoints[j + 1],
                            valuePoints[j] + shapeHalf,
                            valuePoints[j + 1], mRenderPaint);
                    mDrawCanvas.drawLine(valuePoints[j], valuePoints[j + 1] - shapeHalf,
                            valuePoints[j], valuePoints[j + 1]
                                    + shapeHalf, mRenderPaint);

                } else if (shape == GraphShape.TRIANGLE) {

                    // create a triangle path
                    Path tri = new Path();
                    tri.moveTo(valuePoints[j], valuePoints[j + 1] - shapeHalf);
                    tri.lineTo(valuePoints[j] + shapeHalf, valuePoints[j + 1] + shapeHalf);
                    tri.lineTo(valuePoints[j] - shapeHalf, valuePoints[j + 1] + shapeHalf);
                    tri.close();

                    mDrawCanvas.drawPath(tri, mRenderPaint);

                } else if (shape == GraphShape.CUSTOM) {

                    Path customShape = dataSet.getCustomScatterShape();

                    if (customShape == null)
                        return;

                    // transform the provided custom path
                    mTrans.pathValueToPixel(customShape);
                    mDrawCanvas.drawPath(customShape, mRenderPaint);
                }
            }
        }
    }

    @Override
    protected void drawValues() {
        // if values are drawn
        if (mDrawYValues && mData.getEntriesCount() < mMaxVisibleCount * mTrans.getScaleX()) {

            ArrayList<DecartDataSet> dataSets = mData
                    .getDataSets();

            for (int i = 0; i < mData.getDataSetCount(); i++) {

                DecartDataSet dataSet = dataSets.get(i);
                ArrayList<DecartEntry> entries = dataSet.getEntries();

                float[] positions = mTrans.generateTransformedValuesDecart(entries, mPhaseY);

                float shapeSize = dataSet.getScatterShapeSize();

                for (int j = 0; j < positions.length * mPhaseX; j += 2) {

                    if (isOffContentRight(positions[j]))
                        break;

                    if (isOffContentLeft(positions[j]) || isOffContentTop(positions[j + 1])
                            || isOffContentBottom(positions[j + 1]))
                        continue;

                    float val = entries.get(j / 2).getYVal();

                    if (mDrawUnitInChart) {

                        mDrawCanvas.drawText(mValueFormatter.getFormattedValue(val),
                                positions[j],
                                positions[j + 1] - shapeSize, mValuePaint);
                    } else {

                        mDrawCanvas.drawText(mValueFormatter.getFormattedValue(val), positions[j],
                                positions[j + 1] - shapeSize,
                                mValuePaint);
                    }
                }
            }
        }
    }

    @Override
    protected void drawHighlights() {

        for (int i = 0; i < mIndicesToHightlight.length; i++) {

            DecartDataSet set = mData.getDataSetByIndex(mIndicesToHightlight[i]
                    .getDataSetIndex());

            if (set == null)
                continue;

            mHighlightPaint.setColor(set.getHighLightColor());

            float xVal = mIndicesToHightlight[i].getDecartEntry().getXVal(); // get the
            // x-position

            float yVal = mIndicesToHightlight[i].getDecartEntry().getYVal();
            // y-position

            float[] pts = new float[]{
                    xVal, mYChartMax, xVal, mYChartMin, 0, yVal, mDeltaX, yVal
            };

            mTrans.pointValuesToPixel(pts);
            // draw the highlight lines
            mDrawCanvas.drawLines(pts, mHighlightPaint);
        }
    }

    @Override
    protected void drawAdditional() {

    }

    /**
     * Returns all possible predefined scattershapes.
     *
     * @return
     */
    public static GraphShape[] getAllPossibleShapes() {
        return new GraphShape[]{
                GraphShape.SQUARE, GraphShape.CIRCLE, GraphShape.TRIANGLE, GraphShape.CROSS
        };
    }
}
