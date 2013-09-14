package net.zllr.precisepitch;

import net.zllr.precisepitch.model.MeasuredPitch;

public class Histogram {
    private static final int MAX_NOTE = 59;
    private int histdata[][]; //histdata[note][cent]
    private int maxCount[];   //maxCount[note]
    
    public Histogram() {
        histdata = new int[MAX_NOTE][100];
        maxCount = new int[MAX_NOTE];
    }
    
    public Histogram(Histogram h) {
        this();
        for (int i = 0; i < h.histdata.length; i++) {
            for (int j = 0; j < h.histdata[i].length; j++) {
                histdata[i][j] = h.histdata[i][j];
            }
            maxCount[i] = h.maxCount[i];
        }
    }
    
    public void update(MeasuredPitch d) {
        if (d.note >= 0 && d.note < MAX_NOTE) {
            int cent = (int)(d.cent + 50.0);
            cent = Math.max(0, Math.min(cent, 99));
            histdata[d.note][cent]++;
            if (maxCount[d.note] < histdata[d.note][cent]) {
                maxCount[d.note] = histdata[d.note][cent];
            }
        }
    }
    
    public int count(int note, int cent) {
        return histdata[note][cent + 50];
    }
    
    public double normalized(int note, int cent) {
        return (double)histdata[note][cent + 50] / (double)maxCount[note];
    }
    
    public void reset() {
        for (int i = 0; i < histdata.length; i++) {
            for (int j = 0; j < histdata[i].length; j++) {
                histdata[i][j] = 0;
            }
            maxCount[i] = 0;
        }
    }
}
