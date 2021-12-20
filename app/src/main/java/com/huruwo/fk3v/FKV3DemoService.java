package com.huruwo.fk3v;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FKV3DemoService extends VpnService {

    private ParcelFileDescriptor vpnParcelDescriptor;//vpn服务配置
    private FileDescriptor vpnFileDescriptor;

    public static String VPN_INTENT_CMD = "vpn_tag";
    public static String STOP_SERVICE = "stop_service";

    private BroadcastReceiver stopBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("stop_kill".equals(intent.getAction())) {
                Toast.makeText(getApplicationContext(),"关闭VPN服务",Toast.LENGTH_LONG).show();
                stopService();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        //创建广播接收者
        LocalBroadcastManager lbm =
                LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(stopBr, new IntentFilter("stop_kill"));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //如果有代理相关参数 从Inten里面传入取得 比如端口号这些 结束命令
        String arg1 = intent.getStringExtra("VPN_INTENT_CMD");
        if (arg1!=null) {
            Toast.makeText(this,"参数传递"+arg1,Toast.LENGTH_LONG).show();
        }

        initDescriptor();


        vpnFileDescriptor = vpnParcelDescriptor.getFileDescriptor();



        //captureNetData();



        if(vpnParcelDescriptor==null) {
            Toast.makeText(this,"配置失败",Toast.LENGTH_LONG).show();
            return START_STICKY;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // TODO
            //高版本的系统里面设置 前台服务提高保活
            //foregroundService
        }

        vpnParcelDescriptor.getFd();



        return START_STICKY; //START_STICKY 服务被杀死重新调用onStartCommand
    }

    private void initDescriptor(){
        Builder descriptorBuilder = new Builder();

        PackageManager packageManager = getPackageManager();

        String[] appPackages = {
                "com.huruwo.fk3v"
        };  //允许代理的APP列表

      //关于这里参数设置解析 我们还是要从官方文档来看 https://developer.android.google.cn/guide/topics/connectivity/vpn

        /**
         * builder.setMtu(int mut)//设置读写操作时最大缓存
         *        .setSession(String session)//设置该次服务名称，服务启动后可在手机设置界面查看
         *        .addAddress(String address, int port)//设置虚拟主机地址和端口
         *        .addRoute(String address, int port)//设置允许通过的路由
         *        .addDnsServer(String address)//添加域名服务器
         *        .addAllowedApplication(String name)//添加允许访问连接的程序
         *        .setConfigureIntent(PendingIntent intent);//设置配置启动项
         */

        descriptorBuilder.setSession(getString(R.string.app_name));

        descriptorBuilder.setMtu(1500);

        //添加至少一个 IPv4 或 IPv6 地址以及系统指定为本地 TUN 接口地址的子网掩码
        descriptorBuilder.addAddress("10.0.0.2", 32);

        //0.0.0.0 0 允许所有流量通过 两种 ipv4 ipv6

        descriptorBuilder.addRoute("0.0.0.0", 0);

        try {
            descriptorBuilder.addDisallowedApplication(getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        descriptorBuilder.addDnsServer("8.8.8.8");

        descriptorBuilder.addRoute("8.8.8.8", 32);

//        for (String appPackage: appPackages) {
//            try {
//                packageManager.getPackageInfo(appPackage, 0);
//                descriptorBuilder.addAllowedApplication(appPackage);
//            } catch (PackageManager.NameNotFoundException e) {
//                // The app isn't installed.
//                LogXUtils.e("添加允许的VPN应用出错，未找到");
//                e.printStackTrace();
//            }
//        }

        // Just bypass myself
        try {

            //什么都不加 没网络


//            descriptorBuilder.addDisallowedApplication("mark.via");//有网络


//            descriptorBuilder.addDisallowedApplication(getPackageName());
//            descriptorBuilder.addDisallowedApplication("mark.via");//有网络

//             descriptorBuilder.addAllowedApplication(getPackageName());
//             descriptorBuilder.addDisallowedApplication("mark.via");//没有网络

//             descriptorBuilder.addDisallowedApplication(getPackageName());
//             descriptorBuilder.addAllowedApplication("mark.via");//没有网络


//            descriptorBuilder.addAllowedApplication(getPackageName());
//            descriptorBuilder.addAllowedApplication("mark.via");//没有网络


        } catch (Exception e) {
            e.printStackTrace();
        }

        vpnParcelDescriptor = descriptorBuilder.establish();
    }

    private void captureNetData(){


        new Thread(new Runnable() {
            @Override
            public void run() {
                //输入读取

                FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                while (!Thread.interrupted()) {
                    int readBytes = 0;
                    try {
                        readBytes = vpnInput.read(byteBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    LogXUtils.e("读取字节:"+readBytes+"");
                }
                //输出读取
                FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
            }
        }).start();


    }


    @Override
    public boolean stopService(Intent name) {
        LogXUtils.e("stopService");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        LogXUtils.e("onDestroy");
        super.onDestroy();
    }

    public void stopService() {
        try {
            if(vpnParcelDescriptor!=null) {
                vpnParcelDescriptor.close();
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
