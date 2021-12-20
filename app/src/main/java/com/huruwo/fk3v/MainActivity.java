package com.huruwo.fk3v;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;
    private Button button;
    private Button button3;
    private TextView textView;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    textView.setText(msg.obj.toString());
                    break;
            }
        }
    };





    private static final int VPN_REQUEST_CODE = 0x0F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button = (Button) findViewById(R.id.button);
        button3 = (Button) findViewById(R.id.button3);
        textView = (TextView) findViewById(R.id.textView);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVPN();
            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVPN();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNetIp();
            }
        });


        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void startVPN(){
        Intent vpnIntent = VpnService.prepare(MainActivity.this);//弹出确认VPN框
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
    }

    private void stopVPN(){
        //关闭VPN需要与service通信 调用  stopSelf()函数
        Intent intent = new Intent("stop_kill");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void requestNetIp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                Request request = new Request
                        .Builder()
                        .get()
                        .url("https://pv.sohu.com/cityjson?ie=utf-8")
                        .addHeader("user-agent","Mozilla/5.0 (Linux; Android 6.0.1; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Mobile Safari/537.36")
                        .addHeader("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String data  = response.body().string();
                    Message m = handler.obtainMessage();
                    m.obj = data;
                    m.what = 0;
                    handler.sendMessage(m);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent1 = new Intent(MainActivity.this, FKV3DemoService.class);
            startService(intent1);
        }
    }
}