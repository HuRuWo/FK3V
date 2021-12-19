package com.huruwo.fk3v;

import static com.huruwo.fk3v.FKV3DemoService.STOP_SERVICE;
import static com.huruwo.fk3v.FKV3DemoService.VPN_INTENT_CMD;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;

    private static final int VPN_REQUEST_CODE = 0x0F;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);


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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent1 = new Intent(MainActivity.this,FKV3DemoService.class);
            startService(intent1);
        }
    }
}