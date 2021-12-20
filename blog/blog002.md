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

