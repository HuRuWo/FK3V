## Android VPNService的基本使用

### 新建类继承VPNService

```
public class FKV3DemoService extends VpnService {
}
```


###  VPNService在onStartCommand里面填写配置文件`ParcelFileDescriptor`
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

### 权限申请

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
### 配置文件注册服务

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

### 启动服务

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


### 关闭服务

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