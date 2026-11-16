package com.optiklab.allstar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;

public class SearchActivity extends Activity {
    BookDb books;Store store;LinearLayout results;Spinner program,subject,book;EditText query;String qr;
    @Override public void onCreate(Bundle s){super.onCreate(s);books=new BookDb(this);store=new Store(this);qr=getIntent().getStringExtra("qr");show();}
    private Spinner spinner(String[] opts){Spinner s=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,opts);s.setAdapter(adapter);s.setBackground(Ui.outlined(this,Ui.PANEL,15,Ui.STROKE));s.setPadding(Ui.dp(this,9),Ui.dp(this,4),Ui.dp(this,9),Ui.dp(this,4));return s;}
    private void show(){LinearLayout root=Ui.base(this);Ui.header(this,root,qr==null?"Test seç":"QR'ı tanımla",qr==null?"Ders, konu veya test ara":"Bu kodu hangi testle eşleştirelim?");
        if(qr!=null){LinearLayout banner=Ui.card(this);banner.addView(Ui.text(this,"İlk okutma",14,Ui.ACCENT,true));banner.addView(Ui.text(this,"Testini bir kez seç. Bu QR daha sonra doğrudan optiği açacak.",12,Ui.MUTED,false),Ui.lp(this,-1,-2,7));root.addView(banner,Ui.lp(this,-1,-2,0));Ui.spacer(this,root,12);}
        query=Ui.input(this,"Konu, test veya ders ara...");root.addView(query,Ui.lp(this,-1,-2,0));Ui.spacer(this,root,12);LinearLayout filters=Ui.horizontal(this);program=spinner(new String[]{"Tümü","TYT","AYT"});LinearLayout.LayoutParams f=new LinearLayout.LayoutParams(0,Ui.dp(this,51),1);f.rightMargin=Ui.dp(this,8);filters.addView(program,f);subject=spinner(new String[]{"Tüm dersler"});filters.addView(subject,new LinearLayout.LayoutParams(0,Ui.dp(this,51),1));root.addView(filters);book=spinner(new String[]{"Tüm kitaplar","1. Kitap","2. Kitap","3. Kitap","4. Kitap","5. Kitap","6. Kitap"});root.addView(book,Ui.lp(this,-1,51,10));Ui.spacer(this,root,14);
        ScrollView scroll=Ui.scrolling(this,root);results=Ui.vertical(this);scroll.addView(results);
        program.setOnItemSelectedListener(listener(()->{String p=prog();List<String> opts=books.subjects(p);subject.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,opts));reload();}));subject.setOnItemSelectedListener(listener(this::reload));book.setOnItemSelectedListener(listener(this::reload));query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int ct,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){reload();}public void afterTextChanged(Editable e){}});reload();
    }
    private AdapterView.OnItemSelectedListener listener(Runnable r){return new AdapterView.OnItemSelectedListener(){public void onNothingSelected(AdapterView<?> p){}public void onItemSelected(AdapterView<?> p,View v,int pos,long id){r.run();}};}
    String prog(){int i=program.getSelectedItemPosition();return i==1?"TYT":i==2?"AYT":"";}
    private void reload(){if(results==null||program==null||subject==null||book==null||query==null)return;results.removeAllViews();String s=subject.getSelectedItem()==null?"":subject.getSelectedItem().toString();if(s.equals("Tüm dersler"))s="";List<BookDb.Test> list=books.search(query.getText().toString(),prog(),s,book.getSelectedItemPosition(),70);
        if(list.isEmpty()){Ui.info(this,results,"Eşleşen test bulunamadı. Aramayı kısaltmayı dene.");return;}
        results.addView(Ui.text(this,list.size()==70?"İlk 70 eşleşme — aramayı daraltabilirsin":list.size()+" test",12,Ui.MUTED,false),Ui.lp(this,-1,-2,0));for(BookDb.Test t:list){LinearLayout item=Ui.card(this);TextView head=Ui.text(this,Ui.title(t),15,Ui.WHITE,true);item.addView(head);TextView meta=Ui.text(this,t.summary()+"   ·   "+t.questions+" soru",12,Ui.MUTED,false);item.addView(meta,Ui.lp(this,-1,-2,7));if(t.review==1)item.addView(Ui.text(this,"⚠ Cevap anahtarında numara hatası",11,Ui.DANGER,false),Ui.lp(this,-1,-2,8));else if(t.label==null||t.label.trim().isEmpty())item.addView(Ui.text(this,"! Test etiketi doğrulanmalı",11,Ui.MUTED,false),Ui.lp(this,-1,-2,8));item.setOnClickListener(v->choose(t));results.addView(item,Ui.lp(this,-1,-2,10));}}
    private void choose(BookDb.Test t){if(t.review==1){new android.app.AlertDialog.Builder(this).setTitle("Kontrol gerekli").setMessage("Bu testin basılı cevap anahtarında numaralandırma hatası var. Yanlış puanlama yapmamak için şu an optik kontrolü kapalı.").setPositiveButton("Anladım",(d,w)->{}).show();return;}
        if(t.label==null||t.label.trim().isEmpty()){
            new android.app.AlertDialog.Builder(this).setTitle("Etiket doğrulanmadı").setMessage("Bu kaydın test türü PDF'den kesin okunamadı. Kitabındaki konuyu, test sırasını ve "+t.questions+" soru olduğunu karşılaştır.\n\nKaynak: "+t.pdf+", PDF sayfa "+t.page).setPositiveButton("Doğru test",(d,w)->open(t)).setNegativeButton("Geri",(d,w)->{}).show();
        }else open(t);
    }
    private void open(BookDb.Test t){if(qr!=null)store.mapQr(qr,t.id);startActivity(new Intent(this,OpticActivity.class).putExtra("test_id",t.id));finish();}
}
