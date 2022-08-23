package org.sifacai.vlcjellyfin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

public class PopMenu extends PopupWindow {

    public class menu{
        public int groupid;
        public int id;
        public int orderid;
        public String name;
        public View v;
    }

    private Context context;
    private ArrayList<menu> items;
    private View attView;
    private LinearLayout contentView;

    public PopMenu(Context context, View attView) {
        super(context);
        this.context = context;
        items = new ArrayList<>();

        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        contentView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.popmenu,null);
        setContentView(contentView);
        this.attView = attView;
    }

    public menu add(int groupid,int id,int orderid,String name){
        menu m = new menu();
        m.groupid = groupid;
        m.id = id;
        m.orderid = orderid;
        m.name = name;
        View v = LayoutInflater.from(context).inflate(R.layout.popmenu_item,null);
        m.v = v;
        ((TextView)v).setText(name);
        items.add(m);
        contentView.addView(v);
        return m;
    }

    public void show(){
        contentView.measure(makeDropDownMeasureSpec(getWidth())
                ,makeDropDownMeasureSpec(getHeight()));
        int offx = 0;
        int offy = contentView.getMeasuredHeight() + attView.getHeight() + 2;
        showAsDropDown(attView,offx,-offy);
    }

    public void show(int index){
        show();
        items.get(index).v.requestFocus();
    }

    public void show(String name){
        show();
        for (menu m:items) {
            if(m.name.equals(name)){
                m.v.requestFocus();
            }
        }
    }

    @SuppressWarnings("ResourceType")
    private static int makeDropDownMeasureSpec(int measureSpec) {
        int mode;
        if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mode = View.MeasureSpec.UNSPECIFIED;
        } else {
            mode = View.MeasureSpec.EXACTLY;
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode);
    }
}
