package com.example.bluetoothconnector;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BTEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        Log.v("RECEIVER_", " CHANGED");

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {


            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    == BluetoothAdapter.STATE_OFF) ;
            // Bluetooth is disconnected, do handling here
        }
    }
}
