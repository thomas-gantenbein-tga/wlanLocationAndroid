package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import java.util.List;

public class Measurement {
    private int averageRssi;
    private int maxRssi;
    private int minRssi;
    private double averageAbsoluteDeviation;
    private List<Double> distanceList;

    public Measurement(int averageRssi, int maxRssi, int minRssi, double deviation, List<Double> distanceList) {
        this.averageRssi = averageRssi;
        this.maxRssi = maxRssi;
        this.minRssi = minRssi;
        this.averageAbsoluteDeviation = deviation;
        this.distanceList = distanceList;
    }

    public int getAverageRssi() {
        return averageRssi;
    }

    public int getMaxRssi() {
        return maxRssi;
    }

    public int getMinRssi() {
        return minRssi;
    }

    public double getAverageAbsoluteDeviation() {
        return averageAbsoluteDeviation;
    }

    public List<Double> getDistanceList() {
        return distanceList;
    }
}
