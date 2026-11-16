package com.optiklab.allstar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import java.util.List;

/** Embedded QR scanner: uses the library's camera view instead of its external CaptureActivity. */
public final class QrScannerActivity extends Activity {
    public static final String EXTRA_QR_RESULT = "qr_result";
    private static final int PERMISSION_CAMERA = 7021;
    private static final String TAG = "AllStarQR";
    private DecoratedBarcodeView scanner;
    private boolean scanFinished = false;
    private boolean showingError = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            LinearLayout root = Ui.base(this);
            Ui.header(this, root, "QR kodunu okut", "Kitaptaki test QR kodunu kameraya göster");
            scanner = new DecoratedBarcodeView(this);
            scanner.setStatusText("QR kodunu çerçevenin içine yerleştir");
            root.addView(scanner, new LinearLayout.LayoutParams(-1, 0, 1));
            TextView fallback = Ui.button(this, "⌕  Kamerasız test ara", false);
            root.addView(fallback, Ui.lp(this, -1, -2, 12));
            fallback.setOnClickListener(v -> {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
            });
            scanner.decodeSingle(new BarcodeCallback() {
                @Override public void barcodeResult(BarcodeResult result) {
                    if (scanFinished || result == null || result.getText() == null) return;
                    String content = result.getText().trim();
                    if (content.isEmpty()) return;
                    scanFinished = true;
                    scanner.pause();
                    setResult(RESULT_OK, new Intent().putExtra(EXTRA_QR_RESULT, content));
                    finish();
                }
                @Override public void possibleResultPoints(List<ResultPoint> points) { }
            });
        } catch (RuntimeException e) {
            reportCameraError(e);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (scanFinished || showingError || scanner == null) return;
        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            resumeCamera();
        }
    }

    private void resumeCamera() {
        if (scanner == null || isFinishing() || scanFinished) return;
        try {
            scanner.resume();
        } catch (RuntimeException e) {
            reportCameraError(e);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                resumeCamera();
            else showFallback("Kamera izni verilmedi. Testi arayabilir veya uygulama ayarlarından kameraya izin verebilirsin.");
        }
    }

    private void reportCameraError(RuntimeException e) {
        Log.e(TAG, "QR camera init failure", e);
        showFallback("Kamera açılırken bir sorun oluştu. Testi ismiyle de açabilirsin.");
    }

    private void showFallback(String message) {
        if (showingError || isFinishing()) return;
        showingError = true;
        new AlertDialog.Builder(this)
            .setTitle("QR tarayıcı açılamadı")
            .setMessage(message)
            .setPositiveButton("Test Ara", (dialog, which) -> {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
            })
            .setNegativeButton("Geri Dön", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    @Override protected void onPause() {
        if (scanner != null) {
            try { scanner.pause(); } catch (RuntimeException e) {
                Log.w(TAG, "Failed to pause scanner", e);
            }
        }
        super.onPause();
    }

    @Override public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        return (scanner != null && scanner.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
    }
}
