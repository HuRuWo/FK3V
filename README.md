## FK3V

一个实验性的VPNService相关教学项目，旨在帮助大家理解VPNService的使用和开发流程。

>可能是目前(2021年12月)最好的VPNService中文资料。如果你正在寻找VPNService相关资料，那么恭喜你找到了最详细的教程。
                         
                                                                                                          

### 项目进度列表

- 1.VPNService相关介绍

VPNService是android系统自带的VPN服务。基于VPNService你可以拦截并且处理APP的流量。

基于这个服务可以做到代理VPN服务

我们常见的SSR、V2ray等代理软件在安卓的实现都基于VPNService

当然基于这个服务我们还可以做一些其他的事情，除了做VPN代理

比如:

1.网络流量拦截分析

2.网络穿透服务

![img.png](https://developer.android.com/images/guide/topics/connectivity/vpn-app-arch.svg)
  
- 2.VPNService基本使用

- 新建类继承VPNService

```
public class FKV3DemoService extends VpnService {
}
```


- VPNService在onStartCommand里面填写配置文件`ParcelFileDescriptor`
```java
Builder descriptorBuilder = new Builder();
   /**
         * builder.setMtu(int mut)//设置读写操作时最大缓存
         *        .setSession(String session)//设置该次服务名称，服务启动后可在手机设置界面查看
         *        .addAddress(String address, int port)//设置虚拟主机地址和端口
         *        .addRoute(String address, int port)//设置允许通过的路由
         *        .addDnsServer(String address)//添加域名服务器
         *        .addAllowedApplication(String name)//添加允许访问连接的程序
         *        .setConfigureIntent(PendingIntent intent);//设置配置启动项
         */
descriptor = descriptorBuilder.establish();        
```


- 权限申请

```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERACT_ACROSS_USERS" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />

```
- 配置文件注册服务

```xml
<service
            android:name=".FKV3DemoService"
            android:permission="android.permission.BIND_VPN_SERVICE">
            <intent-filter>
                <action android:name="android.net.VpnService" />
            </intent-filter>
            <meta-data
                android:name="android.net.VpnService.SUPPORTS_ALWAYS_ON"
                android:value="true" />
</service>
```

- 启动服务

VPN启动服务有点特殊，需要一个确认服务的界面。用户点击了确认才能真正拉起来服务

```java
    private void startVPN(){
        Intent vpnIntent = VpnService.prepare(MainActivity.this);//弹出确认VPN框
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent1 = new Intent(MainActivity.this,FKV3DemoService.class);
            startService(intent1);
        }
    }
```


- 关闭服务

关闭服务 需要将descriptor和service都关闭了

```java
    public void stopService() {
        try {
            if(descriptor!=null) {
                descriptor.close();
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

为了和sevice通信，我们在service里面注册一个本地接收器方便消息传递

```
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
```

我们在主界面调用本地接收器传输消息即可

```java
   private void stopVPN(){
        //关闭VPN需要与service通信 调用  stopSelf()函数
        Intent intent = new Intent("stop_kill");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

```

- 3.VPNService的高阶应用(实现一个代理)

根据上面的服务创建，我们得到了一个VPNService启动在系统中。

但是我们目前没有使用这个服务做任何具体的事情，当然我们还是从一个代理服务来看吧

在我们做任何对数据转发之前，我们先尝试来读取VPNServcie中拦截到的数据







- 4.VPNService的源码解析




### 项目事宜人群

- 拥有基本java语言基础
- 基本的Android开发基础


### 参考资料

唯一参考资料 也是官方资料

https://developer.android.google.cn/reference/android/net/VpnService

https://developer.android.google.cn/guide/topics/connectivity/vpn

https://android.googlesource.com/platform/development/+/master/samples/ToyVpn 最好的示例

https://github.com/mightofcode/android-vpnservice-example 一个有意思的demo

[comment]: <> (Android VPNService简述    https://www.jianshu.com/p/d2e3ccd6bcb3)

[comment]: <> (有赞团队关于VPNSevice的介绍 https://tech.youzan.com/app-gateway-one-switch/ )

[comment]: <> (还有一些比如ssr_adnroid、clash_android、V2ray_android等项目的分析)


