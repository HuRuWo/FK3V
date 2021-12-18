package com.huruwo.fk3v;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

public class FKV3DemoService extends VpnService {

    private ParcelFileDescriptor descriptor;//vpn服务配置



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //如果有代理相关参数 从Inten里面传入取得 比如端口号这些

        //String arg1 = intent.getStringExtra("arg1");
        //String arg2 = intent.getStringExtra("arg2");


       //

        initDescriptor();

        if(descriptor==null) {
            Toast.makeText(this,"配置失败",Toast.LENGTH_LONG).show();
            return START_STICKY;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // TODO
            //高版本的系统里面设置 前台服务提高保活
            //foregroundService
        }



        return START_STICKY; //START_STICKY 服务被杀死重新调用onStartCommand
    }


    private void initDescriptor(){
        Builder descriptorBuilder = new Builder();

        PackageManager packageManager = getPackageManager();

        String[] appPackages = {
                "com.example.xxxx"
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
        descriptorBuilder.addAddress("26.26.26.1", 32);

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
//                Logger.d("添加允许的VPN应用出错，未找到");
//                e.printStackTrace();
//            }
//        }

        descriptor = descriptorBuilder.establish();
    }

}
