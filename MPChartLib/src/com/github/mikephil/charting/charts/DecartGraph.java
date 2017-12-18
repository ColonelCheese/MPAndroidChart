package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.data.DecartData;
import com.github.mikephil.charting.data.DecartDataSet;
import com.github.mikephil.charting.data.DecartEntry;
import com.github.mikephil.charting.utils.DecartHighlight;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Decart's orthogonal graph. Draws dots, triangles, squares and custom shapes into the
 * chartview.
 */
public class DecartGraph extends DecartGraphBase<DecartData> {

    protected int offContentRectWidthDp = 5;

    protected boolean showOutBounds;

    protected boolean drawInking;

    protected boolean drawInkingOutBounds;

    protected float backgroundInkingMultiplier = 1.2f;

    protected List<RectF> filledRects = new LinkedList<>();

    protected static final float highlightRadius = Utils.convertDpToPixel(36);

    protected static final float highlightWhiteRadius = Utils.convertDpToPixel(18);

    protected int highlightAlpha = 60;

    /**
     * enum that defines the shape that is drawn where the values are
     */
    public enum GraphShape {
        CROSS, TRIANGLE, CIRCLE, STROKE_CIRCLE, SQUARE, CUSTOM, LINE, DASHED_LINE, SMOOTHEDLINE, SMOOTHED_AND_DASHED_LINE,
        CIRCLE_HIGHLIGHT, TRIANGLE_HIGHLIGHT
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

