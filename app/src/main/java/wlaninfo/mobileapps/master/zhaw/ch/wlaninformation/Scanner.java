package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Scanner {

    private final WifiManager wifiManager;
    private static boolean scanning = false;
    private final MainActivity activity;
    private final TextView distanceTextView;
    private final TextView rssiTextView;
    private final TextView maxTextView;
    private final TextView minTextView;
    private final TextView deviationTextView;
    private static int loops = 0;
    private static final String MEASURE_NOT_AVAILABLE = "----";
    private final TextView deviationOverallTextView;
    private final TextView minimumOverallTextView;
    private final TextView maximumOverallTextView;
    private final TextView distanceOverallOverallTextView;
    private static List<Double> distanceList = new ArrayList<>();


    private int outputPower;
    private final int frequency;

    public Scanner(MainActivity activity, int outputPower) {
        this.activity = activity;
        Context context = activity.getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        distanceTextView = activity.findViewById(R.id.distance);
        rssiTextView = activity.findViewById(R.id.rssi);
        maxTextView = activity.findViewById(R.id.maximum);
        minTextView = activity.findViewById(R.id.minimum);
        deviationTextView = activity.findViewById(R.id.deviation);
        deviationOverallTextView = activity.findViewById(R.id.deviationOverall);
        minimumOverallTextView = activity.findViewById(R.id.minimumOverall);
        maximumOverallTextView = activity.findViewById(R.id.maximumOverall);
        distanceOverallOverallTextView = activity.findViewById(R.id.distanceOverall);
        this.outputPower = outputPower;
        frequency = wifiManager.getConnectionInfo().getFrequency();
    }

    public static boolean getScanning() {
        return Scanner.scanning;
    }

    public static void setLoops(int loops) {
        Scanner.loops = loops;
    }

    public void scanOnce(int waitInMillis) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(waitInMillis);
                Measurement measurement = sampleRssi(1, 0);
                updateAndRemoveAggregates(measurement);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void scanOverSomeTime(int count, int inveralInMs, int waitInMillis) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(waitInMillis);
                Measurement measurement = sampleRssi(count, inveralInMs);
                updateWithAggregates(measurement);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void scanContinuously(int count, int inveralInMs) {
        Thread thread = new Thread(() -> {
            while (scanning) {
                try {
                    Measurement measurement = sampleRssi(count, inveralInMs);
                    updateAggregatesOverTime(measurement);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setLoops(0);
            setScanning(false);
        });
        thread.start();
    }

    private void updateCurrentMeasures(Measurement measurement) {
        activity.runOnUiThread(() ->
                {
                    rssiTextView.setText(String.valueOf(measurement.getAverageRssi()));
                    double distance = calculateDistance(measurement.getAverageRssi(), frequency);
                    String distanceAsString = formatDistance(distance);
                    distanceTextView.setText(distanceAsString);
                }
        );
    }

    private void updateWithAggregates(Measurement measurement) {
        updateCurrentMeasures(measurement);
        activity.runOnUiThread(() ->
                {
                    double minDistance = calculateDistance(measurement.getMaxRssi(), frequency);
                    double maxDistance = calculateDistance(measurement.getMinRssi(), frequency);
                    double deviation = measurement.getAverageAbsoluteDeviation();
                    minTextView.setText(formatDistance(minDistance));
                    maxTextView.setText(formatDistance(maxDistance));
                    deviationTextView.setText(formatDistance(deviation));
                }
        );
    }

    private void updateAndRemoveAggregates(Measurement measurement) {
        updateCurrentMeasures(measurement);
        activity.runOnUiThread(() ->
                {
                    minTextView.setText(MEASURE_NOT_AVAILABLE);
                    maxTextView.setText(MEASURE_NOT_AVAILABLE);
                    deviationTextView.setText(MEASURE_NOT_AVAILABLE);
                    deviationOverallTextView.setText(MEASURE_NOT_AVAILABLE);
                    distanceOverallOverallTextView.setText(MEASURE_NOT_AVAILABLE);
                    maximumOverallTextView.setText(MEASURE_NOT_AVAILABLE);
                    minimumOverallTextView.setText(MEASURE_NOT_AVAILABLE);
                    deviationOverallTextView.setText(MEASURE_NOT_AVAILABLE);
                }
        );
    }

    private void updateAggregatesOverTime(Measurement measurement) {
        updateWithAggregates(measurement);
        distanceList.addAll(measurement.getDistanceList());
        if (loops == 0) {
            updateWithAggregates(measurement);
        } else {
            updateCurrentMeasures(measurement);

            double minDistance = distanceList.stream().mapToDouble(a -> a).min().getAsDouble();
            double maxDistance = distanceList.stream().mapToDouble(a -> a).max().getAsDouble();
            double averageDistance = distanceList.stream().mapToDouble(a -> a).average().getAsDouble();
            activity.runOnUiThread(() -> {

                minimumOverallTextView.setText(formatDistance(minDistance));
                maximumOverallTextView.setText(formatDistance(maxDistance));
                deviationOverallTextView.setText(formatDistance(getAverageAbsoluteDeviation(distanceList)));
                distanceOverallOverallTextView.setText(formatDistance(averageDistance));
            });

        }
        loops++;
    }


    private String formatDistance(double distance) {
        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(distance);
    }

    public double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (outputPower + 27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }


    public Measurement sampleRssi(int count, int intervalInMs) throws InterruptedException {
        List<Integer> rssiList = new ArrayList<>();
        while (rssiList.size() < count) {
            Thread.sleep(intervalInMs);
            int rssi = wifiManager.getConnectionInfo().getRssi();
            if (rssi != 255) {
                rssiList.add(rssi);
            }
        }
        int averageRssi = (int) Math.round(rssiList.stream().mapToDouble(a -> a).average().getAsDouble());
        int minRssi = (int) Math.round(rssiList.stream().mapToDouble(a -> a).min().getAsDouble());
        int maxRssi = (int) Math.round(rssiList.stream().mapToDouble(a -> a).max().getAsDouble());

        List<Double> distanceList = rssiList.stream().map(a -> calculateDistance(a, frequency)).collect(Collectors.toList());
        double averageAbsoluteDeviation = getAverageAbsoluteDeviation(distanceList);
        return new Measurement(averageRssi, maxRssi, minRssi, averageAbsoluteDeviation, distanceList);
    }

    private double getAverageAbsoluteDeviation(List<Double> distanceList) {
        double averageDistance = distanceList.stream().mapToDouble(a -> a).average().getAsDouble();
        double sumDeviations = 0;
        for (double number : distanceList) {
            sumDeviations += Math.abs(averageDistance - number);
        }

        return sumDeviations / distanceList.size();
    }

    public static void setScanning(boolean scanning) {
        Scanner.scanning = scanning;
    }

    public void setOutputPower(int outputPower) {
        this.outputPower = outputPower;
    }

    public static String getMeasureNotAvailable() {
        return MEASURE_NOT_AVAILABLE;
    }

    public static void clearMeasureList() {
        distanceList.clear();
    }
}
