package com.jacky.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AndroidDisplay extends Activity {

    private final int LOCALPORT = 8888;
    private Button btnDisplay;
    private ImageView imgVDisplay;
    private ServerSocket srvSocket = null;
    private static Handler mHandler;

    private Bitmap bitmap = null;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_android_display);

        btnDisplay = findViewById(R.id.display);
        imgVDisplay = findViewById(R.id.imageView_Pic);

        btnDisplay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new RecvThread().start();
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (bitmap != null){
                    imgVDisplay.setImageBitmap(bitmap);
                }
                super.handleMessage(msg);
            }

        };
    }

    private class RecvThread extends Thread {
        private byte[] data = new byte[409600];
        private byte[] byteBuffer = new byte[2048];
        private int dataLength;
        private int count = 0;

        @Override
        public void run() {
            try {
                srvSocket = new ServerSocket(LOCALPORT);
                while (true) {
                    new DisplayImageThread(data, count).start();
                    try (Socket socket = srvSocket.accept()) {
                        InputStream inputStream = socket.getInputStream();
                        count = 0;
                        do {
                            dataLength = inputStream.read(byteBuffer);
                            if (dataLength != -1) {
                                System.arraycopy(byteBuffer, 0, data, count, dataLength);
                                count += dataLength;
                            }
                        } while (dataLength != -1);
                        Log.v("AndroidVideo", Integer.toString(count));

                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class DisplayImageThread extends Thread {
        private byte[] data;
        private int count;

        DisplayImageThread(byte[] data, int count) {
            this.data = data;
            this.count = count;
        }

        @Override
        public void run() {
            bitmap = BitmapFactory.decodeByteArray(data, 0, count);
            Message msg = new Message();
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_android_display, menu);
        return true;
    }
}
