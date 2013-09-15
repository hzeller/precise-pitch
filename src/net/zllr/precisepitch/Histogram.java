package net.zllr.precisepitch;

import android.graphics.Color;

public class Histogram {
    private int histData[];
    private int maxCount;
    
    private boolean filtered;
    private double filteredData[];
    
    private static double gaussian(int pos) {
        double sigma = 2.0;
        return Math.exp(-(0.5 *
                (((double)pos)/ sigma) *
                (((double)pos)/ sigma) )) / (sigma * 2.50662827463) ;
    }
    
    public Histogram(int buckets) {
        histData = new int[buckets];
        filteredData = new double[buckets];
        maxCount = 0;
        filtered = false;
    }
    
    public Histogram(Histogram h) {
        this(h.histData.length);
        for (int i = 0; i < h.histData.length; i++) {
            histData[i] = h.histData[i];
        }
        maxCount = h.maxCount;
        filtered = h.filtered;
    }
    
    public void increment(int bucket) {
        histData[bucket]++;
        if (maxCount < histData[bucket]) {
            maxCount = histData[bucket];
        }
        filtered = false;
    }
    
    public int count(int bucket) {
        return histData[bucket];
    }
    
    public double normalized(int bucket) {
        if (maxCount == 0) {
            return 0.0;
        } else {
            return (double)histData[bucket] / (double)maxCount;
        }
    }
    
    public void filter(int radius) {
        if (!filtered) {
            double maxFiltered = 0.0;
            for (int i = 0; i < filteredData.length; i++) {
                int tapStart = Math.max(0, i - radius);
                int tapStop = Math.min(i + radius, histData.length - 1);
                filteredData[i] = 0.0;
                for (int j = tapStart; j < tapStop; j++) {
                    filteredData[i] += histData[j] * gaussian(i - j);
                }
                if (maxFiltered < filteredData[i]) {
                    maxFiltered = filteredData[i];
                }
            }
            for (int i = 0; i < filteredData.length; i++) {
                filteredData[i] /= maxFiltered;
            }
            filtered = true;
        }
    }
    
    public double filtered(int bucket) {
        if (filtered) {
            return filteredData[bucket];
        } else {
            return normalized(bucket);
        }
    }
    
    public void reset() {
        for (int i = 0; i < histData.length; i++) {
            histData[i] = 0;
        }
        maxCount = 0;
        filtered = false;
    }
    
    public static int getJetColor(double value) {
        //value from 0.0 to 1.0
        int fourValue = (int)(4.0 * 256.0 * value);
        int red   = Math.min(fourValue - 384, -fourValue + 1152);
        int green = Math.min(fourValue - 128, -fourValue + 896);
        int blue  = Math.min(fourValue + 128, -fourValue + 640);
        red   = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue  = Math.min(255, Math.max(0, blue));
        
        return Color.rgb(red, green, blue);
    }
    
    public static int getBlackBlueColor(double value) {
        int blue = (int)(value * 255.0);
        int redgreen = (int)(value * 0xBB);
        return Color.rgb(redgreen, redgreen, blue);
    }
    
    public static int getBlackRedColor(double value) {
        int red = (int)(value * 255.0);
        int greenblue = (int)(value * 0xBB);
        return Color.rgb(red, greenblue, greenblue);
    }
}
