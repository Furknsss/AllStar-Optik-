package com.optiklab.allstar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends Activity {
    @Override public void onCreate(Bundle b){super.onCreate(b);Store st=new Store(this);Store.Attempt attempt=st.attempt(getIntent().getLongExtra("attempt_id",-1));if(attempt==null){finish();return;}
        BookDb books=new BookDb(this);BookDb.Test test=books.test(attempt.testId);if(test==null){finish();return;}List<BookDb.Answer> keys=books.answers(test.id);
        LinearLayout root=Ui.base(this);Ui.header(this,root,"Test Sonucu",test.summary());ScrollView scroll=Ui.scrolling(this,root);LinearLayout body=Ui.vertical(this);scroll.addView(body);
        body.addView(Ui.text(this,Ui.title(test),18,Ui.WHITE,true));Ui.spacer(this,body,21);
        LinearLayout stats=Ui.horizontal(this);score(stats,String.valueOf(attempt.correct),"Doğru",Ui.SUCCESS);score(stats,String.valueOf(attempt.wrong),"Yanlış",Ui.DANGER);score(stats,String.valueOf(attempt.blank),"Boş",Ui.WHITE);body.addView(stats);
        double net=attempt.correct-attempt.wrong/4.0;TextView netText=Ui.text(this,String.format(Locale.forLanguageTag("tr-TR"),"%.2f net",net),25,Ui.WHITE,true);body.addView(netText,Ui.lp(this,-1,-2,22));Ui.info(this,body,"Doğru − (Yanlış / 4). Yanlışlarını çözmek için soruya dokun.");Ui.spacer(this,body,25);
        body.addView(Ui.text(this,"Soru analizi",19,Ui.WHITE,true));
        for(int i=0;i<keys.size();i++){
            BookDb.Answer k=keys.get(i);String mine=i<attempt.choices.length?attempt.choices[i]:null;boolean correct=k.correct.equals(mine);boolean blank=mine==null;final int number=k.printedNumber;
            LinearLayout item=Ui.horizontal(this);item.setPadding(Ui.dp(this,12),Ui.dp(this,13),Ui.dp(this,12),Ui.dp(this,13));item.setBackground(Ui.outlined(this,Ui.PANEL,14,Ui.STROKE));
            TextView num=Ui.text(this,number+".",15,Ui.WHITE,true);item.addView(num,new LinearLayout.LayoutParams(Ui.dp(this,40),-2));
            LinearLayout info=Ui.vertical(this);info.addView(Ui.text(this,blank?"Boş":correct?"Doğru":"Yanlış",14,blank?Ui.MUTED:correct?Ui.SUCCESS:Ui.DANGER,true));info.addView(Ui.text(this,"Sen: "+(blank?"—":mine)+"   •   Anahtar: "+k.correct,12,Ui.MUTED,false),Ui.lp(this,-1,-2,5));item.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            if(!correct){TextView solve=Ui.button(this,"✦ Çöz",false);item.addView(solve);solve.setOnClickListener(v->startActivity(new Intent(this,SolveActivity.class).putExtra("test_id",test.id).putExtra("question_no",number).putExtra("correct",k.correct)));}
            body.addView(item,Ui.lp(this,-1,-2,9));
        }
        TextView done=Ui.button(this,"ANA EKRANA DÖN",true);body.addView(done,Ui.lp(this,-1,-2,25));done.setOnClickListener(v->{Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);finish();});Ui.spacer(this,body,30);
    }
    private void score(LinearLayout row,String number,String name,int color){LinearLayout card=Ui.card(this);card.addView(Ui.text(this,number,26,color,true));card.addView(Ui.text(this,name,12,Ui.MUTED,false),Ui.lp(this,-1,-2,6));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.rightMargin=Ui.dp(this,6);row.addView(card,p);}
}
