package com.example.airqualityapp.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bluetoothconnector.BTConnectedInterface;
import com.example.bluetoothconnector.ConnectedThreadReadWriteData;

public class HomeViewModel extends ViewModel {
    boolean connectedToDevice;
    static int co2In_PPM;
    static int airQuality;

    public int getCo2In_PPM() {
        return co2In_PPM;
    }

    public void setCo2In_PPM(int co2In_PPM) {
        this.co2In_PPM = co2In_PPM;
    }

    public int getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(int airQuality) {
        this.airQuality = airQuality;
    }
}
