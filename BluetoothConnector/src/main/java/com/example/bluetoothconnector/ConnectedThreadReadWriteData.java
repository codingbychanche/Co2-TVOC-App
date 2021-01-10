package com.example.bluetoothconnector;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.os.Handler;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles sending and receiving to/ from connected device.
 * Invoked by {@link ConnectThread}
 */
public class ConnectedThreadReadWriteData extends Thread {

    // Debug
    private String tag = getClass().getSimpleName();

    // BT
    private final BluetoothSocket mSocket;
    private final InputStream mIs;
    private final OutputStream mOs;
    private float dataChunksActuallySend = 1;
    private float dataChunksArrived = 1;

    // UI
    private BTConnectedInterface connectedInterface;
    private String receivedData;

    /**
     * Connected Thread
     * <p>
     * When a bluetooth connection has been established, this thread
     * receives a data- stream from the device connected and invokes the
     * callback instance {@link BTConnectedInterface}
     *
     * The connected device is represented by it's socket.
     *
     * @param socket
     * @param connectedInterface
     */
    public ConnectedThreadReadWriteData(BluetoothSocket socket, BTConnectedInterface connectedInterface) {
        mSocket = socket;
        this.connectedInterface = connectedInterface;

        InputStream tmpIs = null;
        OutputStream tmpOs = null;

        try {
            tmpIs = mSocket.getInputStream();
            tmpOs = mSocket.getOutputStream();
            connectedInterface.receiveStatusMessage("Trying to get Socket....");
        } catch (IOException e) {
            connectedInterface.receiveStatusMessage(e.toString());
        }
        mOs = tmpOs;
        mIs = tmpIs;
    }

    /*
     * Read incoming
     */
    public void run() {
        // Read Data
        byte[] inBuffer = new byte[1024];

        connectedInterface.success(this);
        connectedInterface.receiveStatusMessage("Socket obtained....Listening for incoming data...");

        int length;

        try {
            while (true) {
                int readBytes = 0;
                length = mIs.read(inBuffer);
                while (readBytes != length) {
                    receivedData = new String(inBuffer, 0, length);
                    readBytes++;
                }

                // Wait before next data chunk arrives.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
              connectedInterface.receiveData(receivedData);
            }

        } catch (IOException e) {
            connectedInterface.receiveErrorMessage(e.toString());
        }
    }

    /**
     * Send to device.
     *
     * @param dataToSend
     */
    public void send(String dataToSend) {
        try {
            connectedInterface.receiveStatusMessage("Sending:"+dataToSend);
            mOs.write(dataToSend.getBytes());
        } catch (IOException e) {
            connectedInterface.receiveErrorMessage(e.toString());
        }
    }

    /**
     * Closes this connection
     *
     */
    public void cancel() {

        connectedInterface.receiveStatusMessage("Closing connection..");

        try {
            if (mIs != null)
                mIs.close();
            if (mOs != null)
                mOs.close();
            if (mSocket != null)
                mSocket.close();

        } catch (IOException e) {
            connectedInterface.receiveErrorMessage(e.toString());
        } finally {
            connectedInterface.receiveStatusMessage("Input stream and socket closed");
            this.interrupt();

        }
    }
}
