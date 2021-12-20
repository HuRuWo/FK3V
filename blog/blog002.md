## Android VPNService的进阶应用 

根据上面的服务创建，我们得到了一个VPNService启动在系统中。

但是我们目前没有使用这个服务做任何具体的事情，当然我们还是从一个代理服务来看吧

在我们做任何对数据转发之前，我们先尝试来读取VPNServcie中拦截到的数据


### VPNService的数据流向

原理分析图

![img.png](https://developer.android.com/images/guide/topics/connectivity/vpn-app-arch.svg)

由此可以得知:

- 数据流入方向

VPN gateway -> internet -> 进入指定Socket通道 -> AppService -> local TUN interface -> System network -> App应用

- 数据流出方向 

App应用 -> System network-> local TUN interface -> AppService -> 进入指定Socket通道 -> internet -> VPN gateway

我们的VPNService处于中间的层，对接local TUN interface转发出来的流量以及处理进来的流量给local TUN interface


### local TUN interface

local TUN interface 意为本地端口转发，其实这也是VPNService服务的原理。通过对流量的端口转发实现本地流量的代理。

具体的端口转发原理资料非常少，我们后面可以分析源码来讲解原理。


### 解析APP数据流量

网络协议有很多种，这里以http/https(TCP)为例，来看下VPNService对TCP流量的处理。

>VpnService 和相关的 Builder 类允许应用指定网络参数，比如接口 IP 地址和路由，随后系统使用这些参数创建并配置一个虚拟网络接口。
> 应用将会接收到一个该网络接口对应的文件描述符，之后就可以通过写入或者读取该描述符进行网络隧道通信。

#### 几个重要的类和方法

ParcelFileDescriptor

文件描述符，是一种程序读写已打开文件、socket的对象。
在VPNService由DescriptorBuilder创建。

FileDescriptor

它代表了原始的Linux文件描述符。在VPNService中由ParcelFileDescriptor直接调用getFileDescriptor()方法得到。

在FileDescriptor对象的基础上我们可以对这个对象进行读写操作。


FileChannel

针对FileDescriptor文件对象，我们建立读写通道。

```java
FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
```

#### 文件数据读取

如此，我们在FileDescriptors上面插了两个管道

vpnInput  数据进入管道

vpnOutput 数据流出管道

为了避免影响测试效果，先让vpn只允许自己用

```java
     try {
            descriptorBuilder.addDisallowedApplication(getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
```