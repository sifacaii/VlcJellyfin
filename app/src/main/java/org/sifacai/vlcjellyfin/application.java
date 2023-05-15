package org.sifacai.vlcjellyfin;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import org.sifacai.vlcjellyfin.Utils.JfClient;

public class application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JfClient.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
