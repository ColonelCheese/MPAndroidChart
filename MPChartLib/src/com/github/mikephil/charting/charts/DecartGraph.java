package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.data.DecartData;
import com.github.mikephil.charting.data.DecartDataSet;
import com.github.mikephil.charting.data.DecartEntry;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Decart's orthogonal graph. Draws dots, triangles, squares and custom shapes into the
 * chartview.
 */
public class DecartGraph extends DecartGraphBase<DecartData> {


    private float backgroundInkingMultiplier = 1.2f;

    private List<RectF> filledRects = new LinkedList<>();

    /**
     * enum that defines the shape that is drawn where the values are
     */
    public enum GraphShape {
        CROSS, TRIANGLE, CIRCLE, STROKE_CIRCLE, SQUARE, CUSTOM, LINE, DASHED_LINE, SMOOTHEDLINE, SMOOTHED_AND_DASHED_LINE
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
    protected void calcMinMax(boolean fixedValues) {
        super.calcMinMax(fixedValues);

        if (mDeltaX == 0 && mData.getEntriesCount() > 0) {
            mDeltaX = 1;
            mTouchOffset = 1 * 0.025f;
        }
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

            if (shape == GraphShape.SMOOTHEDLINE) {
                getPaintColor(dataSet, 0);
                drawLine(valuePoints, true);
            } else if (shape == GraphShape.SMOOTHED_AND_DASHED_LINE) {
                drawDashedLine(valuePoints, dataSet.getColor(0), dataSet.getColor(1), true);
            } else if (shape == GraphShape.LINE) {
                getPaintColor(dataSet, 0);
                drawLine(valuePoints, false);
            } else if (shape == GraphShape.DASHED_LINE) {
                drawDashedLine(valuePoints, dataSet.getColor(0), dataSet.getColor(1), false);
            } else {

                for (int j = 0; j < valuePoints.length * mPhaseX; j += 2) {

                    if (isOffContentRight(valuePoints[j]))
                        break;

                    // make sure the lines don't do shitty things outside bounds
                    if (j != 0 && isOffContentLeft(valuePoints[j])
                            && isOffContentTop(valuePoints[j + 1])
                            && isOffContentBottom(valuePoints[j + 1]))
                        continue;

                    // Set the color for the currently drawn value. If the index is
                    // out of bounds, reuse colors.
                    getPaintColor(dataSet, j / 2);
                    float sizeMultiplier = getSizeMultiplyer(dataSet, j / 2);

                    if (shape == GraphShape.SQUARE) {
                        drawSquare(shapeHalf, valuePoints, j, sizeMultiplier);
                    } else if (shape == GraphShape.STROKE_CIRCLE) {
                        Paint tmpPaint = new Paint(mRenderPaint);
                        tmpPaint.setStyle(Paint.Style.STROKE);
                        tmpPaint.setStrokeWidth(3f);
                        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier * backgroundInkingMultiplier,
                                tmpPaint);
                        tmpPaint.setStyle(Paint.Style.FILL);
                        tmpPaint.setColor(Color.WHITE);
                        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier * backgroundInkingMultiplier,
                                tmpPaint);
                    } else if (shape == GraphShape.CIRCLE) {
                        //draw inking
                        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier * backgroundInkingMultiplier,
                                mGridBackgroundPaint);
                        //draw shape
                        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier,
                                mRenderPaint);

                    } else if (shape == GraphShape.CROSS) {

                        mDrawCanvas.drawLine((valuePoints[j] - shapeHalf) * sizeMultiplier,
                                (valuePoints[j + 1]) * sizeMultiplier,
                                (valuePoints[j] + shapeHalf) * sizeMultiplier,
                                (valuePoints[j + 1]) * sizeMultiplier,
                                mRenderPaint);

                        mDrawCanvas.drawLine(valuePoints[j] * sizeMultiplier,
                                (valuePoints[j + 1] - shapeHalf) * sizeMultiplier,
                                valuePoints[j] * sizeMultiplier,
                                (valuePoints[j + 1] + shapeHalf) * sizeMultiplier,
                                mRenderPaint);

                    } else if (shape == GraphShape.TRIANGLE) {
                        //draw inking
                        float shapeHalfMI = shapeHalf * sizeMultiplier * backgroundInkingMultiplier;
                        Path triI = new Path();
                        triI.moveTo(valuePoints[j], valuePoints[j + 1] - shapeHalfMI);
                        triI.lineTo(valuePoints[j] + shapeHalfMI, valuePoints[j + 1] + shapeHalfMI);
                        triI.lineTo(valuePoints[j] - shapeHalfMI, valuePoints[j + 1] + shapeHalfMI);
                        triI.close();

                        mDrawCanvas.drawPath(triI, mGridBackgroundPaint);
                        //draw shape
                        float shapeHalfM = shapeHalf * sizeMultiplier;
                        Path tri = new Path();
                        tri.moveTo(valuePoints[j], valuePoints[j + 1] - shapeHalfM);
                        tri.lineTo(valuePoints[j] + shapeHalfM, valuePoints[j + 1] + shapeHalfM);
                        tri.lineTo(valuePoints[j] - shapeHalfM, valuePoints[j + 1] + shapeHalfM);
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
    }

