package com.ndzl.ibeaconmeter;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Map;

public class Fragment_BeaconInfo extends Fragment {

    TextView textView;
    TextView maxRssi;

    public Fragment_BeaconInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment__info, container, false);

        textView = view.findViewById(R.id.tv_list_info);

        StringBuilder _sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = MainNavActivity.mapInfoBeaconManager.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String,String> pairs = (Map.Entry<String,String>)iterator.next();
            String value =  pairs.getValue();
            String key = pairs.getKey();
           _sb.append(key+": "+value+"\n");
        }

        textView.setText(_sb.toString());
        return view;
    }
}