# AllStar Optik — Android

Bağımsız kişisel Android optik cevap kontrol uygulaması; yayınevinin resmî ürünü değildir.

Kaynak Java dosyaları depoda düzenlenebilir şekilde tutulur. Cevap veritabanı `build/source_v03.zip.base64` içinde `app/src/main/assets/answers.sqlite` olarak saklanır; Android derleme iş akışı kaynak arşivini açar.

## İşlevler
- QR tarama / TYT-AYT, ders, kitap, konu ve test arama
- A-E optik, doğru/yanlış/boş/net, yerel geçmiş
- Yanlış sorunun fotoğrafını kamera/galeriden yükleme ve Gemini ile doğrudan tam çözüm (API anahtarı gerekir)
- Kamera izni kullanım anında; galeri için ekstra izin yoktur

## Veri sınırları
48 kullanıcı PDF'sinden otomatik çıkarılmış 2.565 test grubu, 22.681 cevap. 3 numaralandırma hatalı grup puanlanmaz; 219 belirsiz etiket ayrıca doğrulanmalıdır. Diğer cevapların tamamı elle doğrulanmadığından hata olasılığı vardır. QR eşleştirmeleri ilk seferde yapılır.

## Derleme
Actions → `AllStar Optik Android APK` → başarılı çalıştırmanın `AllStar-Optik-v0.3-debug-APK` artifact'i.

Not: GitHub Actions şu an GitHub tarafından runner atanamadan (0 işlem adımı) durduruluyor. Bu nedenle **henüz kurulabilir/test edilmiş APK yoktur**.
