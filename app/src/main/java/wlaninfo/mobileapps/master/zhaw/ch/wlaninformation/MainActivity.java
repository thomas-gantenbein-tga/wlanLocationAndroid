package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_WIFI = 1;
    private WifiManager wifiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public static double calculateDistance(double levelInDb, double freqInMHz) {
        double exp = (16 + 27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
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
        for(int i = 0; i < 10; i++) {
            rssiList.add(wifiManager.getConnectionInfo().getRssi());
            Thread.sleep(100);
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

    public void connect(View view) {
        TextView ssidTextView = findViewById(R.id.ssid);
        TextView passwordTextView = findViewById(R.id.password);
        String networkSSID = ssidTextView.getText().toString();
        String networkPass = passwordTextView.getText().toString();

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\"" + networkPass + "\"";


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_DENIED) {
            //if not, ask the user for this permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_WIFI);
        } else {
            wifiManager.addNetwork(conf);
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                }
            }
        }
    }

    public void stopScan(View view) {
        Scanner.setScanning(false);
    }

    public void startContinuousScan(View view) {
        Thread thread = new Thread(new Scanner(wifiManager, this));
        thread.start();
    }

    public void updateUi(int rssi, String distance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView distanceTextView = findViewById(R.id.distance);
                distanceTextView.setText(distance);
                Log.d("lkj", "lkj");
                TextView rssiTextView = findViewById(R.id.rssi);
                rssiTextView.setText(String.valueOf(rssi));
            }
        });

    }


}
