package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static int outputPower = 0;
    private WifiManager wifiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public static double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (outputPower + 27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    public void sampleDistanceOnce(View view) {
        String rssiAsString = String.valueOf(wifiManager.getConnectionInfo().getRssi());
        TextView rssiTextView = findViewById(R.id.rssi);
        rssiTextView.setText(rssiAsString);

        double distance = calculateDistance(wifiManager.getConnectionInfo().getRssi(), wifiManager.getConnectionInfo().getFrequency());
        DecimalFormat df = new DecimalFormat("###.##");
        String distanceAsString = df.format(distance);
        TextView distanceTextView = findViewById(R.id.distance);
        distanceTextView.setText(distanceAsString);
    }

    public void sampleDistanceOverTime(View view) throws InterruptedException {
        List<Integer> rssiList = new ArrayList<>();
        while(rssiList.size() < 10) {
            int rssi = wifiManager.getConnectionInfo().getRssi();
            if (rssi != 255) {
                rssiList.add(rssi);
                Thread.sleep(100);
            }
        }
        int averageRssi = (int) Math.round(rssiList.stream().mapToDouble(a -> a).average().getAsDouble());
        TextView rssiTextView = findViewById(R.id.rssi);
        rssiTextView.setText(String.valueOf(averageRssi));

        double distance = calculateDistance(averageRssi, wifiManager.getConnectionInfo().getFrequency());
        DecimalFormat df = new DecimalFormat("###.##");
        String distanceAsString = df.format(distance);
        TextView distanceTextView = findViewById(R.id.distance);
        distanceTextView.setText(distanceAsString);
    }

    public void updateOutputPower(View view) {
        TextView outputPowerTextView = findViewById(R.id.outputPower);
        int outputPower = Integer.parseInt(outputPowerTextView.getText().toString());
        MainActivity.outputPower = outputPower;

    }

    public void stopScan(View view) {
        Scanner.setScanning(false);
    }

    public void startContinuousScan(View view) {
        if (!Scanner.getScanning()) {
            Scanner.setScanning(true);
            Thread thread = new Thread(new Scanner(wifiManager, this));
            thread.start();
        }
    }

    public void updateUi(int rssi, String distance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

    }



}