    private void drawLine(float[] valuePoints, boolean smooth) {
        if (valuePoints.length > 1) {
            Paint.Style prevStyle = mRenderPaint.getStyle();
            mRenderPaint.setStyle(Paint.Style.STROKE);
            Path path;
            if (smooth) {
                path = getSmootedLinePath(valuePoints);
            } else {
                path = getLinePath(valuePoints);
            }
            mDrawCanvas.drawPath(path, mRenderPaint);
            mRenderPaint.setStyle(prevStyle);
        }
    }

    private Path getLinePath(float[] valuePoints) {
        Path path = new Path();
        path.moveTo(valuePoints[0], valuePoints[1]);
        for (int j = 2; j < valuePoints.length; j += 2) {
            path.lineTo(valuePoints[j], valuePoints[j + 1]);
        }
        return path;
    }

    private void drawDashedLine(float[] valuePoints, int firstColor, int secondColor, boolean smooth) {
        if (valuePoints.length > 1) {
            Paint.Style prevStyle = mRenderPaint.getStyle();
            mRenderPaint.setStyle(Paint.Style.STROKE);
            Path path;
            if (smooth) {
                path = getSmootedLinePath(valuePoints);
            } else {
                path = getLinePath(valuePoints);
            }
            mRenderPaint.setColor(firstColor);
            float lineHeight = Utils.convertDpToPixel(15);
            mRenderPaint.setPathEffect(new DashPathEffect(new float[]{lineHeight, lineHeight}, 0));
            mDrawCanvas.drawPath(path, mRenderPaint);

            mRenderPaint.setColor(secondColor);
            mRenderPaint.setPathEffect(new DashPathEffect(new float[]{lineHeight, lineHeight}, lineHeight));
            mDrawCanvas.drawPath(path, mRenderPaint);

            mRenderPaint.setPathEffect(null);
            mRenderPaint.setStyle(prevStyle);
        }
    }

    private Path getSmootedLinePath(float[] valuePoints) {
        Path path = new Path();
        List<CPoint> points = new ArrayList<CPoint>();
        for (int j = 0; j < valuePoints.length; j += 2) {
            points.add(new CPoint(valuePoints[j], valuePoints[j + 1]));
        }
        float mCubicIntensity = 0.1f;
        if (points.size() > 1) {
            for (int j = 0; j < points.size() * mPhaseX; j++) {

                CPoint point = points.get(j);

                if (j == 0) {
                    CPoint next = points.get(j + 1);
                    point.dx = ((next.x - point.x) * mCubicIntensity);
                    point.dy = ((next.y - point.y) * mCubicIntensity);
                } else if (j == points.size() - 1) {
                    CPoint prev = points.get(j - 1);
                    point.dx = ((point.x - prev.x) * mCubicIntensity);
                    point.dy = ((point.y - prev.y) * mCubicIntensity);
                } else {
                    CPoint next = points.get(j + 1);
                    CPoint prev = points.get(j - 1);
                    point.dx = ((next.x - prev.x) * mCubicIntensity);
                    point.dy = ((next.y - prev.y) * mCubicIntensity);
                }

                // create the cubic-spline path
                if (j == 0) {
                    path.moveTo(point.x, point.y * mPhaseY);
                } else {
                    CPoint prev = points.get(j - 1);
                    path.cubicTo(prev.x + prev.dx, (prev.y + prev.dy) * mPhaseY, point.x
                                    - point.dx,
                            (point.y - point.dy) * mPhaseY, point.x, point.y * mPhaseY);
                }
            }
        }
        return path;
    }

    private void drawSquare(float shapeHalf, float[] valuePoints, int j, float sizeMultiplier) {
        //draw inking
        float shapeHalfMI = shapeHalf * sizeMultiplier * backgroundInkingMultiplier;
        mDrawCanvas.drawRect((valuePoints[j] - shapeHalfMI),
                (valuePoints[j + 1] - shapeHalfMI),
                (valuePoints[j] + shapeHalfMI),
                (valuePoints[j + 1] + shapeHalfMI), mGridBackgroundPaint);
        //draw shape
        float shapeHalfM = shapeHalf * sizeMultiplier;
        mDrawCanvas.drawRect((valuePoints[j] - shapeHalfM),
                (valuePoints[j + 1] - shapeHalfM),
                (valuePoints[j] + shapeHalfM),
                (valuePoints[j + 1] + shapeHalfM), mRenderPaint);
    }

    public float getSizeMultiplyer(DecartDataSet dataSet, int j) {
        return 1f;
    }

