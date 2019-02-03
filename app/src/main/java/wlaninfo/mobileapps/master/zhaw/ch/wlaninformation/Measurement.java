package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

public class Measurement {
    private int averageRssi;
    private int maxRssi;
    private int minRssi;
    private double averageAbsoluteDeviation;

    public Measurement(int averageRssi, int maxRssi, int minRssi, double deviation) {
        this.averageRssi = averageRssi;
        this.maxRssi = maxRssi;
        this.minRssi = minRssi;
        this.averageAbsoluteDeviation = deviation;
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
}
