package com.optiklab.allstar;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public final class SettingsActivity extends Activity {
    private static final String[] MODEL_IDS = {
        "auto",
        "gemini-4-hydrogen",
        "gemini-3.8-flash",
        "gemini-3.5-flash-lite",
        "gemini-3.6-flash",
        "gemini-4-carbon",
        "gemini-4-argon"
    };
    private static final String[] MODEL_LABELS = {
        "Otomatik · Hydrogen → 3.8 Flash → Flash-Lite",
        "Gemini 4 Hydrogen",
        "Gemini 3.8 Flash",
        "Gemini 3.5 Flash-Lite",
        "Gemini 3.6 Flash",
        "Gemini 4 Carbon · ücretli erişim",
        "Gemini 4 Argon · ücretli erişim"
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root=Ui.base(this);
        Ui.header(this,root,"Ayarlar","Yapay zekâ ve gizlilik");
        ScrollView scroll=Ui.scrolling(this,root);
        LinearLayout body=Ui.vertical(this);
        scroll.addView(body);
        body.addView(Ui.text(this,"Yapay zekâ çözümü",20,Ui.WHITE,true));
        Ui.info(this,body,"Sorunun fotoğrafı yalnızca çözüm istediğinde Gemini API'ye gönderilir. Optik ve istatistikler cihazında kalır.");
        Ui.spacer(this,body,21);
        body.addView(Ui.text(this,"Gemini API anahtarı",14,Ui.WHITE,true));
        EditText api=Ui.input(this,"API anahtarını gir...");
        api.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if(!SecureKey.get(this).isEmpty())api.setHint("Anahtar kayıtlı (değiştirmek için yaz)");
        body.addView(api,Ui.lp(this,-1,-2,8));
        Ui.info(this,body,"Anahtar Android Keystore ile şifrelenerek bu cihazda saklanır.");
        Ui.spacer(this,body,23);

        body.addView(Ui.text(this,"Yapay zekâ modeli",14,Ui.WHITE,true));
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,MODEL_LABELS);
        spinner.setAdapter(adapter);
        spinner.setBackground(Ui.outlined(this,Ui.PANEL,15,Ui.STROKE));
        spinner.setPadding(Ui.dp(this,10),Ui.dp(this,10),Ui.dp(this,10),Ui.dp(this,10));
        body.addView(spinner,Ui.lp(this,-1,54,9));
        String current=SecureKey.model(this);
        int chosen=0;
        for(int i=0;i<MODEL_IDS.length;i++)if(MODEL_IDS[i].equals(current)){chosen=i;break;}
        spinner.setSelection(chosen);
        Ui.info(this,body,"Otomatik mod ücretsiz erişime uygun modelleri sırayla dener. 503 yoğunluk hatasında yeniden dener ve yedek modele geçer. Model erişimi API anahtarının hesabına bağlıdır.");
        Ui.info(this,body,"Carbon ve Argon ücretli Google API erişimi gerektirebilir. Bunlar yalnızca sen seçersen kullanılır; istekler hesabının kotasına ve ücretlendirmesine tabidir.");

        TextView save=Ui.button(this,"AYARLARI KAYDET",true);
        body.addView(save,Ui.lp(this,-1,-2,25));
        save.setOnClickListener(v->{
            try {
                String secret=api.getText().toString().trim();
                if(!secret.isEmpty())SecureKey.put(this,secret);
                SecureKey.setModel(this,MODEL_IDS[spinner.getSelectedItemPosition()]);
                Toast.makeText(this,"Kaydedildi",Toast.LENGTH_SHORT).show();
                finish();
            } catch(Exception e) {
                Toast.makeText(this,"Ayarlar kaydedilemedi",Toast.LENGTH_LONG).show();
            }
        });
        Ui.spacer(this,body,28);
        body.addView(Ui.text(this,"Uygulama hakkında",17,Ui.WHITE,true));
        Ui.info(this,body,"Offline test arama, optik kontrol ve geçmiş. QR ve fotoğraf için kamera izni gerektiğinde istenir. 345 Yayınevi'nin resmî uygulaması değildir.");
    }
}
