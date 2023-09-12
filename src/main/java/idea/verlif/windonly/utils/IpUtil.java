package idea.verlif.windonly.utils;

import idea.verlif.windonly.WindonlyException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {

    public static String getLocalIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            throw new WindonlyException(e);
        }
    }

}
