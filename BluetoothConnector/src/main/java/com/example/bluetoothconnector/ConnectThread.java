package com.example.bluetoothconnector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

/**
 * Connect Thread
 * <p>
 * This try's to establish a connection to a BT device. The device must already have been bound.
 * If successful this thread starts the {@link ConnectedThreadReadWriteData} which reads from the input stream.
 */
public class ConnectThread extends Thread  {

    // Debug
    private String tag;

    // BT
    private BluetoothSocket mSocket = null;
    private BluetoothDevice mDevice = null;
    private ConnectedThreadReadWriteData connectedThreadReadWriteData;

    // UI
    private Context c;

    // For the HC- 05 we have to use this UUID:
    private UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BTConnectedInterface connectedInterface;

    /**
     * Connect to device.
     *
     * @param mDevice
     * @param connectedInterface
     */
    public ConnectThread(BluetoothDevice mDevice, BTConnectedInterface connectedInterface) {

        this.mDevice = mDevice;
        mSocket = null;
        this.connectedInterface = connectedInterface;

        tag = getClass().getSimpleName();

        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(myUUID);
            connectedInterface.receiveStatusMessage("Creating socket....");
        } catch (IOException e) {
            connectedInterface.receiveStatusMessage(e.toString());
        }
        this.c = c;
    }

    /**
     *
     */
    public void run() {
        connectedThreadReadWriteData = null;
        try {
            mSocket.connect();
            connectedInterface.receiveStatusMessage("Connecting to socket...");

            // This reads incoming data from the conneted device...
            connectedThreadReadWriteData = new ConnectedThreadReadWriteData(mSocket, connectedInterface);
            connectedThreadReadWriteData.start();

        } catch (IOException e) {
            connectedInterface.receiveErrorMessage("Data transfer thread could not be started:" + e.toString());
            if (connectedThreadReadWriteData != null) connectedThreadReadWriteData.cancel();
        }
        return;
    }

    /**
     *
     */
    public void cancel() {
        connectedInterface.receiveStatusMessage("Canceling connection....");
    }
}