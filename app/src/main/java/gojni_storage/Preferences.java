package gojni_storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public class Preferences {

    public static void putString_zz(Context context, String k, String v) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference_zz", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(k, v);
        editor.commit();
    }
    public static String getString_zz(Context context, String k,String default1) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference_zz", Context.MODE_PRIVATE);
        if (default1 != null) {
            return sharedPref.getString(k, default1);
        } else {
            return sharedPref.getString(k, "");
        }
    }



    public static void putString(Context context, String k, String v) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(k, v);
        editor.commit();
    }

    public static String getString(Context context, String k,String default1) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference", Context.MODE_PRIVATE);
        if (default1 != null) {
            return sharedPref.getString(k, default1);
        } else {
            return sharedPref.getString(k, "");
        }
    }

    public static void putBool(Context context, String k, boolean v) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(k, v);
        editor.commit();
    }

    public static boolean getBool(Context context, String k, Boolean default1) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference", Context.MODE_PRIVATE);
        if (default1 != null) {
            return sharedPref.getBoolean(k, default1);
        } else {
            return sharedPref.getBoolean(k, false);
        }
    }

    public static void putInt(Context context, String k, int v) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(k, v);
        editor.commit();
    }

    public static int getInt(Context context, String k, @Nullable Integer default1) {
        SharedPreferences sharedPref = context.getSharedPreferences("config_preference", Context.MODE_PRIVATE);
        if (default1 != null) {
            return sharedPref.getInt(k, default1);
        } else {
            return sharedPref.getInt(k, 0);
        }
    }
}