package com.example.arduinoserial;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity implements ArduinoListener {

    private static final String TAG = "MainActivityTAG";
    private TextView serialStatusTextView;
    private TextView receivedMessageTextView;
    private EditText messageToSendEditText;
    private Arduino serialPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serialStatusTextView = findViewById(R.id.serialStatus);
        receivedMessageTextView = findViewById(R.id.receivedMessage);
        messageToSendEditText = findViewById(R.id.messageToSend);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String msg = messageToSendEditText.getText().toString();
            if (msg.length() > 0) {
                serialPort.send(msg.getBytes());
                messageToSendEditText.setText("");
            } else {
                Snackbar.make(v, "Cannot send empty message", Snackbar.LENGTH_LONG).show();
            }
        });

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
        serialStatusTextView.setText(R.string.serialListening);
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
        serialStatusTextView.setText(R.string.serialAttached);
        serialPort.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Log.i(TAG, "Arduino detached");
        serialStatusTextView.setText(R.string.serialDetached);
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        // new message received from serialPort
        String message = new String(bytes);
        display(message);
    }

    @Override
    public void onArduinoOpened() {
        // you can start the communication
        serialStatusTextView.setText(R.string.serialOpen);
        String str = "Hello World!";
        serialPort.send(str.getBytes());
    }

    @Override
    public void onUsbPermissionDenied() {
        // Permission denied, display popup then
        serialStatusTextView.setText(R.string.SerialPermission);
        Log.i(TAG, "Permission denied... New attempt in 3 sec");
        new Handler().postDelayed(() -> serialPort.reopen(), 3000);
    }

    public void display(final String message) {
        try {
            Log.i(TAG, "Received message: " + message);
            runOnUiThread(() -> receivedMessageTextView.setText(message));
        } catch (Exception e) {
            Log.i(TAG, "Failed to receive message");
            e.printStackTrace();
        }
    }
}