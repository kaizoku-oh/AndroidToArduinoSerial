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

    private static final String TAG = "MainActivityTAG";
    ChatView chatView;
    private Arduino serialPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatView = findViewById(R.id.chat_view);

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

        serialPort = new Arduino(this);
        serialPort.setBaudRate(19200);
        /* OPEN SMART FTDI: New USB device found, idVendor=1a86, idProduct=7523, bcdDevice= 2.54 */
        /* ESP32-WROOM-32: New USB device found, idVendor=10c4, idProduct=ea60, bcdDevice= 1.00 */
        /* Arduino Nano: New USB device found, idVendor=0403, idProduct=0000, bcdDevice= 6.00 */
        serialPort.addVendorId(0x1a86);
        Log.i(TAG, "Please plug an Arduino via OTG.");
        Log.i(TAG, "On some devices you will have to enable OTG Storage in the phone's settings");
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
        String message = new String(bytes);
        display(message);
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
        new Handler().postDelayed(() -> serialPort.reopen(), 3000);
    }

    public void display(final String message) {
        try {
            Log.i(TAG, "Received message: " + message);
            long tsLong = System.currentTimeMillis()/ 1000;
            chatView.addMessage(new ChatMessage(message, tsLong, RECEIVED));
        } catch (Exception e) {
            Log.i(TAG, "Failed to receive message");
            e.printStackTrace();
        }
    }
}