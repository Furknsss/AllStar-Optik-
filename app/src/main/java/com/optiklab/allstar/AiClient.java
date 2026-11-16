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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Gemini response with 503/429 retry and free-tier-safe model fallback. */
public final class AiClient {
    private AiClient() {}

    public static final class SolutionResult {
        public final String text;
        public final String model;
        public final boolean usedFallback;
        public SolutionResult(String text, String model, boolean usedFallback) {
            this.text = text;
            this.model = model;
            this.usedFallback = usedFallback;
        }
    }

    private static final class ApiError extends Exception {
        final int status;
        final String apiMessage;
        ApiError(int status, String message) {
            super(message);
            this.status = status;
            this.apiMessage = message;
        }
    }

    private static String read(InputStream input) throws Exception {
        if (input == null) return "";
        try (InputStream in = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[4096];
            int size;
            while ((size = in.read(bytes)) != -1) output.write(bytes, 0, size);
            return output.toString("UTF-8");
        }
    }

    /** Paid models are NEVER tried implicitly; user must pick one explicitly. */
    private static List<String> models(String preferred) {
        LinkedHashSet<String> order = new LinkedHashSet<>();
        if (preferred != null && !preferred.trim().isEmpty() && !preferred.equals("auto")) {
            order.add(preferred.trim());
        } else {
            order.add("gemini-4-hydrogen");
        }
        order.add("gemini-3.8-flash");
        order.add("gemini-3.5-flash-lite");
        if (!"auto".equals(preferred) && "gemini-3.8-flash".equals(preferred))
            order.add("gemini-4-hydrogen");
        return new ArrayList<>(order);
    }

