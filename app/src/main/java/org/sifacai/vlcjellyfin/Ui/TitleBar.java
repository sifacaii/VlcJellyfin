package org.sifacai.vlcjellyfin.Ui;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class TitleBar extends FrameLayout {

    public TitleBar(Context context) {
        super(context);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initView(){

    }

    static class Builder{
        LinearLayout mainlayout;
        TextView backBtn;


        public Builder(Context context) {
            mainlayout = new LinearLayout(context);
            mainlayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            backBtn=new TextView(context);
            backBtn.setText("<");


        }
    }
}
