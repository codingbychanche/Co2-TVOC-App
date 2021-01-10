package com.example.airqualityapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.airqualityapp.ui.home.HomeViewModel;
import com.example.bluetoothconnector.BTConnectedInterface;
import com.example.bluetoothconnector.ConnectThread;
import com.example.bluetoothconnector.ConnectedThreadReadWriteData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

// Bluetooth
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Set;

/**
 * Main
 */
public class MainActivity extends AppCompatActivity implements FragmentSelectDevice.getDataFromFragment, BTConnectedInterface {

    // View Model
    private HomeViewModel homeViewModel;

    // UI
    private Handler handler;
    private ProgressBar workingProgerssBar;
    private ImageButton reconnectView;
    private TextView btStatusView, btNameAndAddressView;

    // Bluetooth
    private BluetoothAdapter blueToothAdapter;
    private ConnectedThreadReadWriteData connectedThreadReadWriteData;
    private Set<BluetoothDevice> btBondedDevices;
    private BluetoothDevice bluetoothDeviceCurentlyConnectedTo;
    private ConnectThread connectThread;
    private String adressOfCurrentDevice;

    // Shared
    SharedPreferences sharedPreferences;

    /**
     * On Create
     *
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // View Models
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // UI
        handler = new Handler();
        workingProgerssBar = findViewById(R.id.working);
        reconnectView = findViewById(R.id.reconnect_button);
        btStatusView = findViewById(R.id.bt_status);
        btNameAndAddressView = findViewById(R.id.bt_device_name_and_address);

        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bm.getAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothIntent, 1);
        }

        connectToBluetoothDeviceLogic();

        // If show, this button lets the user reconnect manually to the bluetooth device...
        reconnectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToBluetoothDeviceLogic();
            }
        });
    }

    /**
     * Callback for {@link FragmentSelectDevice.getDataFromFragment}
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void getDialogInput(String buttonPressed, String deviceName, BluetoothDevice bluetoothDeviceToConnectTo) {

        if (buttonPressed.equals("CANCEL")) {

        } else {
            //connectionStatus.append(getResources().getString(R.string.name_of_device_selected) + deviceName);
            connectToDevice(bluetoothDeviceToConnectTo);
            adressOfCurrentDevice = bluetoothDeviceToConnectTo.getAddress();
        }
    }

    /**
     * Callback when a connection was established.
     * <p>
     * Receives an instance of the {@link ConnectedThreadReadWriteData} over which
     * data can be send to the connected device.
     */
    @Override
    public void success(ConnectedThreadReadWriteData connectedThreadReadWriteData) {
        this.connectedThreadReadWriteData = connectedThreadReadWriteData;

        handler.post(new Runnable() {
            @Override
            public void run() {
                workingProgerssBar.setVisibility(View.GONE);
                reconnectView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Data received via the bluetooth connection is received here.
     *
     * @param receivedData
     */
    public void receiveData(String receivedData) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                //btStatusView.setText(receivedData);
            }
        });
    }

    /**
     * This callback serves as a receiver for generic status messages
     * from the connection.
     *
     * @param statusMessage
     */
    public void receiveStatusMessage(String statusMessage) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                btStatusView.setText(statusMessage);
            }
        });
    }

    /**
     * Callback when an error occurred..
     *
     * @param error
     */
    public void receiveErrorMessage(String error) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                btStatusView.setText(error);
                workingProgerssBar.setVisibility(View.GONE);
                reconnectView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Shows a list of bonded devices to choose from and
     * to connect to.
     */
    private void showDeviceList() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentSelectDevice fragmentSelectDevice = FragmentSelectDevice.newInstance("Titel");
        fragmentSelectDevice.show(fm, "fragment_select_device");
    }

    /**
     * When bluetooth is turned on or it was turned on, this checks if a
     * device address was stored in shared prefs and if so, gets the
     * associated device and try's to connect to it. If not, show a
     * list of bonded dev's allowing the user to select one.....
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectToBluetoothDeviceLogic() {
        // Device already selected and saved after leaving previous session?
        // If so, try to reconnect... If not, show list of devices
        // to connect to.
        adressOfCurrentDevice = currentStateRestoreFromSharedPref();
        if (adressOfCurrentDevice.equals("NO_DEVICE")) {
            showDeviceList();
        } else {
            bluetoothDeviceCurentlyConnectedTo = getBlueToothDeviceByAdress(adressOfCurrentDevice);
            if (bluetoothDeviceCurentlyConnectedTo != null)
                connectToDevice(bluetoothDeviceCurentlyConnectedTo);
        }
    }

    /**
     * Connects to device.
     *
     * @param bluetoothDevice
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectToDevice(BluetoothDevice bluetoothDevice) {

        workingProgerssBar.setVisibility(View.VISIBLE);
        reconnectView.setVisibility(View.GONE);

        if (bluetoothDevice != null) {
            String name = bluetoothDevice.getName();
            adressOfCurrentDevice = bluetoothDevice.getAddress();

            BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            blueToothAdapter = bm.getAdapter();

            connectThread = new ConnectThread(bluetoothDevice, this);
            connectThread.start();

            currentStateSaveToSharedPref(adressOfCurrentDevice);

            btNameAndAddressView.setText("Current device:" + name + " // " + adressOfCurrentDevice);
        } else {
            btNameAndAddressView.setText("-");
        }
    }

    /**
     * Get Bluetooth device by it's address
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothDevice getBlueToothDeviceByAdress(String adressOfBluetoothDevice) {
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BluetoothAdapter bluetoothAdapter = bm.getAdapter();

        btBondedDevices = bluetoothAdapter.getBondedDevices();

        if (btBondedDevices.size() > 0) {
            //connectionStatus.append(getResources().getString(R.string.found_bomded_devices_looking_for_hc05) + ":" + adressOfBluetoothDevice);

            for (BluetoothDevice dev : btBondedDevices) {
                if (dev.getAddress().equals(adressOfBluetoothDevice)) {
                    bluetoothDeviceCurentlyConnectedTo = dev;
                }
            }
        } else {
            //connectionStatus.append(getResources().getString(R.string.error_no_dev_with_this_address_found));
            showDeviceList();
        }
        return bluetoothDeviceCurentlyConnectedTo;
    }

    /*
     * Save current state to sharedPreferences.
     */
    private void currentStateSaveToSharedPref(String adressOfCurrentDevice) {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // ToDo
        editor.putString("adressOfCurrentDevice", adressOfCurrentDevice);
        editor.commit();
    }

    /*
     * Restore from shared pref's..
     */
    private String currentStateRestoreFromSharedPref() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        String adressOfCurrentDevice = sharedPreferences.getString("adressOfCurrentDevice", "NO_DEVICE");
        return adressOfCurrentDevice;
    }

    /*
     * When app gets destroyed by the system...
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connectedThreadReadWriteData != null)
            connectedThreadReadWriteData.cancel();
    }

    /*
     * When back button was pressed....
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (connectedThreadReadWriteData != null)
            connectedThreadReadWriteData.cancel();
    }
}