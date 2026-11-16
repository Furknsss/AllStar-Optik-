package com.optiklab.allstar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class Ui {
    static final int BG=Color.rgb(0,0,0), PANEL=Color.rgb(19,24,36), PANEL2=Color.rgb(27,33,47), WHITE=Color.rgb(242,245,255), MUTED=Color.rgb(148,161,182), ACCENT=Color.rgb(110,139,255), SUCCESS=Color.rgb(95,221,164), DANGER=Color.rgb(252,111,130), STROKE=Color.rgb(48,56,75);
    private Ui() {}
    static int dp(Context c,float v){return Math.round(v*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable bg(Context c,int fill,int radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(c,radius));return d;}
    static GradientDrawable outlined(Context c,int fill,int radius,int stroke){GradientDrawable d=bg(c,fill,radius);d.setStroke(dp(c,1),stroke);return d;}
    static TextView text(Context c,String s,int sp,int col,boolean bold){TextView t=new TextView(c);t.setText(s);t.setTextColor(col);t.setTextSize(sp);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL); t.setIncludeFontPadding(false);return t;}
    static LinearLayout vertical(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static LinearLayout horizontal(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    static LinearLayout base(Activity a){a.getWindow().setStatusBarColor(BG);a.getWindow().setNavigationBarColor(BG);a.getWindow().getDecorView().setSystemUiVisibility(0);LinearLayout r=vertical(a);r.setBackgroundColor(BG);r.setPadding(dp(a,22),dp(a,20),dp(a,22),dp(a,16));a.setContentView(r);return r;}
    static ScrollView scrolling(Context c,LinearLayout root){ScrollView v=new ScrollView(c);v.setFillViewport(false);v.setVerticalScrollBarEnabled(false);root.addView(v,new LinearLayout.LayoutParams(-1,0,1));return v;}
    static LinearLayout card(Context c){LinearLayout l=vertical(c);l.setPadding(dp(c,17),dp(c,17),dp(c,17),dp(c,17));l.setBackground(outlined(c,PANEL,20,STROKE));return l;}
    static TextView button(Context c,String txt,boolean primary){TextView t=text(c,txt,15,primary?BG:WHITE,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(c,19),dp(c,16),dp(c,19),dp(c,16));t.setBackground(primary?bg(c,ACCENT,16):outlined(c,PANEL2,16,STROKE));return t;}
    static LinearLayout.LayoutParams lp(Context c,int w,int h,int mt){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:dp(c,w),h<0?h:dp(c,h));p.topMargin=dp(c,mt);return p;}
    static void spacer(Context c,LinearLayout col,int height){View v=new View(c);col.addView(v,lp(c,1,height,0));}
    static void header(Activity a,LinearLayout root,String title,String subtitle){LinearLayout r=horizontal(a);TextView back=text(a,"‹",34,WHITE,false);back.setGravity(Gravity.CENTER);r.addView(back,new LinearLayout.LayoutParams(dp(a,36),dp(a,42)));back.setOnClickListener(v->a.finish());LinearLayout titles=vertical(a);titles.addView(text(a,title,21,WHITE,true));if(subtitle!=null)titles.addView(text(a,subtitle,12,MUTED,false),lp(a,-1,-2,5));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.leftMargin=dp(a,10);r.addView(titles,p);root.addView(r,lp(a,-1,-2,0));spacer(a,root,20);}
    static EditText input(Context c,String hint){EditText e=new EditText(c);e.setSingleLine(true);e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setTextSize(15);e.setPadding(dp(c,18),dp(c,15),dp(c,18),dp(c,15));e.setHint(hint);e.setBackground(outlined(c,PANEL,16,STROKE));return e;}
    static void info(Context c,LinearLayout col,String message){TextView v=text(c,message,12,MUTED,false);v.setLineSpacing(dp(c,3),1);col.addView(v,lp(c,-1,-2,10));}
    static void divider(Context c,LinearLayout p){View v=new View(c);v.setBackgroundColor(STROKE);p.addView(v,lp(c,-1,1,14));}
    static String title(BookDb.Test t){String topic=t.topic==null||t.topic.trim().isEmpty()?"Konu belirlenemedi":t.topic;String label=t.label==null||t.label.trim().isEmpty()?"Test türü doğrulanmadı":t.label;return topic+" · "+label;}
}
