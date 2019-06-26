package com.ndzl.ibeaconmeter;


import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_OneBeacon extends Fragment {

    EditText etBeacon;
    TextView tvRSSI;
    private BroadcastReceiver receiver;

    Button btChase;
    public Fragment_OneBeacon() {
        // Required empty public constructor
    }

    String beacon_to_chase = "";

    boolean goOn=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment__one_beacon, container, false);

        final Observer<String> nameObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                if(goOn){
                    if(newName.replaceAll(":", "").contains(beacon_to_chase))
                        tvRSSI.setText(newName.substring(18));
                }
            }
        };
        NameViewModel.model.getCurrentName().observeForever( nameObserver );

        etBeacon = view.findViewById(R.id.etBeaconBarcode);
        tvRSSI = view.findViewById(R.id.tv_chased_rssi);
        btChase =view.findViewById(R.id.btChase);
        btChase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beacon_to_chase = etBeacon.getText().toString();
                if(beacon_to_chase.length()<12)
                    etBeacon.setText("ERR");
                else{

                        goOn=true;
                }
            }
        });

        //GESTIONE LETTURA BARCODE DA INTENT DATAWEDGE
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if(action.equals("com.ndzl.readmybarcode")){
                    String barcode_value = intent.getStringExtra("com.symbol.datawedge.data_string");
                    etBeacon.setText(barcode_value);
                }

            }
        };
        registerReceivers();

        return view;
    }

    void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ndzl.readmybarcode");
        filter.addCategory("android.intent.category.DEFAULT");
        Intent regres = getActivity().registerReceiver(receiver, filter);
    }

}
