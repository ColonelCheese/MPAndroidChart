
package com.github.mikephil.charting.data;

import java.util.ArrayList;

public class DecartData extends BarLineScatterCandleData<ScatterDataSet> {

    public DecartData(ArrayList<String> xVals) {
        super(xVals);
    }

    public DecartData(String[] xVals) {
        super(xVals);
    }

    public DecartData(ArrayList<String> xVals, ArrayList<ScatterDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public DecartData(String[] xVals, ArrayList<ScatterDataSet> dataSets) {
        super(xVals, dataSets);
    }

    public DecartData(ArrayList<String> xVals, ScatterDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }

    public DecartData(String[] xVals, ScatterDataSet dataSet) {
        super(xVals, toArrayList(dataSet));
    }

    private static ArrayList<ScatterDataSet> toArrayList(ScatterDataSet dataSet) {
        ArrayList<ScatterDataSet> sets = new ArrayList<ScatterDataSet>();
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

        for (ScatterDataSet set : mDataSets) {
            float size = set.getScatterShapeSize();

            if (size > max)
                max = size;
        }

        return max;
    }
}
