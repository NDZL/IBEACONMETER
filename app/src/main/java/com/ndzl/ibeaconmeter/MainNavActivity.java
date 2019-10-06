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
import android.view.KeyEvent;
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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainNavActivity extends AppCompatActivity implements BeaconConsumer {
    private TextView mTextMessage;
    private BeaconManager beaconManager;
    public static final String TAG = "ndzl-iBeacon-Meter";
    Timer tim;

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
                    Fragment fr_ALL = new Fragment_Allbeacons();
                    replaceFragment(fr_ALL);
                    return true;
                case R.id.navigation_dashboard:
                    Fragment fr_ONE = new Fragment_OneBeacon();
                    replaceFragment(fr_ONE);
                    return true;
                case R.id.navigation_notifications:
                    Fragment fr_INFO = new Fragment_BeaconInfo();
                    replaceFragment(fr_INFO);
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
        Fragment fr_ALL = new Fragment_Allbeacons();
        replaceFragment(fr_ALL);
        mTextMessage.setText("");


        /*
        tim = new Timer("NIK", false);
        tim.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }

                });
            }
        }, 100, 1000);
        */

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }

    static Map<String,String> mapInfoBeaconManager = new HashMap<>();
    void startBeaconLibrary(){
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.setForegroundScanPeriod(300); //speed! default is 1100ms //n.DZL

        mapInfoBeaconManager.put("Availability", ""+beaconManager.checkAvailability());
        mapInfoBeaconManager.put("BackgroundMode", ""+beaconManager.getBackgroundMode());
        mapInfoBeaconManager.put("BackgroundScanPeriod", ""+beaconManager.getBackgroundScanPeriod());
        mapInfoBeaconManager.put("BackgroundBetweenScanPeriod", ""+beaconManager.getBackgroundBetweenScanPeriod());
        mapInfoBeaconManager.put("RangedRegions", ""+beaconManager.getRangedRegions().size());
        mapInfoBeaconManager.put("RangingNotifiers", ""+beaconManager.getRangingNotifiers().size());
        mapInfoBeaconManager.put("ForegroundScanPeriod", ""+beaconManager.getForegroundScanPeriod());
        mapInfoBeaconManager.put("ForegroundBetweenScanPeriod", ""+beaconManager.getForegroundBetweenScanPeriod());
        mapInfoBeaconManager.put("ForegroundServiceNotificationId", ""+beaconManager.getForegroundServiceNotificationId());

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.bind(this);
    }


    int resetcounter=3;
    int resetrows=10;

    String toBePrinted = "x";
    int maxRssi=-1000;
    String maxRSSI_mac="";
    void PrintOnScreen(String _s){

        toBePrinted = _s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NameViewModel.model.getCurrentName().setValue(toBePrinted+"\n");

                NameViewModel.model.getGetmaxval().setValue(maxRSSI_mac+" "+maxRssi);
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
                    if(resetcounter--==0){
                        maxRssi=-1000;
                        maxRSSI_mac="";
                        resetcounter=3;
                    }

                    for (Object _b : beacons) {
                        String btAddr = ((Beacon)_b).getBluetoothAddress();
                        String bRssi = ""+((Beacon)_b).getRssi();
                        String bBattery = ""+ ((Beacon)_b).getId3();
                       // String bName = ""+ ((Beacon)_b).getBluetoothName();
                       // String bTxPow = ""+((Beacon)_b).getTxPower();  //The calibrated measured Tx power of the Beacon in RSSI This value is baked into an Beacon when it is manufactured, and it is transmitted with each packet to aid in the mDistance estimate

                        if(((Beacon)_b).getRssi()>maxRssi){
                            maxRssi = ((Beacon)_b).getRssi();
                            maxRSSI_mac = btAddr.substring(12);
                        }
                        Calendar ct = Calendar.getInstance();


                        PrintOnScreen(btAddr+" "+bRssi+" dBm @"+ct.get(Calendar.MINUTE)+"'"+ct.get(Calendar.SECOND)+"\" btry:"+bBattery+"%");
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
