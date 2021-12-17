package com.volca.volcasdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

class Reporter {
    private static final String TAG = "Reporter";

    private static final String HOST_IP = "82.157.114.172";
    private static final int HOST_PORT = 9999;

    private static String prefix;

    static void init(Context context) {
        String packageName = context.getPackageName();
        String appName = String.valueOf(context.getPackageManager().getApplicationLabel(context.getApplicationInfo()));
        prefix = "[" + packageName + ": " + appName + "] ";
    }

    static void report(String content) {
        if (!content.endsWith("\n")) {
            content += "\n";
        }
        final String data = prefix + content;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket = null;
                try {
                    InetAddress address = InetAddress.getByName(HOST_IP);
                    byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, HOST_PORT);
                    socket = new DatagramSocket();
                    socket.send(packet);
                    Log.d(TAG, "report: " + data);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        }, "ReporterThread");
        thread.start();
    }
}
