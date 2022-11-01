package com.google.cardboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VrActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    static {
        System.loadLibrary("native_lib");
    }

    private static final String TAG = VrActivity.class.getSimpleName();

    // Permission request codes
    private static final int PERMISSIONS_REQUEST_CODE = 2;

    private GLSurfaceView glView;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        initializeNative(width, height);

        setContentView(R.layout.activity_vr);
        glView = findViewById(R.id.surface_view);
        glView.setEGLContextClientVersion(2);
        Renderer renderer = new Renderer();
        glView.setRenderer(renderer);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        setImmersiveSticky();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                (visibility) -> {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        setImmersiveSticky();
                    }
                });

        // Forces screen to max brightness.
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1.f;
        getWindow().setAttributes(layout);

        // Prevents screen from dimming/locking.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseNative();
        glView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (VERSION.SDK_INT < VERSION_CODES.Q && !isReadExternalStorageEnabled()) {
            final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);

            return;
        }

        glView.onResume();
        resumeNative();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyNative();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveSticky();
        }
    }

    private class Renderer implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            surfaceCreatedNative();
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            setScreenResolutionNative(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            renderNative();
        }
    }

    // Called from activity_vr.xml
    public void closeSample(View view) {
        Log.d(TAG, "Leaving VR sample");
        finish();
    }

    // Called from activity_vr.xml
    public void showSettings(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.settings_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.switch_viewer) {
            switchViewerNative();
            return true;
        }
        return false;
    }

    private boolean isReadExternalStorageEnabled() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!isReadExternalStorageEnabled()) {
            Toast.makeText(this, R.string.read_storage_permission, Toast.LENGTH_LONG).show();
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Permission denied with checking "Do not ask again". Note that in Android R "Do not ask
                // again" is not available anymore.
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            }
            finish();
        }
    }

    private void setImmersiveSticky() {
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private native void initializeNative(int screenWidth, int screenHeight);

    private native void destroyNative();

    private native void resumeNative();

    private native void pauseNative();

    private native void surfaceCreatedNative();

    private native void setScreenResolutionNative(int width, int height);

    private native void renderNative();

    private native void switchViewerNative();
}
