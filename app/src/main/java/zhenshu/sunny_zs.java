package zhenshu;

import android.os.Build;
import android.util.Log;
import androidx.core.os.EnvironmentCompat;

/* loaded from: classes.dex */
public class sunny_zs {
    public static String cacerts(Boolean modules) {
        boolean mnq = isEmulator();
        String currentAndroidVersion = android.os.Build.VERSION.RELEASE;
        String[] versionParts = currentAndroidVersion.split("\\.");
        float versionFloat = 0.0f;
        if (versionParts.length >= 1) {
            versionFloat += Float.parseFloat(versionParts[0]);
        }
        if (versionParts.length >= 2) {
            versionFloat += Float.parseFloat(versionParts[1]) / 10.0f;
        }
        if (versionParts.length >= 3) {
            versionFloat += Float.parseFloat(versionParts[2]) / 100.0f;
        }
        if (versionParts.length >= 4) {
            versionFloat += Float.parseFloat(versionParts[3]) / 1000.0f;
        }
        float currentVersionFloat = versionFloat;
        if (mnq) {
            int a;
            if (currentVersionFloat >= 14.0f) {
                a = new Root().writeFile_cacerts14(); //安卓14及以上迁移到证书路径
            } else {
                a = new Root().writeFile_cacerts(); //迁移到证书路径
            }
            if (a == 0) {
                return "安装成功!";
            }
            if (modules) {
                int a2 = new Root().writeFile_modules();  //面具模块
                if (a2 == 0) {
                    return "安装成功请重启手机!";
                }
            }
            if (currentVersionFloat < 14.0f) {
                int a2 = new Root().writeFile_lingshi();  //临时证书
                if (a2 == 0) {
                    return "安装成功(临时证书)!";
                }
            }
            if (currentVersionFloat >= 14.0f) {
                int a2 = new Root().writeFile_lingshi14();  //安卓14及以上临时证书
                if (a2 == 0) {
                    return "安装成功(临时证书)!";
                }
            }
            return "安装失败";
        }
        if (modules) {
            int a2 = new Root().writeFile_modules();  //面具模块
            if (a2 == 0) {
                return "安装成功请重启手机!";
            }
        }
        int a4;
        if (currentVersionFloat >= 14.0f) {
            a4 = new Root().writeFile_cacerts14(); //安卓14及以上迁移到证书路径
        } else {
            a4 = new Root().writeFile_cacerts(); //迁移到证书路径
        }
        if (a4 == 0) {
            return "安装成功!";
        }
        if (currentVersionFloat < 14.0f) {
            int a2 = new Root().writeFile_lingshi();  //临时证书
            if (a2 == 0) {
                return "安装成功(临时证书)!";
            }
        }
        if (currentVersionFloat >= 14.0f) {
            int a2 = new Root().writeFile_lingshi14();  //安卓14及以上临时证书
            if (a2 == 0) {
                return "安装成功(临时证书)!";
            }
        }
        return "安装失败";
    }


    public static String getDeviceArchitecture() {
        String[] strArr;
        String arch = "";
        if (Build.VERSION.SDK_INT >= 21) {
            for (String supportedArch : Build.SUPPORTED_ABIS) {
                arch = arch + supportedArch + ",";
            }
            return arch;
        }
        String arch2 = Build.CPU_ABI;
        return arch2;
    }

    public static boolean getarmeabi() {
        String armeabi = getDeviceArchitecture();
        return armeabi.contains("x86_64") || armeabi.contains("x86");
    }

    public static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) || Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains(EnvironmentCompat.MEDIA_UNKNOWN) || Build.HARDWARE.contains("goldfish") || Build.HARDWARE.contains("ranchu") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") || (Build.PRODUCT.contains("sdk") && Build.PRODUCT.contains("google_sdk")) || "google_sdk".equals(Build.PRODUCT) || getarmeabi();
    }
}
