package com.optiklab.allstar;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class SolveActivity extends Activity {
    static final int CAMERA=31, GALLERY=32, CAMERA_PERMISSION=7011;
    Store store;BookDb.Test test;int question;String answer;Uri captureUri;String imagePath;Bitmap photo;
    TextView status,solution,solveButton;ImageView preview;
    @Override public void onCreate(Bundle b){super.onCreate(b);store=new Store(this);String id=getIntent().getStringExtra("test_id");test=new BookDb(this).test(id);question=getIntent().getIntExtra("question_no",1);answer=getIntent().getStringExtra("correct");if(test==null){finish();return;}if(answer==null)answer="Bilinmiyor";show();String[] previous=store.getSolution(test.id,question);if(previous[0]!=null){loadFromFile(previous[0]);}if(previous[1]!=null&&!previous[1].isBlank()){solution.setText(previous[1]);status.setText("Kaydedilmiş çözüm");solveButton.setText("YENİDEN ÇÖZ");}}
    private void show(){LinearLayout root=Ui.base(this);Ui.header(this,root,question+". Soru",test.summary());ScrollView scroll=Ui.scrolling(this,root);LinearLayout content=Ui.vertical(this);scroll.addView(content);
        content.addView(Ui.text(this,"Doğrudan tam çözüm",22,Ui.WHITE,true));Ui.info(this,content,"Sorunun tamamını çek. Yapay zekâ adım adım çözer ve cevap anahtarıyla karşılaştırır.");Ui.spacer(this,content,18);
        LinearLayout photoCard=Ui.card(this);preview=new ImageView(this);preview.setScaleType(ImageView.ScaleType.FIT_CENTER);preview.setBackground(Ui.bg(this,Ui.PANEL2,14));photoCard.addView(preview,new LinearLayout.LayoutParams(-1,Ui.dp(this,200)));content.addView(photoCard);
        LinearLayout actions=Ui.horizontal(this);TextView camera=Ui.button(this,"⌾  Kamera",false);TextView gallery=Ui.button(this,"▧  Galeri",false);LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(0,-2,1);a.rightMargin=Ui.dp(this,8);actions.addView(camera,a);actions.addView(gallery,new LinearLayout.LayoutParams(0,-2,1));camera.setOnClickListener(v->openCamera());gallery.setOnClickListener(v->openGallery());content.addView(actions,Ui.lp(this,-1,-2,12));
        solveButton=Ui.button(this,"ADIM ADIM ÇÖZ  ✦",true);solveButton.setOnClickListener(v->startSolve());content.addView(solveButton,Ui.lp(this,-1,-2,18));
        status=Ui.text(this,"Fotoğraf eklemen gerekiyor.",13,Ui.MUTED,false);content.addView(status,Ui.lp(this,-1,-2,16));Ui.spacer(this,content,20);
        solution=Ui.text(this,"",15,Ui.WHITE,false);solution.setLineSpacing(Ui.dp(this,5),1.1f);content.addView(solution);Ui.spacer(this,content,40);
    }
    private void openGallery(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,GALLERY);}
    private void openCamera(){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);return;
        }
        launchCamera();
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==CAMERA_PERMISSION){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)launchCamera();
            else new android.app.AlertDialog.Builder(this).setTitle("Kamera izni verilmedi")
                .setMessage("Soru fotoğrafı çekmek için izin gerekiyor. Dilersen galeriden görsel seçebilirsin.")
                .setPositiveButton("Galeriden Seç",(d,w)->openGallery()).setNegativeButton("Kapat",(d,w)->{}).show();
        }
    }
    private void launchCamera(){try{ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,"Optik_soru_"+System.currentTimeMillis()+".jpg");values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");if(android.os.Build.VERSION.SDK_INT>=29)values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Optik");captureUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);if(captureUri==null)throw new Exception("Fotoğraf dosyası oluşturulamadı");Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);intent.putExtra(MediaStore.EXTRA_OUTPUT,captureUri);intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);startActivityForResult(intent,CAMERA);}catch(Exception e){Toast.makeText(this,"Kamera açılamadı: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK){if(req==CAMERA&&captureUri!=null)getContentResolver().delete(captureUri,null,null);return;}Uri u=req==CAMERA?captureUri:(data==null?null:data.getData());if(u==null)return;try{
            Bitmap bitmap=decode(u);if(bitmap==null)throw new IllegalStateException("Fotoğraf okunamadı");File dir=new File(getFilesDir(),"questions");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Depolama açılamadı");File f=new File(dir,test.id.replaceAll("[^a-zA-Z0-9_-]","_")+"_"+question+".jpg");try(FileOutputStream out=new FileOutputStream(f)){bitmap.compress(Bitmap.CompressFormat.JPEG,90,out);}photo=bitmap;imagePath=f.getAbsolutePath();store.savePhoto(test.id,question,imagePath);preview.setImageBitmap(photo);status.setText("Fotoğraf hazır · çözüm oluşturuluyor");if(req==CAMERA&&captureUri!=null){getContentResolver().delete(captureUri,null,null);captureUri=null;}startSolve();
        }catch(Exception e){status.setText("Fotoğraf işlenemedi: "+e.getMessage());}}
    private Bitmap decode(Uri u) throws Exception {
        BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;
        try(InputStream input=getContentResolver().openInputStream(u)){BitmapFactory.decodeStream(input,null,opts);}
        int max=1900, sample=1;while(Math.max(opts.outWidth,opts.outHeight)/sample>max)sample*=2;opts.inJustDecodeBounds=false;opts.inSampleSize=Math.max(1,sample);Bitmap result;
        try(InputStream input=getContentResolver().openInputStream(u)){result=BitmapFactory.decodeStream(input,null,opts);}if(result==null)throw new IllegalStateException("Boş resim");
        int degrees=0;try(InputStream input=getContentResolver().openInputStream(u)){ExifInterface exif=new ExifInterface(input);int o=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);if(o==ExifInterface.ORIENTATION_ROTATE_90)degrees=90;else if(o==ExifInterface.ORIENTATION_ROTATE_180)degrees=180;else if(o==ExifInterface.ORIENTATION_ROTATE_270)degrees=270;}catch(Exception ignore){}
        if(degrees!=0){Matrix m=new Matrix();m.postRotate(degrees);result=Bitmap.createBitmap(result,0,0,result.getWidth(),result.getHeight(),m,true);}
        int w=result.getWidth(),h=result.getHeight();if(Math.max(w,h)>max){float s=(float)max/Math.max(w,h);result=Bitmap.createScaledBitmap(result,Math.round(w*s),Math.round(h*s),true);}return result;
    }
    private void loadFromFile(String path){try(FileInputStream in=new FileInputStream(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;photo=BitmapFactory.decodeStream(in,null,options);if(photo!=null){imagePath=path;preview.setImageBitmap(photo);status.setText("Kaydedilmiş fotoğraf");}}catch(Exception ignored){}}
    private void startSolve(){if(photo==null){status.setText("Önce sorunun fotoğrafını çek veya galeriden seç.");return;}String key=SecureKey.get(this);if(key.trim().isEmpty()){
            status.setText("Çözüm için Gemini API anahtarını ayarlardan gir.");new android.app.AlertDialog.Builder(this).setTitle("Yapay zekâ bağlantısı").setMessage("Gemini API anahtarı henüz eklenmemiş. Optik kontrolü internetsiz çalışır, yapay zekâ çözümü için API erişimi gerekir.").setPositiveButton("Ayarları aç",(d,w)->startActivity(new Intent(this,SettingsActivity.class))).setNegativeButton("Daha sonra",(d,w)->{}).show();return;
        }
        solveButton.setEnabled(false);solveButton.setAlpha(.5f);status.setText("Soru analiz ediliyor...");solution.setText("");Bitmap toSend=photo;String model=SecureKey.model(this);
        Executors.newSingleThreadExecutor().execute(()->{
            try{String answerText=AiClient.solve(key,model,toSend,test.subject,test.topic==null?"":test.topic,question,answer);store.saveSolution(test.id,question,answerText);runOnUiThread(()->{solveButton.setEnabled(true);solveButton.setAlpha(1f);solveButton.setText("YENİDEN ÇÖZ");status.setText("Tam çözüm hazır · cihazına kaydedildi");solution.setText(answerText);});}
            catch(Exception e){runOnUiThread(()->{solveButton.setEnabled(true);solveButton.setAlpha(1f);status.setText("Çözüm alınamadı: "+e.getMessage());});}
        });
    }
}
