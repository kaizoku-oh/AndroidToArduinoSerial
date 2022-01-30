package com.example.arduinoserial;

import static co.intentservice.chatui.models.ChatMessage.Type.RECEIVED;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import me.aflak.arduino.Arduino;
import me.aflak.arduino.ArduinoListener;

public class MainActivity extends AppCompatActivity implements ArduinoListener {

    /*
     * Vendor IDs:
     *   OPEN SMART FTDI: 0x1A86
     *   ESP32-WROOM-32:  0x10C4
     *   Arduino Nano:    0x0403
     */
    private static final int VENDOR_ID = 0x1A86;
    private static final int BAUDRATE = 19600;
    private static final int SERIAL_REOPEN_TIMEOUT_MS = 3000;
    private static final String TAG = "MainActivityTAG";

    private Arduino serialPort;
    private ChatView chatView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatView = findViewById(R.id.chat_view);

        serialPort = new Arduino(this);
        serialPort.setBaudRate(BAUDRATE);
        serialPort.addVendorId(VENDOR_ID);
        Log.i(TAG, "Please plug an Arduino via OTG.");
        Log.i(TAG, "On some devices you will have to enable OTG Storage in the phone's settings");

        chatView.setOnSentMessageListener(chatMessage -> {
            // perform actual message sending
            Log.i(TAG, "onCreate: Sending message...");
            serialPort.send(chatMessage.getMessage().getBytes());
            long tsLong = System.currentTimeMillis();
            chatView.addMessage(new ChatMessage(chatMessage.getMessage(), tsLong, RECEIVED));
            return true;
        });

        chatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {
                // will be called when the user starts typing
                Log.i(TAG, "userStartedTyping: Typing...");
            }

            @Override
            public void userStoppedTyping() {
                // will be called when the user stops typing
                Log.i(TAG, "userStoppedTyping: Stopped typing");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        serialPort.setArduinoListener(this);
        Log.i(TAG, "Listening");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPort.unsetArduinoListener();
        serialPort.close();
        Log.i(TAG, "Serial closed");
    }

    @Override
    public void onArduinoAttached(UsbDevice device) {
        Log.i(TAG, "Serial attached!");
        serialPort.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Log.i(TAG, "Serial detached");
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        // new message received from serialPort
        String message;
        long tsLong;

        message = new String(bytes);
        tsLong = System.currentTimeMillis();
        chatView.addMessage(new ChatMessage(message, tsLong, RECEIVED));
    }

    @Override
    public void onArduinoOpened() {
        // you can start the communication
        Log.i(TAG, "Serial opened");
    }

    @Override
    public void onUsbPermissionDenied() {
        // Permission denied, display popup then
        Log.i(TAG, "Permission denied... New attempt in 3 sec");
        new Handler().postDelayed(() -> serialPort.reopen(), SERIAL_REOPEN_TIMEOUT_MS);
    }
}