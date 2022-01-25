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

        serialPort = new Arduino(this);
        serialPort.setBaudRate(9600);
        /* FTDI: New USB device found, idVendor=1a86, idProduct=7523, bcdDevice= 2.54 */
        /* ESP32: New USB device found, idVendor=10c4, idProduct=ea60, bcdDevice= 1.00 */
        serialPort.addVendorId(0x10c4);
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
        // new message received from serialPort
        String message = new String(bytes);
        Log.i(TAG, "> " + message);
        display(message);
    }

    @Override
    public void onArduinoOpened() {
        // you can start the communication
        String str = "Hello World!";
        serialPort.send(str.getBytes());
    }

    @Override
    public void onUsbPermissionDenied() {
        // Permission denied, display popup then
        Log.i(TAG, "Permission denied... New attempt in 3 sec");
        new Handler().postDelayed(() -> serialPort.reopen(), 3000);
    }

    public void display(final String message) {
        Log.i(TAG, "Received message: " + message);
    }
}