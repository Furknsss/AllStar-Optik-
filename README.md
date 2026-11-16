# AllStar Optik v0.4

Bu proje 345'in resmi ürünü değildir. Kişisel Android optik kontrol uygulamasıdır.

## Kullanım
QR okut ya da TYT/AYT, ders, kitap, konu ve test ara; şık işaretle; doğru-yanlış-boş-net sonuçlarını ve cihazdaki geçmişi gör. Yanlış soruyu kamerayla çekip, kullanıcı tarafından girilen Gemini API anahtarıyla tam çözüm al.

## Veri doğrulaması
Kullanıcının sağladığı **48 PDF / 460 sayfa** kaynak olarak kullanılmıştır. PDF cevaplarından algılanan 2.565 grup içinde aynı sayfada aynı test ve cevapları tekrar eden bir grup tekilleştirildi. Sonuç: **2.564 farklı grup, 22.673 cevap**, 219 görsel etiketi PDF başlık logo görsellerinden ayrıştırıldı, 781 sayısal başlık resmi görsellerle türüne göre düzeltildi, **boş etiket 0**. 2.561 grubun soru numaraları 1..N ardışık ve geçerli A-E şıkları içeriyor.

**Üç basılı cevap anahtarı hatası** (AYT Mantık 4-C'de 10. cevap yok; TYT-AYT Tarih 5. kitap Günlük 3-B ve ÖSYM 6'da basılı 12 numarası yinelenmiş) PDF'de gerçek hata olduğundan tahmin edilmedi, yanlış sonuç oluşturmamak için uygulamada arama dışına alındı. Bunların **tam doğruluğu için yayınevinin düzeltilmiş anahtarı veya ilgili kitabın içeriği gerekir**. Bu nedenle tüm yayınevi cevaplarının tartışmasız doğru olduğu iddia edilmez. Android gerçek cihaz testi de ayrıca gereklidir.

## Derleme
GitHub Actions: `AllStar Optik Android APK` → başarılı koşu → `AllStar-Optik-v0.4-debug-APK` artifact'i. İş akışı `build/source_v04.zip.base64` arşivini açarak uygulamayı derler.

`CAMERA` izni QR okutma/fotoğraf çekme sırasında istenir; galeri Android'in belge seçicisini kullanır. `INTERNET` normal Android iznidir.