    public static SolutionResult solve(String apiKey, String preferred, Bitmap bitmap,
            String subject, String topic, int question, String correct) throws Exception {
        if (apiKey == null || apiKey.trim().isEmpty()) throw new IllegalStateException("Gemini API anahtarı gerekli.");
        if (bitmap == null) throw new IllegalStateException("Önce sorunun fotoğrafını ekle.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        String encoded = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        String prompt =
            "Sen YKS'de uzman, konuyu anlaşılır öğreten bir matematik/geometri öğretmenisin. Öğrenci yalnızca cevabı değil, "
          + "NASIL DÜŞÜNMESİ GEREKTİĞİNİ öğrenmek istiyor. Bu fotoğrafta " + subject + " / " + topic
          + " / " + question + ". soruyu analiz et. "
          + "Görseldeki soru kökünü, şeklin üzerindeki harfleri, ölçüleri ve ne sorulduğunu çok dikkatli oku. "
          + "Şeklin görünüşüne bakarak ölçü veya diklik varsayma, yazmayan bir bilgiyi uydurma. "
          + "Fotoğrafta birden fazla soru varsa yalnızca " + question + ". soruyu çöz. "
          + "Şekil/sayı okunmuyorsa çözüm uydurmak yerine hangi veriyi tekrar çekmesi gerektiğini açıkça söyle. "
          + "Yanıtı TÜRKÇE, sakin, öğrencinin anlayacağı şekilde anlat. Şu düzeni mutlaka uygula:\n"
          + "## Soru bizden ne istiyor?\nSoruyu 1-2 cümlede doğru özetle.\n"
          + "## Çözüm fikri\n**Nasıl fark ederiz?** Soru türünü tanıtan ipucunu ve yöntem seçiminin gerekçesini anlat.\n"
          + "## 1. Adım — ...\n**Ne yapıyoruz?** Somut işlem/çizim/denklem.\n**Neden?** Bu adımın mantığı.\n"
          + "## 2. Adım — ...\nGerekli sonraki adımlar için aynı yaklaşım; işlem atlama.\n"
          + "## ÖSYM taktiği\nBu soruya özel, daha hızlı çözme veya önemli bir tuzaktan kaçınma taktiği ver. "
          + "**En sık hata:** Öğrencinin bu soru türünde düşebileceği bir yanlışı açıkla. Genel geçer, boş öğüt yazma.\n"
          + "## Sonuç ve cevap kontrolü\nHesapladığın sonucu, doğru seçeneği ve kısa tutarlılık kontrolünü göster. "
          + "Cevap anahtarı " + correct + " diyor. Çözümü bu seçeneğe zorlamadan bağımsız çöz; "
          + "uyuşmazlık varsa açıkça belirt.\n"
          + "Formülleri görsel olarak güzel göstermek için satır içi \\( ... \\) ve ayrı satır \\[ ... \\] "
          + "LaTeX gösterimini kullan. Karmaşık ifade yerine doğru, sade ve açık denklemler seç. "
          + "Sadece Markdown başlıkları ve açıklayıcı paragraflar kullan; ham HTML ve kod bloğu yazma. "
          + "Aşırı uzatma, ama öğrenci hiçbir adımda 'neden?' sorusuyla kalmasın.";
        JSONArray parts = new JSONArray()
            .put(new JSONObject().put("text", prompt))
            .put(new JSONObject().put("inline_data", new JSONObject()
                .put("mime_type", "image/jpeg").put("data", encoded)));
        JSONObject body = new JSONObject()
            .put("contents", new JSONArray().put(new JSONObject().put("parts", parts)))
            .put("generationConfig", new JSONObject().put("temperature", 0.15));
        String payload = body.toString();

        List<String> choices = models(preferred == null ? "auto" : preferred);
        ApiError lastApi = null;
        Exception lastNetwork = null;
        for (int i = 0; i < choices.size(); i++) {
            String current = choices.get(i);
            int maxAttempts = i == 0 ? 2 : 1;
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Çözüm iptal edildi");
                try {
                    String text = request(apiKey, current, payload);
                    return new SolutionResult(text, current, i > 0);
                } catch (ApiError e) {
                    lastApi = e;
                    if (e.status == 401 || e.status == 400) {
                        throw new IllegalStateException("Gemini isteği reddetti (" + e.status + "): " + shortMessage(e.apiMessage));
                    }
                    // 403: paid/unauthorized model; 404: model not available.
                    // 429/500/502/503/504: temporary limit or service outage.
                    if (e.status == 403 || e.status == 404) break;
                    if (e.status == 429 || e.status == 500 || e.status == 502 ||
                        e.status == 503 || e.status == 504) {
                        if (attempt + 1 < maxAttempts) {
                            Thread.sleep(1000L * (attempt + 1));
                            continue;
                        }
                        break;
                    }
                    throw new IllegalStateException("Gemini API hatası (" + e.status + "): " + shortMessage(e.apiMessage));
                } catch (java.io.IOException e) {
                    lastNetwork = e;
                    if (attempt + 1 < maxAttempts) {
                        Thread.sleep(1000L);
                        continue;
                    }
                    break;
                }
            }
        }

        if (lastApi != null) {
            if (lastApi.status == 429)
                throw new IllegalStateException("Gemini kullanım sınırına ulaştı. Kota durumunu Google AI Studio'dan kontrol et.");
            if (lastApi.status == 403 || lastApi.status == 404)
                throw new IllegalStateException("Bu API anahtarı için seçilen/yedek modeller kullanılabilir değil. Ayarlardan Gemini 3.8 Flash seçebilir veya AI Studio erişimini kontrol edebilirsin.");
            if (lastApi.status == 503 || lastApi.status == 500 || lastApi.status == 502 || lastApi.status == 504)
                throw new IllegalStateException("Gemini sunucuları şu anda yoğun. Yedek modeller de yanıt vermedi; biraz sonra tekrar dene. Fotoğrafın kaydedildi.");
            throw new IllegalStateException("Gemini API hatası (" + lastApi.status + "): " + shortMessage(lastApi.apiMessage));
        }
        throw new IllegalStateException("Gemini bağlantısı kurulamadı. İnternetini kontrol et. "
            + (lastNetwork == null ? "" : shortMessage(lastNetwork.getMessage())));
    }

    private static String shortMessage(String s) {
        if (s == null) return "";
        return s.substring(0, Math.min(s.length(), 190));
    }

    private static String request(String apiKey, String model, String payload) throws Exception {
        if (!model.matches("[a-zA-Z0-9._-]{3,80}")) throw new ApiError(400, "Geçersiz model kimliği");
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent");
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(25000);
        c.setReadTimeout(90000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        c.setRequestProperty("x-goog-api-key", apiKey);
        try {
            try (OutputStream stream = c.getOutputStream()) {
                stream.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            int status = c.getResponseCode();
            String response = read(status >= 200 && status < 300 ? c.getInputStream() : c.getErrorStream());
            if (status < 200 || status >= 300) {
                String message = response;
                try { message = new JSONObject(response).getJSONObject("error").optString("message", response); }
                catch (Exception ignored) {}
                throw new ApiError(status, shortMessage(message));
            }
            JSONObject json = new JSONObject(response);
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0)
                throw new IllegalStateException("Gemini çözüm döndürmedi. Aynı fotoğrafla tekrar dene.");
            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
            JSONArray textParts = content == null ? null : content.optJSONArray("parts");
            if (textParts == null) throw new IllegalStateException("Modelin yanıtında çözüm metni yok.");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < textParts.length(); i++) {
                String part = textParts.getJSONObject(i).optString("text", "");
                if (!part.isEmpty()) result.append(part).append("\n");
            }
            if (result.length() == 0) throw new IllegalStateException("Model boş yanıt verdi. Tekrar dene.");
            return result.toString().trim();
        } finally {
            c.disconnect();
        }
    }
}
