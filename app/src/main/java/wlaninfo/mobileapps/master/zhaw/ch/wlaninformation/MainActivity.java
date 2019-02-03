package wlaninfo.mobileapps.master.zhaw.ch.wlaninformation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static int outputPower = 0;
    private Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanner = new Scanner(this, outputPower);
    }


    public void sampleDistanceOnce(View view) {
        scanner.scanOnce();
    }

    public void sampleDistanceOverTime(View view) throws InterruptedException {
        scanner.scanOverSomeTime(10, 100);
    }

    public void updateOutputPower(View view) {
        TextView outputPowerTextView = findViewById(R.id.outputPower);
        scanner.setOutputPower(Integer.parseInt(outputPowerTextView.getText().toString()));
    }

    public void stopScan(View view) {
        Scanner.setScanning(false);
    }

    public void startContinuousScan(View view) {
        if (!Scanner.getScanning()) {
            Scanner.setScanning(true);
            scanner.scanContinuously(10, 100);
        }
    }

}