    public void getPaintColor(DecartDataSet dataSet, int j) {
        mRenderPaint.setColor(dataSet.getColor());
    }

    public String getShapeLabel(DecartDataSet dataSet, int j) {
        float val = ((DecartEntry) dataSet.getEntries().get(j)).getYVal();
        return mValueFormatter.getFormattedValue(val);
    }

    @Override
    protected void drawValues() {
        filledRects.clear();
        // if values are drawn
        if (mDrawYValues && mData.getEntriesCount() < mMaxVisibleCount * mTrans.getScaleX()) {

            ArrayList<DecartDataSet> dataSets = mData
                    .getDataSets();

            for (int i = 0; i < mData.getDataSetCount(); i++) {
                DecartDataSet dataSet = dataSets.get(i);
                if (!dataSet.getDisableValueDrawing()) {
                    ArrayList<DecartEntry> entries = dataSet.getEntries();
                    float[] positions = mTrans.generateTransformedValuesDecart(entries, mPhaseY);
                    for (int j = 0; j < positions.length * mPhaseX; j += 2) {
                        float shapeSize = dataSet.getScatterShapeSize();
                        filledRects.add(new RectF(positions[j] - shapeSize / 2, positions[j + 1] - shapeSize / 2, positions[j] + shapeSize / 2, positions[j + 1] + shapeSize / 2));
                    }
                }
            }
            for (int i = 0; i < mData.getDataSetCount(); i++) {

                DecartDataSet dataSet = dataSets.get(i);
                if (!dataSet.getDisableValueDrawing()) {
                    ArrayList<DecartEntry> entries = dataSet.getEntries();

                    float[] positions = mTrans.generateTransformedValuesDecart(entries, mPhaseY);

                    float shapeSize = dataSet.getScatterShapeSize();

                    for (int j = 0; j < positions.length * mPhaseX; j += 2) {

                        float pointX = positions[j];
                        if (isOffContentRight(pointX))
                            break;

                        float pointY = positions[j + 1];
                        if (isOffContentLeft(pointX) || isOffContentTop(pointY)
                                || isOffContentBottom(pointY))
                            continue;

                        String shapeLabel = getShapeLabel(dataSet, j / 2);

                        if (mDrawUnitInChart) {
                            mDrawCanvas.drawText(shapeLabel,
                                    positions[j],
                                    positions[j + 1] - shapeSize, mValuePaint);
                        } else {
                            float textWidth = mValuePaint.measureText(shapeLabel);
                            mGridBackgroundPaint.setAlpha(200);
                            //FIXME: replace magic numbers by dimens and add separate Paint for text bg
                            RectF rect = new RectF(
                                    pointX + shapeSize + 4,
                                    pointY - mValuePaint.getTextSize() + 5,
                                    pointX + shapeSize + textWidth + 4,
                                    pointY + mValuePaint.getTextSize() + 5);
                            boolean intersects = false;
                            for (RectF filledRect : filledRects) {
                                if (rect.intersect(filledRect)) {
                                    intersects = true;
                                    break;
                                }
                            }
                            if (intersects) {
                                //try to draw on left
                                RectF rectL = new RectF(
                                        pointX - shapeSize - textWidth - 4,
                                        pointY - mValuePaint.getTextSize() + 5,
                                        pointX - shapeSize - 4,
                                        pointY + mValuePaint.getTextSize() + 5);
                                boolean intersectsL = false;
                                for (RectF filledRect : filledRects) {
                                    if (rectL.intersect(filledRect)) {
                                        intersectsL = true;
                                        break;
                                    }
                                }
                                if (intersectsL) {
                                    continue;
                                } else {
                                    if (!isOffContentRect(rectL)) {
                                        mDrawCanvas.drawRoundRect(rectL, 8f, 8f,
                                                mGridBackgroundPaint);
                                        mDrawCanvas.drawText(shapeLabel,
                                                pointX - textWidth / 2f - 4 - shapeSize,
                                                pointY + mValuePaint.getTextSize() / 2,
                                                mValuePaint);
                                        filledRects.add(new RectF(rectL.left, pointY - shapeSize / 2, pointX + shapeSize / 2, pointY + shapeSize / 2));
                                    }
                                    continue;
                                }
                            }
                            //drawTextOnRightSide:
                            if (!isOffContentRect(rect)) {
                                mDrawCanvas.drawRoundRect(rect, 8f, 8f,
                                        mGridBackgroundPaint);
                                mDrawCanvas.drawText(shapeLabel,
                                        pointX + textWidth / 2f + 4 + shapeSize,
                                        pointY + mValuePaint.getTextSize() / 2,
                                        mValuePaint);
                                filledRects.add(new RectF(pointX - shapeSize / 2, pointY - shapeSize / 2, rect.right, pointY + shapeSize / 2));
                            }
                        }
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

    class CPoint {
        float x, y;
        float dx, dy;

        CPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }
}
