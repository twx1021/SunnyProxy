package hev.sockstun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/* loaded from: classes.dex */
public class RouterIPRange {
    public static void IPV4(Context context) {
        @SuppressLint("WrongConstant") WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String routerIpAddress = Formatter.formatIpAddress(dhcpInfo.gateway);
        String subnetMask = Formatter.formatIpAddress(dhcpInfo.netmask);
        String routerIPRange = calculateIPRange(routerIpAddress, subnetMask);
        int ipAddress = wifiInfo.getIpAddress();
        String ipAddressString = Formatter.formatIpAddress(ipAddress);
        String ipPrefix = ipAddressString.substring(0, ipAddressString.lastIndexOf(".") + 1);
        //Log.d("久久算法助手", "IP Range: " + routerIPRange + ipPrefix);
    }

    private static String calculateIPRange(String routerIpAddress, String subnetMask) {
        String[] routerIpParts = routerIpAddress.split("\\.");
        String[] subnetMaskParts = subnetMask.split("\\.");
        int[] ipRange = new int[4];
        for (int i = 0; i < 4; i++) {
            ipRange[i] = Integer.parseInt(routerIpParts[i]) & Integer.parseInt(subnetMaskParts[i]);
        }
        return ipRange[0] + "." + ipRange[1] + "." + ipRange[2] + "." + (ipRange[3] + 1) + " - " + ipRange[0] + "." + ipRange[1] + "." + ipRange[2] + "." + (ipRange[3] + 254);
    }
}
