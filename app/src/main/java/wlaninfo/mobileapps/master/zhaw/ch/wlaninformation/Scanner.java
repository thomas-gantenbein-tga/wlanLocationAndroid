package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Scanner implements Runnable {

    private final WifiManager wifiManager;
    private static boolean scanning = true;
    private final MainActivity activity;

    public Scanner(WifiManager wifiManager, MainActivity activity) {
        this.wifiManager = wifiManager;
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            while(scanning) {
                int averageRssi = sampleDistanceOverTime();
                double distance = MainActivity.calculateDistance(averageRssi, wifiManager.getConnectionInfo().getFrequency());
                DecimalFormat df = new DecimalFormat("###.##");
                String distanceAsString = df.format(distance);
                activity.updateUi(averageRssi, distanceAsString);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setScanning(boolean scanning) {
        Scanner.scanning = scanning;
    }

    public int sampleDistanceOverTime() throws InterruptedException {
        List<Integer> rssiList = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            rssiList.add(wifiManager.getConnectionInfo().getRssi());
            Thread.sleep(100);
        }
        return (int) Math.round(rssiList.stream().mapToDouble(a -> a).average().getAsDouble());

    }
}
