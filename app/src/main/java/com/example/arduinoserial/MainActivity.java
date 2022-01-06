package com.example.arduinoserial;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity implements ArduinoListener {

    private static final String TAG = "MainActivityTAG";
    private Arduino serialPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serialPort = new Arduino(this, 9600);
        serialPort.setBaudRate(9600);
        serialPort.addVendorId(1234);
        Log.i(TAG, "Please plug an Arduino via OTG.");
        Log.i(TAG, "On some devices you will have to enable OTG Storage in the phone's settings");
    }


    @Override
    protected void onStart() {
        super.onStart();
        serialPort.setArduinoListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPort.unsetArduinoListener();
        serialPort.close();
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        Log.i(TAG, "Arduino attached!");
        serialPort.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Log.i(TAG, "Arduino detached");
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        Log.i(TAG, "> "+new String(bytes));
    }

    @Override
    public void onArduinoOpened() {
        String str = "Hello World!";
        serialPort.send(str.getBytes());
    }

    @Override
    public void onUsbPermissionDenied() {
        Log.i(TAG, "Permission denied... New attempt in 3 sec");
        new Handler().postDelayed(() -> serialPort.reopen(), 3000);
    }
}