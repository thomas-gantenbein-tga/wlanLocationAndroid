package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int attenuationParam = 15;
        scanner = new Scanner(this, attenuationParam);
    }


    public void sampleDistanceOnce(View view) {
        stopScan(view);
        scanner.scanOnce(1000);
    }

    public void sampleDistanceOverTime(View view) throws InterruptedException {
        stopScan(view);
        scanner.scanOverSomeTime(10, 500, 1000);
    }

    public void updateOutputPower(View view) {
        TextView outputPowerTextView = findViewById(R.id.outputPower);
        scanner.setOutputPower(Integer.parseInt(outputPowerTextView.getText().toString()));
    }

    public void stopScan(View view) {
        Scanner.setScanning(false);
        Scanner.clearMeasureList();
        ((TextView) findViewById(R.id.distanceOverall)).setText(Scanner.getMeasureNotAvailable());
        ((TextView) findViewById(R.id.minimumOverall)).setText(Scanner.getMeasureNotAvailable());
        ((TextView) findViewById(R.id.maximumOverall)).setText(Scanner.getMeasureNotAvailable());
        ((TextView) findViewById(R.id.deviationOverall)).setText(Scanner.getMeasureNotAvailable());
    }

    public void startContinuousScan(View view) {
        if (!Scanner.getScanning()) {
            Scanner.setScanning(true);
            scanner.scanContinuously(10, 500);
        }
    }

}
