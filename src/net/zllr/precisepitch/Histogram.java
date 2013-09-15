package net.zllr.precisepitch;

import java.io.Serializable;

public class Histogram implements Serializable {
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
}
