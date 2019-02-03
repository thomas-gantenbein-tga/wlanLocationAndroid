package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final WifiManager wifiManager;
    private static boolean scanning = true;
    private final MainActivity activity;
    private final TextView distanceTextView;
    private final TextView rssiTextView;


    private int outputPower;
    private final int frequency;

    public Scanner(MainActivity activity, int outputPower) {
        this.activity = activity;
        Context context = activity.getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        distanceTextView = activity.findViewById(R.id.distance);
        rssiTextView = activity.findViewById(R.id.rssi);
        this.outputPower = outputPower;
        frequency = wifiManager.getConnectionInfo().getFrequency();
    }

    public static boolean getScanning() {
        return Scanner.scanning;
    }

    public void scanOnce() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Measurement measurement = sampleRssi(1, 0);
                    updateMainActivity(measurement);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void scanOverSomeTime(int count, int inveralInMs) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Measurement measurement = sampleRssi(count, inveralInMs);
                    updateMainActivity(measurement);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void scanContinuously(int count, int inveralInMs) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (scanning) {
                    try {
                        Measurement measurement = sampleRssi(count, inveralInMs);
                        updateMainActivity(measurement);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void updateMainActivity(Measurement measurement) {
        activity.runOnUiThread(() ->
                {
                    rssiTextView.setText(String.valueOf(measurement.getAverageRssi()));
                    double distance = calculateDistance(measurement.getAverageRssi(), frequency);
                    String distanceAsString = formatDistance(distance);
                    distanceTextView.setText(distanceAsString);
                }
        );
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

        double sumDeviations = 0;
        for (int number : rssiList) {
            sumDeviations += Math.abs(averageRssi - number);
        }

        double averageAbsoluteDeviation = sumDeviations / rssiList.size();
        return new Measurement(averageRssi, maxRssi, minRssi, averageAbsoluteDeviation);
    }

    public static void setScanning(boolean scanning) {
        Scanner.scanning = scanning;
    }

    public void setOutputPower(int outputPower) {
        this.outputPower = outputPower;
    }
}
