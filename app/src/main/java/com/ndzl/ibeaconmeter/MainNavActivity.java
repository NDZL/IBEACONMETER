package com.ndzl.ibeaconmeter;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainNavActivity extends AppCompatActivity implements BeaconConsumer {
    private TextView mTextMessage;
    private BeaconManager beaconManager;
    public static final String TAG = "ndzl-iBeacon-Meter";

    public static ConcurrentLinkedQueue<Collection<Beacon>> clqBeacons = new ConcurrentLinkedQueue<>();

    public void replaceFragment(Fragment destFragment)
    {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.dynamic_fragment_frame_layout, destFragment);
        fragmentTransaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText("");

                    Fragment fr_ALL = new Fragment_Allbeacons();
                    replaceFragment(fr_ALL);

                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);

                    Fragment fr_ONE = new Fragment_OneBeacon();
                    replaceFragment(fr_ONE);

                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    for (Fragment fragment:getSupportFragmentManager().getFragments()) {
                        //getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.tv_message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);



        if (   checkSelfPermission(  android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(  android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 3003);
        }
        else
        {
            startBeaconLibrary();
        }

        NameViewModel.model = ViewModelProviders.of(this).get(NameViewModel.class);

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



    String toBePrinted = "x";
    void PrintOnScreen(String _s){
        toBePrinted = _s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String _old = mTextMessage.getText().toString();
                //mTextMessage.setText(toBePrinted+"\n"+_old);
                NameViewModel.model.getCurrentName().setValue(toBePrinted+"\n"+_old);
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

                    for (Object _b : beacons) {
                        String btAddr = ((Beacon)_b).getBluetoothAddress();
                        String bRssi = ""+((Beacon)_b).getRssi();
                        PrintOnScreen(btAddr+" "+bRssi+" dBm");
                        Log.i(TAG, " "+btAddr+" "+bRssi+" dBm");
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
