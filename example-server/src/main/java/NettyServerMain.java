import com.rpc.annotation.RpcScan;
import com.rpc.config.RpcServiceConfig;
import com.rpc.remoting.transport.netty.server.NettyRpcServer;
import com.rpc.serviceimpl.HelloServiceImpl2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"com.rpc"})
public class NettyServerMain {
    public static void main(String[] args) {
        // 通过注解注册服务
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // 手动注册服务
        HelloServiceImpl2 helloService2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(helloService2).build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
