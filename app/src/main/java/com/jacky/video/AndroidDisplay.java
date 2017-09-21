package com.jacky.video;

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

    private final int LOCALPORT = 18000;
    private Button btnDisplay;
    private ImageView imgVDisplay;
    private ServerSocket srvSocket = null;
    private static Handler mHandler;

    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*屏幕常亮*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/*无标题*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);

		/*全屏*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		/*横屏*/
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_android_display);

        btnDisplay = (Button) findViewById(R.id.display);
        imgVDisplay = (ImageView) findViewById(R.id.imageView_Pic);

        btnDisplay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                new RecvThread().start();
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                //操作UI界面
                if (bitmap != null)
                    imgVDisplay.setImageBitmap(bitmap);
                super.handleMessage(msg);
            }

        };
    }

    /**
     * 开启监听线程
     */
    class RecvThread extends Thread {
        private byte[] data = new byte[409600];
        private byte[] byteBuffer = new byte[2048];
        private int datalength;
        private int count = 0;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // super.run();
            try {
                srvSocket = new ServerSocket(LOCALPORT);
                while (true) {
                    Socket socket = srvSocket.accept();
                    try {
                        InputStream inputStream = socket.getInputStream();
                        count = 0;
                        do {
                            datalength = inputStream.read(byteBuffer);
                            if (datalength != -1) {
                                System.arraycopy(byteBuffer, 0, data, count, datalength);
                                count += datalength;
                            }
                        } while (datalength != -1);
                        Log.v("AndroidVideo", Integer.toString(count));

                        new craetNdisplayImageThread(data, count).start();

                        inputStream.close();
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    } finally {
                        socket.close();
                    }

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    class craetNdisplayImageThread extends Thread {
        private byte[] data;
        private int count;
        //private Bitmap bitmap;

        public craetNdisplayImageThread(byte[] data, int count) {
            // TODO Auto-generated constructor stub
            this.data = data;
            this.count = count;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            //super.run();
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
