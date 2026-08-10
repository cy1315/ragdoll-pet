package com.ragdollpet;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.*;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FloatSvc extends Service {
    private WindowManager wm;
    private WebView wv;
    private WindowManager.LayoutParams lp;
    private Handler h = new Handler(Looper.getMainLooper());
    private long lastTouch;
    private int tapN;
    private long tapT;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        wv = new WebView(this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setBackgroundColor(0x00000000);
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl("file:///android_asset/cat.html");

        Point sz = new Point();
        wm.getDefaultDisplay().getSize(sz);
        int s = (int)(Math.min(sz.x, sz.y) * 0.55);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        lp = new WindowManager.LayoutParams(s, s, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = (sz.x - s) / 2;
        lp.y = sz.y / 4;

        wm.addView(wv, lp);
        setupTouch();
        startFg();
    }

    private void setupTouch() {
        final float[] dragX = {0}, dragY = {0}, startX = {0}, startY = {0};
        final boolean[] dragging = {false};
        final Runnable[] lpR = {null};

        wv.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dragX[0] = e.getRawX() - lp.x;
                        dragY[0] = e.getRawY() - lp.y;
                        startX[0] = e.getRawX();
                        startY[0] = e.getRawY();
                        dragging[0] = false;
                        lpR[0] = new Runnable() { public void run() {
                            wv.evaluateJavascript("javascript:showSleepy()", null);
                        }};
                        h.postDelayed(lpR[0], 600);
                        lastTouch = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(e.getRawX() - startX[0]) > 10 ||
                            Math.abs(e.getRawY() - startY[0]) > 10) {
                            dragging[0] = true;
                            h.removeCallbacks(lpR[0]);
                        }
                        if (dragging[0]) {
                            lp.x = (int)(e.getRawX() - dragX[0]);
                            lp.y = (int)(e.getRawY() - dragY[0]);
                            wm.updateViewLayout(wv, lp);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        h.removeCallbacks(lpR[0]);
                        float dx = e.getRawX() - startX[0];
                        float dy = e.getRawY() - startY[0];
                        if (Math.abs(dx) > 60 || Math.abs(dy) > 60) {
                            fling(dx, dy);
                        } else if (!dragging[0]) {
                            onTap();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void fling(float dx, float dy) {
        Point sz = new Point();
        wm.getDefaultDisplay().getSize(sz);
        lp.x = Math.max(0, Math.min(sz.x - lp.width, (int)(lp.x + dx * 1.5)));
        lp.y = Math.max(0, Math.min(sz.y - lp.height, (int)(lp.y + dy * 1.5)));
        wm.updateViewLayout(wv, lp);
        h.postDelayed(new Runnable() { public void run() {
            Point s = new Point();
            wm.getDefaultDisplay().getSize(s);
            lp.x = (s.x - lp.width) / 2;
            lp.y = s.y / 4;
            wm.updateViewLayout(wv, lp);
        }}, 800);
    }

    private void onTap() {
        lastTouch = System.currentTimeMillis();
        tapN++;
        long now = System.currentTimeMillis();
        if (now - tapT > 400) tapN = 1;
        tapT = now;

        if (tapN == 1) wv.evaluateJavascript("javascript:showBubble('喵~')", null);
        else if (tapN == 2) wv.evaluateJavascript("javascript:showLove()", null);
        else if (tapN == 3) wv.evaluateJavascript("javascript:showExcited()", null);
        else if (tapN >= 5) wv.evaluateJavascript("javascript:showAnnoyed()", null);
    }

    private void startFg() {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, MainAct.class), PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(this,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? PetApp.CHANNEL_ID : "")
            .setContentTitle("布偶猫桌宠")
            .setContentText("小布偶在你屏幕上~")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pi)
            .setOngoing(true)
            .build();
        startForeground(1, n);
    }

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        wm.removeView(wv);
        super.onDestroy();
    }
}