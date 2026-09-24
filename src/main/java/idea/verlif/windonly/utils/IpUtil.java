package idea.verlif.windonly.utils;

import java.net.InetAddress;

public class IpUtil {

    /**
     * 本机地址。{@code InetAddress.getLocalHost()} 可能触发 DNS 查询，属于阻塞操作，
     * 而每添加一个远程数据项都会调用一次，因此缓存结果。
     */
    private static volatile String localIp;

    public static String getLocalIp() {
        String ip = localIp;
        if (ip != null) {
            return ip;
        }
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();
        } catch (Throwable e) {
            ip = "127.0.0.1";
        }
        localIp = ip;
        return ip;
    }

}
