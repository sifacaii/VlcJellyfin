package org.sifacai.vlcjellyfin;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import org.conscrypt.Conscrypt;
import org.sifacai.vlcjellyfin.Utils.JfClient;

import java.security.Security;

public class application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
        JfClient.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
