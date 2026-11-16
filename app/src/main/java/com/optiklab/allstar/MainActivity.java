package com.optiklab.allstar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity {
    private BookDb books; private Store store;
    @Override public void onCreate(Bundle s){super.onCreate(s);books=new BookDb(this);store=new Store(this);show();onDeepLink(getIntent());}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);onDeepLink(intent);}
    @Override protected void onResume(){super.onResume();if(books!=null&&store!=null)show();}
    private void onDeepLink(Intent intent){if(intent!=null&&Intent.ACTION_VIEW.equals(intent.getAction())&&intent.getData()!=null){onQr(intent.getData().toString());}}
    private void onQr(String qr){if(qr==null||qr.trim().isEmpty())return;String id=store.qrTest(qr);if(id!=null&&books.test(id)!=null){startActivity(new Intent(this,OpticActivity.class).putExtra("test_id",id));}
        else{startActivity(new Intent(this,SearchActivity.class).putExtra("qr",qr));}}
    private static final int REQUEST_QR = 7001;
    private void scan() {
        try {
            startActivityForResult(new Intent(this, QrScannerActivity.class), REQUEST_QR);
        } catch (RuntimeException e) {
            android.util.Log.e("AllStarQR", "QR scanner launch failed", e);
            new android.app.AlertDialog.Builder(this).setTitle("QR tarayıcı açılamadı")
                .setMessage("Kamera ekranı başlatılamadı. Dilersen testi arayarak açabilirsin.")
                .setPositiveButton("Test Ara", (d, w) -> openSearch())
                .setNegativeButton("Kapat", null).show();
        }
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QR && resultCode == RESULT_OK && data != null) {
            String qr = data.getStringExtra(QrScannerActivity.EXTRA_QR_RESULT);
            if (qr != null) onQr(qr);
        }
    }
    private void openSearch(){startActivity(new Intent(this,SearchActivity.class));}
    private void show(){LinearLayout root=Ui.base(this);LinearLayout top=Ui.horizontal(this);LinearLayout names=Ui.vertical(this);TextView brand=Ui.text(this,"OPTİK",29,Ui.WHITE,true);brand.setLetterSpacing(0.13f);names.addView(brand);names.addView(Ui.text(this,"345 AllStar kişisel test kontrolü",12,Ui.MUTED,false),Ui.lp(this,-1,-2,5));top.addView(names,new LinearLayout.LayoutParams(0,-2,1));TextView settings=Ui.text(this,"⚙",25,Ui.WHITE,false);settings.setGravity(Gravity.CENTER);settings.setBackground(Ui.outlined(this,Ui.PANEL,16,Ui.STROKE));top.addView(settings,new LinearLayout.LayoutParams(Ui.dp(this,47),Ui.dp(this,47)));settings.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));root.addView(top,Ui.lp(this,-1,-2,5));
        ScrollView scroll=Ui.scrolling(this,root);LinearLayout main=Ui.vertical(this);scroll.addView(main);
        Ui.spacer(this,main,31);main.addView(Ui.text(this,"Sorular bitti. Kontrol zamanı.",24,Ui.WHITE,true));Ui.info(this,main,"QR okut veya testini ara. Optiği doldur, yanlışlarını hemen gör.");Ui.spacer(this,main,22);
        LinearLayout scanCard=Ui.card(this);scanCard.setBackground(Ui.outlined(this,Ui.PANEL2,24,Ui.ACCENT));TextView icon=Ui.text(this,"▦",44,Ui.ACCENT,true);scanCard.addView(icon);scanCard.addView(Ui.text(this,"QR kodunu okut",21,Ui.WHITE,true),Ui.lp(this,-1,-2,9));scanCard.addView(Ui.text(this,"Kamerayla hızlı test kontrolü",13,Ui.MUTED,false),Ui.lp(this,-1,-2,6));TextView qrBtn=Ui.button(this,"QR TARAYICIYI AÇ  ↗",true);scanCard.addView(qrBtn,Ui.lp(this,-1,-2,18));qrBtn.setOnClickListener(v->scan());main.addView(scanCard,Ui.lp(this,-1,-2,5));
        TextView search=Ui.button(this,"⌕   Test ara ve seç",false);main.addView(search,Ui.lp(this,-1,-2,13));search.setOnClickListener(v->openSearch());
        int[] totals=store.summary();Ui.spacer(this,main,28);LinearLayout stats=Ui.horizontal(this);addStat(stats,String.valueOf(totals[0]),"Kontrol edilen");addStat(stats,totals[0]==0?"—":String.format(Locale.forLanguageTag("tr"),"%.0f%%",(totals[1]*100.0/Math.max(1,totals[1]+totals[2]))),"Doğruluk");main.addView(stats);
        Ui.spacer(this,main,29);main.addView(Ui.text(this,"Son Kontroller",19,Ui.WHITE,true));if(totals[0]==0){Ui.info(this,main,"Henüz test çözümü kaydedilmedi. İlk testi seçerek başlayabilirsin.");}else{
            for(Store.Attempt a:store.recent(8)) {BookDb.Test t=books.test(a.testId);if(t==null)continue;LinearLayout item=Ui.card(this);item.addView(Ui.text(this,Ui.title(t),14,Ui.WHITE,true));item.addView(Ui.text(this,t.summary()+"   ·   "+a.correct+"D / "+a.wrong+"Y / "+a.blank+"B",12,Ui.MUTED,false),Ui.lp(this,-1,-2,8));item.setOnClickListener(v->startActivity(new Intent(this,ResultActivity.class).putExtra("attempt_id",a.id)));main.addView(item,Ui.lp(this,-1,-2,10));}
        }
        Ui.spacer(this,main,16);main.addView(Ui.text(this,"Bağımsız kişisel uygulama · Yayınevinin resmî ürünü değildir.",11,Ui.MUTED,false));Ui.spacer(this,main,25);
    }
    private void addStat(LinearLayout row,String value,String label){LinearLayout col=Ui.card(this);col.addView(Ui.text(this,value,24,Ui.WHITE,true));col.addView(Ui.text(this,label,12,Ui.MUTED,false),Ui.lp(this,-1,-2,7));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.rightMargin=Ui.dp(this,8);row.addView(col,p);}
}
