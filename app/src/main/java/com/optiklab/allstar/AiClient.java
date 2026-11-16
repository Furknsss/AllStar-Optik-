package com.optiklab.allstar;

import android.graphics.Bitmap;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AiClient {
    private static String read(InputStream in) throws Exception {if(in==null)return "";try(InputStream stream=in;ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=stream.read(b))!=-1)o.write(b,0,n);return o.toString("UTF-8");}}
    public static String solve(String apiKey,String model,Bitmap bitmap,String subject,String topic,int question,String correct) throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG,85,out);String encoded=Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP);
        String prompt="Sen deneyimli bir YKS öğretmenisin. Bu soru fotoğrafından "+subject+" / "+topic+" / "+question+". soruyu çöz. SORUYA DAİR TAM, DOĞRUDAN ve ADIM ADIM çözüm yaz. Önce görseldeki verilenleri özetle; sonra işlem adımlarını açıkla, atlama yapma; gerektiğinde formülü gerekçelendir. İpucu aşaması yapma. Türkçe yaz, matematikte okunabilir metin ve Unicode sembolleri kullan. Resimde birden fazla soru varsa yalnızca "+question+" numaralı soruyu çözmeye çalış; numara tespit edilemiyorsa hangi soruyu çözdüğünü açıkça söyle. Cevap anahtarındaki seçenek: "+correct+". Bu doğru cevabı ulaşmak için sonucu zorlamadan bağımsız çöz, en sonunda cevap anahtarıyla karşılaştır. Uyuşmuyorsa açıkça UYUŞMAZLIK: diye belirt. Görsel eksik veya belirsizse uydurma, yeniden fotoğraf iste.";
        JSONObject body=new JSONObject();JSONArray parts=new JSONArray().put(new JSONObject().put("text",prompt)).put(new JSONObject().put("inline_data",new JSONObject().put("mime_type","image/jpeg").put("data",encoded)));
        body.put("contents",new JSONArray().put(new JSONObject().put("parts",parts)));body.put("generationConfig",new JSONObject().put("temperature",0.15));
        URL url=new URL("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent");HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(30000);c.setReadTimeout(90000);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");c.setRequestProperty("x-goog-api-key",apiKey);
        try {try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes(StandardCharsets.UTF_8));}int status=c.getResponseCode();String response=read(status>=200&&status<300?c.getInputStream():c.getErrorStream());if(status<200||status>=300){String message=response;try{message=new JSONObject(response).getJSONObject("error").optString("message",response);}catch(Exception ignored){}throw new IllegalStateException("API hatası ("+status+"): "+message.substring(0,Math.min(message.length(),350)));}
            JSONObject json=new JSONObject(response);JSONArray candidates=json.optJSONArray("candidates");if(candidates==null||candidates.length()==0)throw new IllegalStateException("Model çözüm döndürmedi.");JSONArray textParts=candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts");StringBuilder result=new StringBuilder();for(int i=0;i<textParts.length();i++){String value=textParts.getJSONObject(i).optString("text","");if(!value.isEmpty())result.append(value).append("\n");}if(result.length()==0)throw new IllegalStateException("Yanıt boş.");return result.toString().trim();
        }finally{c.disconnect();}
    }
}
