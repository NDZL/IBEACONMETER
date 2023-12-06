package com.ndzl.ibeaconmeter;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NameViewModel extends ViewModel {

    static public NameViewModel model;

    private MutableLiveData<String> currentName;
    public MutableLiveData<String> getCurrentName() {
        if (currentName == null) {
            currentName = new MutableLiveData<String>();
        }
        return currentName;
    }



    private MutableLiveData<String> maxval;
    public MutableLiveData<String> getGetmaxval() {
        if (maxval == null) {
            maxval = new MutableLiveData<String>();
        }
        return maxval;
    }

}