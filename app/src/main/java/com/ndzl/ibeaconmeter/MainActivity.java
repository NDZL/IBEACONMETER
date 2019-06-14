package com.ndzl.ibeaconmeter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.*;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    public static final String TAG = "ndzl-iBeacon-Meter";
    private BeaconManager beaconManager;
    TextView tvOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        tvOut = findViewById(R.id.tv_message);

        if (   checkSelfPermission(  android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(  android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 3003);
        }
        else
            startBeaconLibrary();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 3003: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startBeaconLibrary();

                }
            }
        }
    }

    void startBeaconLibrary(){
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    String toBePrinted = "x";
    void PrintOnScreen(String _s){
        toBePrinted = _s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String _old = tvOut.getText().toString();
                tvOut.setText(toBePrinted+"\n"+_old);
            }
        });
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("FE913213-B311-4A42-8C16-47FAEAC938DB", null, null, null));
        } catch (RemoteException e) {    }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().toString());

                    for (Object _b : beacons) {
                        String btAddr = ((Beacon)_b).getBluetoothAddress();
                        String bRssi = ""+((Beacon)_b).getRssi();
                        PrintOnScreen(btAddr+" "+bRssi+" dBm");
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("FE913213-B311-4A42-8C16-47FAEAC938DB", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}

