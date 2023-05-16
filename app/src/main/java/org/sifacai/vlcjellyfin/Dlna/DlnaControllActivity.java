package org.sifacai.vlcjellyfin.Dlna;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.sifacai.vlcjellyfin.R;
import org.sifacai.vlcjellyfin.Ui.BaseActivity;

public class DlnaControllActivity extends BaseActivity {
    String TAG = "DLNA控制器";

    private AVTransport avTransport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlna_controll);

        avTransport = getIntent().getParcelableExtra("AVT");

        Log.d(TAG, "onCreate: " + avTransport.controlURL);
    }
}