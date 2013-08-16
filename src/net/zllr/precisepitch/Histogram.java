package net.zllr.precisepitch;

import net.zllr.precisepitch.model.MeasuredPitch;

public class Histogram {
    private static final int MAX_NOTE = 56;
    private double histdata[][];
    
    public Histogram() {
        histdata = new double[MAX_NOTE][100];
    }
    
    public Histogram(Histogram h) {
        this();
        for (int i = 0; i < h.histdata.length; i++) {
            for (int j = 0; j < h.histdata[i].length; j++) {
                histdata[i][j] = h.histdata[i][j];
            }
        }
    }
    
    public void update(MeasuredPitch d) {
        if (d.note >= 0 && d.note < MAX_NOTE) {
            int cent = (int)(d.cent + 50.0);
            cent = Math.max(0, Math.min(cent, 99));
            histdata[d.note][cent]++;
        }
    }
    
    public void reset() {
        for (int i = 0; i < histdata.length; i++) {
            for (int j = 0; j < histdata[i].length; j++) {
                histdata[i][j] = 0.0;
            }
        }
    }
}
