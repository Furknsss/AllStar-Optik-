package com.optiklab.allstar;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    @Override public void onCreate(Bundle s){super.onCreate(s);LinearLayout root=Ui.base(this);Ui.header(this,root,"Ayarlar","Yapay zekâ ve gizlilik");ScrollView scroll=Ui.scrolling(this,root);LinearLayout body=Ui.vertical(this);scroll.addView(body);
        body.addView(Ui.text(this,"Yapay zekâ çözümü",20,Ui.WHITE,true));Ui.info(this,body,"Fotoğrafını çektiğin soru, yalnızca Çöz işlevini kullandığında Google Gemini API'ye iletilir. Optik ve istatistikler cihazında kalır.");Ui.spacer(this,body,21);
        body.addView(Ui.text(this,"Gemini API anahtarı",14,Ui.WHITE,true));EditText api=Ui.input(this,"API anahtarını gir...");api.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);if(!SecureKey.get(this).isEmpty())api.setHint("Anahtar kayıtlı (değiştirmek için yaz)");body.addView(api,Ui.lp(this,-1,-2,8));Ui.info(this,body,"Anahtar Android Keystore ile şifrelenerek bu cihazda saklanır. API kullanımı hesabının kota ve fiyatlandırmasına tabidir.");Ui.spacer(this,body,23);
        body.addView(Ui.text(this,"Model kimliği",14,Ui.WHITE,true));EditText model=Ui.input(this,"Model kimliği");model.setText(SecureKey.model(this));body.addView(model,Ui.lp(this,-1,-2,8));Ui.info(this,body,"Varsayılan: gemini-3.8-flash. Hesabında desteklenen başka bir modeli kullanabilirsin.");
        TextView save=Ui.button(this,"AYARLARI KAYDET",true);body.addView(save,Ui.lp(this,-1,-2,25));save.setOnClickListener(v->{String m=model.getText().toString().trim();if(!m.matches("[a-zA-Z0-9._-]{3,80}")){Toast.makeText(this,"Geçersiz model adı",Toast.LENGTH_LONG).show();return;}try{String secret=api.getText().toString().trim();if(!secret.isEmpty())SecureKey.put(this,secret);SecureKey.setModel(this,m);Toast.makeText(this,"Kaydedildi",Toast.LENGTH_SHORT).show();finish();}catch(Exception e){Toast.makeText(this,"Anahtar güvenli saklanamadı",Toast.LENGTH_LONG).show();}});
        Ui.spacer(this,body,28);body.addView(Ui.text(this,"Uygulama hakkında",17,Ui.WHITE,true));Ui.info(this,body,"İnternet olmadan optik doldurma, test arama ve sonuç geçmişi çalışır. 48 PDF'den derlenen 2.565 test içeren cevap veritabanı TASLAKTIR. PDF'deki 3 numaralandırma sorunu puanlamadan çıkarılmıştır. Bazı test türleri ayrıca doğrulanmalıdır. 345 yayınevinin resmî uygulaması değildir.");
    }
}