    public void setShowOutBounds(boolean showOutBounds) {
        this.showOutBounds = showOutBounds;
        invalidate();
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

            int shapeHalf = dataSet.getScatterShapeSize() / 2;

            float[] valuePoints = mTrans.generateTransformedValuesDecart(entries, mPhaseY);

            GraphShape shape = getDataSetShape(dataSet);

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

                int offContentRectWidthPx = Math.round(Utils.convertDpToPixel(offContentRectWidthDp));
                for (int j = 0; j < valuePoints.length * mPhaseX; j += 2) { // valuePoints[j] - pointX, valuePoints[j+1] - pointY

                    shape = getEntryShape(dataSet, j / 2);

                    // Set the color for the currently drawn value. If the index is
                    // out of bounds, reuse colors.
                    getPaintColor(dataSet, j / 2);
                    float sizeMultiplier = getSizeMultiplyer(dataSet, j / 2);

                    int initialRenderAlpha = mRenderPaint.getAlpha();
                    mRenderPaint.setAlpha(dataSet.getScatterShapeAlpha());
                    int initialBackgroundAlpha = mGridBackgroundPaint.getAlpha();
                    mGridBackgroundPaint.setAlpha(dataSet.getScatterShapeAlpha());

                    int offContentRectHeightPx = Math.round(shapeHalf * sizeMultiplier * (drawInkingOutBounds ? backgroundInkingMultiplier : 1));

                    if (showOutBounds && isOffContentRight(valuePoints[j] - shapeHalf)) {
                        float[] vals = new float[2];
                        vals[0] = mContentRect.right;
                        vals[1] = valuePoints[j + 1];
                        drawRect(offContentRectWidthPx, offContentRectHeightPx, vals, 0, drawInkingOutBounds);
                    }
                    if (showOutBounds && isOffContentLeft(valuePoints[j] + shapeHalf)) {
                        float[] vals = new float[2];
                        vals[0] = mContentRect.left;
                        vals[1] = valuePoints[j + 1];
                        drawRect(offContentRectWidthPx, offContentRectHeightPx, vals, 0, drawInkingOutBounds);
                    }
                    if (showOutBounds && isOffContentBottom(valuePoints[j + 1] - shapeHalf)) {
                        float[] vals = new float[2];
                        vals[0] = valuePoints[j];
                        vals[1] = mContentRect.bottom;
                        drawRect(offContentRectHeightPx, offContentRectWidthPx, vals, 0, drawInkingOutBounds);
                    }
                    if (showOutBounds && isOffContentTop(valuePoints[j + 1] + shapeHalf)) {
                        float[] vals = new float[2];
                        vals[0] = valuePoints[j];
                        vals[1] = mContentRect.top;
                        drawRect(offContentRectHeightPx, offContentRectWidthPx, vals, 0, drawInkingOutBounds);
                    }

                    if (j != 0 && isOffContentLeft(valuePoints[j])
                            && isOffContentTop(valuePoints[j + 1])
                            && isOffContentBottom(valuePoints[j + 1])) {
                        continue;
                    }

                    if (shape == GraphShape.SQUARE) {
                        drawSquare(shapeHalf, valuePoints, j, sizeMultiplier, drawInking);
                    } else if (shape == GraphShape.STROKE_CIRCLE) {
                        drawStrokeCircle(shapeHalf, valuePoints, j, sizeMultiplier);
                    } else if (shape == GraphShape.CIRCLE) {
                        drawCircle(shapeHalf, valuePoints, j, sizeMultiplier, drawInking);
                    } else if (shape == GraphShape.CROSS) {
                        drawCross(shapeHalf, valuePoints, j, sizeMultiplier);
                    } else if (shape == GraphShape.TRIANGLE) {
                        drawTrianlge(shapeHalf, valuePoints, j, sizeMultiplier, drawInking);
                    } else if (shape == GraphShape.CIRCLE_HIGHLIGHT) {
                        drawTransparentStrokeCircle(shapeHalf, valuePoints, j, sizeMultiplier * 2);
                        drawCircle(shapeHalf, valuePoints, j, sizeMultiplier, drawInking);
                    } else if (shape == GraphShape.TRIANGLE_HIGHLIGHT) {
                        drawTransparentStrokeTrianlge(shapeHalf, valuePoints, j, sizeMultiplier * 2);
                        drawTrianlge(shapeHalf, valuePoints, j, sizeMultiplier, drawInking);
                    } else if (shape == GraphShape.CUSTOM) {

                        Path customShape = dataSet.getCustomScatterShape();

                        if (customShape == null)
                            return;

                        // transform the provided custom path
                        mTrans.pathValueToPixel(customShape);
                        mDrawCanvas.drawPath(customShape, mRenderPaint);
                    }
                    Bitmap customShapeBitmap = getCustomShapeBitmap(dataSet, j / 2);
                    if (customShapeBitmap != null) {
                        drawCustomShapeBitmap(customShapeBitmap, valuePoints, j, shapeHalf, sizeMultiplier);
                    }
                    mRenderPaint.setAlpha(initialRenderAlpha);
                    mGridBackgroundPaint.setAlpha(initialBackgroundAlpha);
                }
            }
        }
    }

    protected GraphShape getEntryShape(DecartDataSet dataSet, int i) {
        return getDataSetShape(dataSet);
    }

    protected GraphShape getDataSetShape(DecartDataSet dataSet) {
        return dataSet.getScatterShape();
    }

    private void drawTransparentStrokeTrianlge(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier) {
        int initialColor = mRenderPaint.getColor();
        Paint.Style initialStyle = mRenderPaint.getStyle();
        float initialStrokeWidth = mRenderPaint.getStrokeWidth();
        int initialAlpha = mRenderPaint.getAlpha();

        mRenderPaint.setStyle(Paint.Style.STROKE);
        mRenderPaint.setStrokeWidth(3f);

        float shapeHalfM = shapeHalf * sizeMultiplier;
        drawSimpleTriangle(valuePoints[j], valuePoints[j + 1], shapeHalfM, mRenderPaint);

        mRenderPaint.setStyle(Paint.Style.FILL);
        mRenderPaint.setColor(Color.WHITE);
        mRenderPaint.setAlpha(highlightAlpha);

        drawSimpleTriangle(valuePoints[j], valuePoints[j + 1], shapeHalfM, mRenderPaint);

        mRenderPaint.setColor(initialColor);
        mRenderPaint.setStyle(initialStyle);
        mRenderPaint.setStrokeWidth(initialStrokeWidth);
        mRenderPaint.setAlpha(initialAlpha);
    }

    private void drawTrianlge(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier, boolean drawInking) {
        if (drawInking) {
            //draw inking
            float shapeHalfMI = shapeHalf * sizeMultiplier * backgroundInkingMultiplier;
            drawSimpleTriangle(valuePoints[j], valuePoints[j + 1], shapeHalfMI, mGridBackgroundPaint);
        }

        //draw shape
        float shapeHalfM = shapeHalf * sizeMultiplier;
        drawSimpleTriangle(valuePoints[j], valuePoints[j + 1], shapeHalfM, mRenderPaint);
    }

    private void drawSimpleTriangle(float x, float y, float rad, Paint mRenderPaint) {
        Path tri = new Path();
        tri.moveTo(x - 0.866f * rad, y + 0.5f * rad);//   /
        tri.lineTo(x, y - rad);//       /
        tri.lineTo(x + 0.866f * rad, y + 0.5f * rad);//    \
        tri.close();//                _

        mDrawCanvas.drawPath(tri, mRenderPaint);
    }

    private void drawCross(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier) {
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
    }

    private void drawCircle(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier, boolean drawInking) {
        if (drawInking) {
            //draw inking
            mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier * backgroundInkingMultiplier,
                    mGridBackgroundPaint);
        }
        //draw shape
        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier,
                mRenderPaint);
    }

    private void drawStrokeCircle(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier) {
        int initialColor = mRenderPaint.getColor();
        Paint.Style initialStyle = mRenderPaint.getStyle();
        float initialStrokeWidth = mRenderPaint.getStrokeWidth();
        mRenderPaint.setStyle(Paint.Style.STROKE);
        mRenderPaint.setStrokeWidth(3f);
        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier,
                mRenderPaint);

        mRenderPaint.setStyle(Paint.Style.FILL);
        mRenderPaint.setColor(Color.WHITE);
        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier,
                mRenderPaint);
        mRenderPaint.setColor(initialColor);
        mRenderPaint.setStyle(initialStyle);
        mRenderPaint.setStrokeWidth(initialStrokeWidth);
    }

    private void drawTransparentStrokeCircle(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier) {
        int initialColor = mRenderPaint.getColor();
        Paint.Style initialStyle = mRenderPaint.getStyle();
        float initialStrokeWidth = mRenderPaint.getStrokeWidth();
        int initialAlpha = mRenderPaint.getAlpha();

        mRenderPaint.setStyle(Paint.Style.STROKE);
        mRenderPaint.setStrokeWidth(3f);
        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier,
                mRenderPaint);

        mRenderPaint.setStyle(Paint.Style.FILL);
        mRenderPaint.setColor(Color.WHITE);
        mRenderPaint.setAlpha(highlightAlpha);
        mDrawCanvas.drawCircle(valuePoints[j], valuePoints[j + 1], shapeHalf * sizeMultiplier,
                mRenderPaint);

        mRenderPaint.setAlpha(initialAlpha);
        mRenderPaint.setColor(initialColor);
        mRenderPaint.setStyle(initialStyle);
        mRenderPaint.setStrokeWidth(initialStrokeWidth);
    }

    private void drawRect(int width, int height, float[] valuePoints, int j, boolean drawInking) {
        if (drawInking) {
            //draw inking
            float widthHalfMI = width;
            float heightHalfMI = height;
            mDrawCanvas.drawRect((valuePoints[j] - widthHalfMI),
                    (valuePoints[j + 1] - heightHalfMI),
                    (valuePoints[j] + widthHalfMI),
                    (valuePoints[j + 1] + heightHalfMI), mGridBackgroundPaint);
        }
        //draw shape
        float widthHalfMI = width;
        float heightHalfMI = height;
        mDrawCanvas.drawRect((valuePoints[j] - widthHalfMI),
                (valuePoints[j + 1] - heightHalfMI),
                (valuePoints[j] + widthHalfMI),
                (valuePoints[j + 1] + heightHalfMI), mRenderPaint);
    }

    private void drawSquare(int shapeHalf, float[] valuePoints, int j, float sizeMultiplier, boolean drawInking) {
        if (drawInking) {
            //draw inking
            float shapeHalfMI = shapeHalf * sizeMultiplier * backgroundInkingMultiplier;
            mDrawCanvas.drawRect((valuePoints[j] - shapeHalfMI),
                    (valuePoints[j + 1] - shapeHalfMI),
                    (valuePoints[j] + shapeHalfMI),
                    (valuePoints[j + 1] + shapeHalfMI), mGridBackgroundPaint);
        }
        //draw shape
        float shapeHalfM = shapeHalf * sizeMultiplier;
        mDrawCanvas.drawRect((valuePoints[j] - shapeHalfM),
                (valuePoints[j + 1] - shapeHalfM),
                (valuePoints[j] + shapeHalfM),
                (valuePoints[j + 1] + shapeHalfM), mRenderPaint);
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

    protected void drawCustomShapeBitmap(Bitmap bitmap, float[] valuePoints, int j, int shapeHalf, float sizeMultiplier) {
        float sideHalfSize = shapeHalf * sizeMultiplier;
        float left = valuePoints[j] - sideHalfSize;
        float top = valuePoints[j + 1] - sideHalfSize;
        float right = valuePoints[j] + sideHalfSize;
        float bottom = valuePoints[j + 1] + sideHalfSize;
        RectF dstRect = new RectF(left, top, right, bottom);
        mDrawCanvas.drawBitmap(bitmap, null,
                dstRect,
                mRenderPaint);
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

    protected float getSizeMultiplyer(DecartDataSet dataSet, int j) {
        return 1f;
    }

    protected Bitmap getCustomShapeBitmap(DecartDataSet dataSet, int j) {
        return null;
    }

    public void getPaintColor(DecartDataSet dataSet, int itemIndex) {
        mRenderPaint.setColor(dataSet.getColor());
    }

    public String getShapeLabel(DecartDataSet dataSet, int itemIndex) {
        float val = ((DecartEntry) dataSet.getEntries().get(itemIndex)).getYVal();
        return mValueFormatter.getFormattedValue(val);
    }

    @Override
    protected void drawValues() {
        filledRects.clear();
        // if values are drawn
        if (mDrawYValues && mData.getEntriesCount() < mMaxVisibleCount * mTrans.getScaleX()) {
            ArrayList<DecartDataSet> dataSets = mData.getDataSets();

            for (int i = 0; i < mData.getDataSetCount(); i++) {
                DecartDataSet dataSet = dataSets.get(i);

                if (!dataSet.getDisableValueDrawing()) {
                    ArrayList<DecartEntry> entries = dataSet.getEntries();

                    float[] positions = mTrans.generateTransformedValuesDecart(entries, mPhaseY);


                    for (int j = 0; j < positions.length * mPhaseX; j += 2) {
                        float shapeHalf = dataSet.getScatterShapeSize() / 2;
                        float sizeMultiplier = getSizeMultiplyer(dataSet, j / 2);

                        float left = positions[j] - shapeHalf * sizeMultiplier;
                        float top = positions[j + 1] - shapeHalf * sizeMultiplier;
                        float right = positions[j] + shapeHalf * sizeMultiplier;
                        float bottom = positions[j + 1] + shapeHalf * sizeMultiplier;

                        filledRects.add(new RectF(left, top, right, bottom));
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
                            continue;

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
                            //mGridBackgroundPaint.setAlpha(200);
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
                                        //mDrawCanvas.drawRoundRect(rectL, 8f, 8f, mGridBackgroundPaint);
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
                                //mDrawCanvas.drawRoundRect(rect, 8f, 8f, mGridBackgroundPaint);
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
            DecartDataSet set = mData.getDataSetByIndex(mIndicesToHightlight[i].getDataSetIndex());
            if (set == null)
                continue;
            float xVal = mIndicesToHightlight[i].getDecartEntry().getXVal(); // get the
            // x-position
            float yVal = mIndicesToHightlight[i].getDecartEntry().getYVal();
            // y-position
            float[] pts = new float[]{
                    xVal, mYChartMax, xVal, mYChartMin, 0, yVal, mDeltaX, yVal
            };
            mTrans.pointValuesToPixel(pts);
            // draw the highlight lines
            drawHighlight(mIndicesToHightlight[i], pts);
        }
    }

    private void drawHighlight(DecartHighlight decartHighlight, float[] pts) {
        getPaintColor(mData.getDataSetByIndex(decartHighlight.getDataSetIndex()), decartHighlight.getmEntryIndex());
        mHighlightPaint.setColor(mRenderPaint.getColor());
        mHighlightPaint.setAlpha(highlightAlpha);
        float x = pts[0];
        float y = pts[5];
        mDrawCanvas.drawCircle(x, y, getHighlightRadius(mData.getDataSetByIndex(decartHighlight.getDataSetIndex()), decartHighlight.getmEntryIndex()), mHighlightPaint);
        mHighlightPaint.setColor(Color.WHITE);
        GraphShape scatterShape = mData.getDataSetByIndex(decartHighlight.getDataSetIndex()).getScatterShape();
        if (scatterShape == GraphShape.TRIANGLE) {
            drawSimpleTriangle(x, y, getWhiteHighlightRadius(mData.getDataSetByIndex(decartHighlight.getDataSetIndex()), decartHighlight.getmEntryIndex()), mHighlightPaint);
        } else {
            mDrawCanvas.drawCircle(x, y, getWhiteHighlightRadius(mData.getDataSetByIndex(decartHighlight.getDataSetIndex()), decartHighlight.getmEntryIndex()), mHighlightPaint);
        }
    }

    private float getWhiteHighlightRadius(DecartDataSet dataSet, int j) {
        float sizeM = getSizeMultiplyer(dataSet, j);
        float size = dataSet.getScatterShapeSize();
        return 0;
    }

    protected float getHighlightRadius(DecartDataSet dataSet, int j) {
        float sizeM = getSizeMultiplyer(dataSet, j);
        float size = dataSet.getScatterShapeSize();
        return size * 1.25f * sizeM;
    }

    @Override
    protected void drawAdditional() {
        //nothing to draw
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

    @Override
    protected float getYValsNearXValueRound() {
        return super.getYValsNearXValueRound() * 0.0025f;
    }
}
