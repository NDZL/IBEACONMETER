package com.ndzl.ibeaconmeter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fragment_Allbeacons extends Fragment {

    TextView textView;
    TextView maxRssi;

    public Fragment_Allbeacons() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment__allbeacons, container, false);

        textView = view.findViewById(R.id.tv_list_all);

        //MV
        final Observer<String> nameObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                String _cur_txt = textView.getText().toString();
                if(_cur_txt.split("\n").length>50)
                    _cur_txt="";
                textView.setText(newName +_cur_txt);
            }
        };
        NameViewModel.model.getCurrentName().observeForever( nameObserver );



        maxRssi = view.findViewById(R.id.tv_bigRSSI);
        final Observer<String> maxvalObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                maxRssi.setText(newName);
            }
        };
        NameViewModel.model.getGetmaxval().observeForever( maxvalObserver );

        return view;
    }
}