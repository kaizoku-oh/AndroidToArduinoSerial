package com.example.arduinoserial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity implements ArduinoListener {

    private static final String TAG = "MainActivityTAG";
    CoordinatorLayout coordinatorLayout;
    private Arduino serialPort;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        serialPort = new Arduino(this);
        serialPort.setBaudRate(9600);
        /* FTDI: New USB device found, idVendor=1a86, idProduct=7523, bcdDevice= 2.54 */
        /* ESP32: New USB device found, idVendor=10c4, idProduct=ea60, bcdDevice= 1.00 */
        serialPort.addVendorId(0x10c4);
        Log.i(TAG, "Please plug an Arduino via OTG.");
        Log.i(TAG, "On some devices you will have to enable OTG Storage in the phone's settings");
        Snackbar.make(coordinatorLayout, "Please plug an Arduino via OTG", Snackbar.LENGTH_LONG).show();
        Snackbar.make(coordinatorLayout,
                "In some devices you will have to enable OTG Storage in the phone's settings",
                Snackbar.LENGTH_LONG).show();

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
        Snackbar.make(coordinatorLayout, "Arduino attached!", Snackbar.LENGTH_LONG).show();
        serialPort.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Log.i(TAG, "Arduino detached");
        Snackbar.make(coordinatorLayout, "Arduino detached!", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        // new message received from serialPort
        String message = new String(bytes);
        Log.i(TAG, "> " + message);
        Snackbar.make(coordinatorLayout, "Received new message: " + message, Snackbar.LENGTH_LONG).show();
        display(message);
    }

    @Override
    public void onArduinoOpened() {
        // you can start the communication
        String str = "Hello World!";
        Snackbar.make(coordinatorLayout, "Connection is open --> sending data", Snackbar.LENGTH_LONG).show();
        serialPort.send(str.getBytes());
    }

    @Override
    public void onUsbPermissionDenied() {
        // Permission denied, display popup then
        Log.i(TAG, "Permission denied... New attempt in 3 sec");
        Snackbar.make(coordinatorLayout, "Permission denied... New attempt in 3 sec", Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(() -> serialPort.reopen(), 3000);
    }

    public void display(final String message){
        runOnUiThread(() -> textView.setText(message));
    }
}