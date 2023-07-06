package com.nivel4.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.util.HashMap;
import java.util.Map;

public class FileManager {
    private static final String hostStr = "host";
    private static final String portStr = "port";
    private static final String protocolStr = "protocol";
    private static final String FILE = "shared_preferences";

    public static void generateSharedPrefs(Context context, String host, String port, String protocol) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(FILE, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(hostStr, Base64.encodeToString(host.getBytes(), Base64.NO_WRAP));
            editor.putString(portStr, Base64.encodeToString(port.getBytes(), Base64.NO_WRAP));
            editor.putString(protocolStr, Base64.encodeToString(protocol.getBytes(), Base64.NO_WRAP));


            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> readSharedPrefs(Context context) {
        Map<String, String> keyValueMap = new HashMap<>();

        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(FILE, context.MODE_PRIVATE);
            String host = sharedPreferences.getString(hostStr, null);
            String port = sharedPreferences.getString(portStr, null);
            String protocol = sharedPreferences.getString(protocolStr, null);

            if (!host.isEmpty()) {
                keyValueMap.put(hostStr, new String(Base64.decode(host, Base64.NO_WRAP)));
            }

            if (!port.isEmpty()) {
                keyValueMap.put(portStr, new String(Base64.decode(port, Base64.NO_WRAP)));
            }

            if (!protocol.isEmpty()) {
                keyValueMap.put(protocolStr, new String(Base64.decode(protocol, Base64.NO_WRAP)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyValueMap;
    }

    public static boolean checkSharedPrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        return sharedPreferences.contains(hostStr) && sharedPreferences.contains(portStr);
    }
}
