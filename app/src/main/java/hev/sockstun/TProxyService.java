package hev.sockstun;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.*;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import tun2socks.Tun2socks;

import kotlin.jvm.internal.Intrinsics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;

public class TProxyService extends VpnService {
    private static native void TProxyStartService(String config_path, int fd);

    private static native void TProxyStopService();

    private static native long[] TProxyGetStats();

    public static final String ACTION_CONNECT = "hev.sockstun.CONNECT";
    public static final String ACTION_DISCONNECT = "hev.sockstun.DISCONNECT";

    private ParcelFileDescriptor tunFd = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            try {
                stopVPN();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return START_NOT_STICKY;
        }
        onStartCommand_gojni(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopService();
        super.onRevoke();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startService() {
        if (tunFd != null)
            return;
        Preferences prefs = new Preferences(this);

        /* VPN */
        String session = new String();
        VpnService.Builder builder = new VpnService.Builder();
        builder.setBlocking(false);
        builder.setMtu(prefs.getTunnelMtu());
        if (prefs.getIpv4()) {
            String addr = prefs.getTunnelIpv4Address();
            int prefix = prefs.getTunnelIpv4Prefix();
            String dns = prefs.getDnsIpv4();
            builder.addAddress(addr, prefix);
            builder.addRoute("0.0.0.0", 0);
            if (!dns.isEmpty())
                builder.addDnsServer(dns);
            session += "IPv4";
        }
        if (prefs.getIpv6()) {
            String addr = prefs.getTunnelIpv6Address();
            int prefix = prefs.getTunnelIpv6Prefix();
            String dns = prefs.getDnsIpv6();
            builder.addAddress(addr, prefix);
            builder.addRoute("::", 0);
            if (!dns.isEmpty())
                builder.addDnsServer(dns);
            if (!session.isEmpty())
                session += " + ";
            session += "IPv6";
        }
        boolean disallowSelf = true;
        if (prefs.getGlobal()) {
            session += "/Global";
        } else {
            for (String appName : prefs.getApps()) {
                try {
                    builder.addAllowedApplication(appName);
                    disallowSelf = false;
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            session += "/per-App";
        }
        if (disallowSelf) {
            String selfName = getApplicationContext().getPackageName();
            try {
                builder.addDisallowedApplication(selfName);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        builder.setSession(session);
        tunFd = builder.establish();
        if (tunFd == null) {
            stopSelf();
            return;
        }

        /* TProxy */
        File tproxy_file = new File(getCacheDir(), "tproxy.conf");
        try {
            tproxy_file.createNewFile();
            FileOutputStream fos = new FileOutputStream(tproxy_file, false);

            String tproxy_conf = "misc:\n" +
                    "  task-stack-size: " + prefs.getTaskStackSize() + "\n" +
                    "tunnel:\n" +
                    "  mtu: " + prefs.getTunnelMtu() + "\n";

            tproxy_conf += "socks5:\n" +
                    "  port: " + prefs.getSocksPort() + "\n" +
                    "  address: '" + prefs.getSocksAddress() + "'\n" +
                    "  udp: '" + (prefs.getUdpInTcp() ? "tcp" : "udp") + "'\n";

            if (!prefs.getSocksUsername().isEmpty() &&
                    !prefs.getSocksPassword().isEmpty()) {
                tproxy_conf += "  username: '" + prefs.getSocksUsername() + "'\n";
                tproxy_conf += "  password: '" + prefs.getSocksPassword() + "'\n";
            }

            fos.write(tproxy_conf.getBytes());
            fos.close();
        } catch (IOException e) {
            return;
        }
        TProxyStartService(tproxy_file.getAbsolutePath(), tunFd.getFd());
        prefs.setEnable(true);

        String channelName = "socks5";
        initNotificationChannel(channelName);
        createNotification(channelName);
    }

    public void stopService() {
        if (tunFd == null)
            return;

        stopForeground(true);

        /* TProxy */
        TProxyStopService();

        /* VPN */
        try {
            tunFd.close();
        } catch (IOException e) {
        }
        tunFd = null;

        System.exit(0);
    }

    @SuppressLint("ForegroundServiceType")
    private void createNotification(String channelName) {
        Intent i = new Intent(this, TProxyService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelName);
        Notification notify = notification
                .setContentTitle(getString(R.string.app_name) + "[Service is running]")
                .setSmallIcon(R.drawable.tt)
                .setContentIntent(pi)
                .build();
        startForeground(1, notify);
    }

    // create NotificationChannel
    private void initNotificationChannel(String channelName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(channelName, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }


    //以下为libgojni.so

    public static final String PREFERENCE_CONFIG_KEY = "preference_config_key";
    public static final String SUBSCRIBE_CONFIG_URL_KEY = "subscribe_config_url_key";
    public static final String DEFAULT_CONFIG = "{\n\n}";
    private Thread bgThread;
    private String configString = "";
    private ParcelFileDescriptor pfd = null;
    private FileInputStream inputStream = null;
    private FileOutputStream outputStream = null;
    @TargetApi(28)
    private Network underlyingNetwork = null;
    private static volatile boolean running = false;
    private static volatile boolean API_running = false;

    @TargetApi(28)
    private void setUnderlyingNetwork(Network value) {
        setUnderlyingNetworks((value == null) ? null : new Network[]{value});
        underlyingNetwork = value;
    }

    private static final NetworkRequest defaultNetworkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build();
    private final ConnectivityManager.NetworkCallback defaultNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            underlyingNetwork = network;
            setUnderlyingNetwork(network);
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            underlyingNetwork = network;
            setUnderlyingNetwork(network);
        }

        @Override
        public void onLost(Network network) {
            underlyingNetwork = null;
            setUnderlyingNetwork(null);
        }
    };

    public static final class Service implements tun2socks.VpnService {
        private final VpnService vpnService;

        public Service(VpnService service) {
            Intrinsics.checkParameterIsNotNull(service, "service");
            this.vpnService = service;
        }

        @Override
        public boolean protect(long fd) {
            return vpnService.protect((int) fd);
        }

        @Override // tun2socks.VpnService
        public void didStop() {
            //this.vpnService.stopVpn();
        }

    }

    public void UI_peizhi() {
        Preferences prefs = new Preferences(this);
        String Socksaddr = prefs.getSocksAddress();
        int SocksPort = prefs.getSocksPort();
        String SocksUsername = prefs.getSocksUsername();
        String SocksPassword = prefs.getSocksPassword();
        boolean global = prefs.getGlobal();

        Set<String> getApps = prefs.getApps();

        String per_app_allowed_app_list = "";
        int ii = 1;
        for (String appName : prefs.getApps()) {
            if (getApps.size() != ii) {
                per_app_allowed_app_list = per_app_allowed_app_list + appName + ",";
            } else {
                per_app_allowed_app_list = per_app_allowed_app_list + appName;
            }
            ii = ii + 1;
        }

        gojni_storage.Preferences.putString_zz(getApplicationContext(), "socks_addr", Socksaddr);
        gojni_storage.Preferences.putString_zz(getApplicationContext(), "socks_port", String.valueOf(SocksPort));
        gojni_storage.Preferences.putString_zz(getApplicationContext(), "socks_user", SocksUsername);
        gojni_storage.Preferences.putString_zz(getApplicationContext(), "socks_pass", SocksPassword);
        gojni_storage.Preferences.putBool(getApplicationContext(), "quanju_global", global);
        if (global) {
            gojni_storage.Preferences.putBool(getApplicationContext(), "is_enable_per_app_vpn", false);
        } else {
            gojni_storage.Preferences.putBool(getApplicationContext(), "is_enable_per_app_vpn", true);
        }
        gojni_storage.Preferences.putString(getApplicationContext(), "per_app_mode", String.valueOf(0));
        gojni_storage.Preferences.putString(getApplicationContext(), "per_app_allowed_app_list", per_app_allowed_app_list); //允许

    }


    public int onStartCommand_gojni(Intent intent, int flags, int startId) {

        UI_peizhi();
        if (running) {
            API_running = true;
            return START_NOT_STICKY;
        }

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        configString = gojni_storage.Preferences.getString(getApplicationContext(), PREFERENCE_CONFIG_KEY, DEFAULT_CONFIG);
        String socks_addr = gojni_storage.Preferences.getString_zz(getApplicationContext(), "socks_addr", DEFAULT_CONFIG);
        String socks_port = gojni_storage.Preferences.getString_zz(getApplicationContext(), "socks_port", DEFAULT_CONFIG);
        String socks_user = gojni_storage.Preferences.getString_zz(getApplicationContext(), "socks_user", DEFAULT_CONFIG);
        String socks_pass = gojni_storage.Preferences.getString_zz(getApplicationContext(), "socks_pass", DEFAULT_CONFIG);
        bgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String localDns = gojni_storage.Preferences.getString(getApplicationContext(), "local_dns", "223.5.5.5");

                    Builder builder = new Builder()
                            .setSession("Kitsunebi")
                            .setMtu(1500)
                            .addAddress("10.233.233.252", 30)
                            //.addAddress("fe80::e063:f4bf:c97b:2642", 64)
                            .addDnsServer(localDns)
                            .addRoute("0.0.0.0", 0);


                    boolean isEnablePerAppVpn = gojni_storage.Preferences.getBool(getApplicationContext(), "is_enable_per_app_vpn", null);

                    if (isEnablePerAppVpn) {
                        String perAppMode = gojni_storage.Preferences.getString(getApplicationContext(), "per_app_mode", null);
                        int mode = Integer.parseInt(perAppMode);

                        //int mode = 0;
                        switch (mode) {
                            case 0:
                                String allowedAppList = gojni_storage.Preferences.getString(getApplicationContext(), "per_app_allowed_app_list", null); //允许
                                for (String packageName : allowedAppList.split(",")) {
                                    builder.addAllowedApplication(packageName);//允许

                                }
                                break;
                            case 1:
                                String disallowedAppList = gojni_storage.Preferences.getString(getApplicationContext(), "per_app_disallowed_app_list", null); //不允许
                                for (String packageName : disallowedAppList.split(",")) {
                                    builder.addDisallowedApplication(packageName);//不允许

                                }
                                break;
                            default:
                                break;
                        }
                    }

                    pfd = builder.establish();


                    if (Build.VERSION.SDK_INT >= 28) {
                        //   cm.requestNetwork(defaultNetworkRequest, defaultNetworkCallback);
                    }

                    inputStream = new FileInputStream(pfd.getFileDescriptor());
                    outputStream = new FileOutputStream(pfd.getFileDescriptor());


                    Service service = new Service(TProxyService.this);

                    Tun2socks.shutdownActiveConnections();
                    Tun2socks.setLocalDNS(localDns + ":53");

                    String configString;
                    if (socks_pass.isEmpty() || socks_user.isEmpty()) {

                        configString = "{\"dns\" : {}, \"log\" : {\"loglevel\" : \"none\"}, \"outbounds\" : ["
                                + "{\"protocol\" : \"socks\", \"settings\" : {\"servers\" : [{\"address\" : \"" + socks_addr + "\", \"port\" : " + socks_port + "}]}, \"streamSettings\" : {\"network\" : \"tcp\"}, \"tag\" : \"proxy\"}, "
                                + "{\"protocol\" : \"freedom\", \"settings\" : {\"domainStrategy\" : \"AsIs\"}, \"tag\" : \"direct\"}, "
                                + "{\"protocol\" : \"blackhole\", \"settings\" : {}, \"tag\" : \"block\"}], \"policy\" : {\"levels\" : {\"0\": {\"bufferSize\" : 4096, \"connIdle\" : 300, \"downlinkOnly\" : 0, \"handshake\" : 4, \"uplinkOnly\" : 0}}}, "
                                + "\"routing\" : {\"domainStrategy\" : \"AsIs\", \"rules\" : [{\"network\" : \"tcp,udp\", \"outboundTag\" : \"proxy\", \"type\" : \"field\"}]}}";

                    } else {
                        configString = "{\"dns\" : {}, \"log\" : {\"loglevel\" : \"none\"}, \"outbounds\" : ["
                                + "{\"protocol\" : \"socks\", \"settings\" : {\"servers\" : [{\"address\" : \"" + socks_addr + "\", \"port\" : " + socks_port + ", \"users\" : [{\"pass\" : \"" + socks_pass + "\", \"user\" : \"" + socks_user + "\"}]}]}, \"streamSettings\" : {\"network\" : \"tcp\"}, \"tag\" : \"proxy\"}, "
                                + "{\"protocol\" : \"freedom\", \"settings\" : {\"domainStrategy\" : \"AsIs\"}, \"tag\" : \"direct\"}, "
                                + "{\"protocol\" : \"blackhole\", \"settings\" : {}, \"tag\" : \"block\"}], \"policy\" : {\"levels\" : {\"0\": {\"bufferSize\" : 4096, \"connIdle\" : 300, \"downlinkOnly\" : 0, \"handshake\" : 4, \"uplinkOnly\" : 0}}}, "
                                + "\"routing\" : {\"domainStrategy\" : \"AsIs\", \"rules\" : [{\"network\" : \"tcp,udp\", \"outboundTag\" : \"proxy\", \"type\" : \"field\"}]}}";

                    }



                    long detachFd = pfd.detachFd();
                    long ret = Tun2socks.startV2Ray(detachFd, service, null, configString.getBytes(), "tun2socks", "", getApplicationContext().getFilesDir().getAbsolutePath(), true, false, getApplicationContext().getFilesDir().getAbsolutePath());
                    if (ret != 0) {
                        sendBroadcast(new Intent("vpn_start_err_config"));
                        stopVPN();

                        API_running = true;
                        return;
                    }


                    sendBroadcast(new Intent("vpn_started"));
                    gojni_storage.Preferences.putBool(getApplicationContext(), "vpn_is_running", true);

                    running = true;
                    API_running = true;

                    String channelName = "socks5";
                    initNotificationChannel(channelName);
                    createNotification(channelName);


                } catch (Exception e) {
                    e.printStackTrace();
                    API_running = true;
                }
            }
        });
        bgThread.start();

        return START_NOT_STICKY;
    }

    private void stopVPN() throws IOException {
        UI_peizhi();

        //Log.d("XXXXXX", "关闭VPN");
        Tun2socks.stopV2Ray();
        if (pfd != null) {
            pfd.close();
            pfd = null;
        }
        inputStream = null;
        outputStream = null;
        running = false;
        sendBroadcast(new Intent("vpn_stopped"));
        gojni_storage.Preferences.putBool(getApplicationContext(), "vpn_is_running", false);
        stopSelf();
        API_running = true;
    }
}
