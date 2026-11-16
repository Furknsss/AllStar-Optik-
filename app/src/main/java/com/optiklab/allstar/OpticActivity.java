package com.optiklab.allstar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

public class OpticActivity extends Activity {
    BookDb.Test test;List<BookDb.Answer> keys;Store store;String[] selected;TextView progress;LinearLayout optRows;
    @Override public void onCreate(Bundle s){super.onCreate(s);String id=getIntent().getStringExtra("test_id");BookDb b=new BookDb(this);test=b.test(id);if(test==null){finish();return;}keys=b.answers(id);store=new Store(this);selected=new String[keys.size()];
        if(s!=null){String[] restore=s.getStringArray("selected_answers");if(restore!=null&&restore.length==selected.length)selected=restore;}
        show();}
    private void show(){LinearLayout root=Ui.base(this);Ui.header(this,root,"Optik Form",test.summary());root.addView(Ui.text(this,Ui.title(test),19,Ui.WHITE,true));progress=Ui.text(this,"0 / "+keys.size()+" işaretlendi",13,Ui.ACCENT,true);root.addView(progress,Ui.lp(this,-1,-2,11));if(test.label==null||test.label.trim().isEmpty())Ui.info(this,root,"Bu testin etiketi PDF'de belirsiz. Doğru kitap, test ve soru sayısını kontrol et.");
        ScrollView scroll=Ui.scrolling(this,root);optRows=Ui.vertical(this);scroll.addView(optRows);Ui.spacer(this,optRows,10);
        for(int i=0;i<keys.size();i++){
            final int index=i;LinearLayout row=Ui.horizontal(this);row.setPadding(0,Ui.dp(this,11),0,Ui.dp(this,11));TextView num=Ui.text(this,String.format(java.util.Locale.ROOT,"%02d",keys.get(i).printedNumber),15,Ui.MUTED,true);row.addView(num,new LinearLayout.LayoutParams(Ui.dp(this,40),Ui.dp(this,46)));
            for(String letter:new String[]{"A","B","C","D","E"}) {TextView cell=Ui.text(this,letter,15,Ui.WHITE,true);cell.setGravity(Gravity.CENTER);cell.setBackground(Ui.outlined(this,Ui.PANEL,14,Ui.STROKE));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,46),1);p.leftMargin=Ui.dp(this,4);row.addView(cell,p);cell.setOnClickListener(v->{selected[index]=letter.equals(selected[index])?null:letter;refresh(row,index);updateProgress();});}
            optRows.addView(row);if(selected[i]!=null)refresh(row,i);Ui.divider(this,optRows);
        }
        updateProgress();TextView submit=Ui.button(this,"KONTROL ET  →",true);root.addView(submit,Ui.lp(this,-1,-2,12));submit.setOnClickListener(v->submit());
    }
    @Override protected void onSaveInstanceState(Bundle out){out.putStringArray("selected_answers",selected);super.onSaveInstanceState(out);}
    private void refresh(LinearLayout row,int i){for(int j=1;j<=5;j++){TextView v=(TextView)row.getChildAt(j);boolean on=("ABCDE".charAt(j-1)+"").equals(selected[i]);v.setBackground(Ui.outlined(this,on?Ui.ACCENT:Ui.PANEL,14,on?Ui.ACCENT:Ui.STROKE));v.setTextColor(on?Ui.BG:Ui.WHITE);}}
    private void updateProgress(){int n=0;for(String s:selected)if(s!=null)n++;progress.setText(n+" / "+keys.size()+" işaretlendi");}
    private void submit(){if(test.review==1){new android.app.AlertDialog.Builder(this).setTitle("Değerlendirme kapalı").setMessage("Kaynak PDF'de numaralandırma hatası bulundu. Yanlış sonuç üretmemek için bu test puanlanamaz.").setPositiveButton("Tamam",(d,w)->{}).show();return;}
        int empty=0;for(String v:selected)if(v==null)empty++;
        new android.app.AlertDialog.Builder(this).setTitle("Testi bitir?").setMessage(empty>0?empty+" soru boş. Sonuç hesaplanacak ve geçmişine kaydedilecek.":"Tüm sorular işaretlendi. Sonuçlarını kaydedelim mi?").setNegativeButton("Devam et",(d,w)->{}).setPositiveButton("Sonuçları gör",(d,w)->{
            long attempt=store.save(test.id,selected,keys);startActivity(new Intent(this,ResultActivity.class).putExtra("attempt_id",attempt));finish();
        }).show();
    }
}
