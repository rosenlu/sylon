package net.luisr.sylon.ui.acquisition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import net.luisr.sylon.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri";

    private static final String TAG = "Sylon";

    private static final int REQUEST_CODE_CAMERA = 10;

    private final String FILENAME_FORMAT = "yyyy-MM-dd_HHmmss-SSS";

    private ImageCapture imageCapture = null;

    private File imgDirectory;
    private File pdfDirectory;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CODE_CAMERA);
        }

        findViewById(R.id.camera_capture_button).setOnClickListener(v -> takePhoto());

        File outputDirectory = getOutputDirectory();
        imgDirectory = new File(outputDirectory, "img");
        pdfDirectory = new File(outputDirectory, "pdf");
        if (!imgDirectory.mkdirs() || !imgDirectory.exists()) {
            Log.w(TAG, "Could not create img subdir.");
        }
        if (!pdfDirectory.mkdirs() || !pdfDirectory.exists()) {
            Log.w(TAG, "Could not create pdf subdir.");
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private File getOutputDirectory() {
        File[] externalMediaDirs = getExternalMediaDirs();
        File filesDir = getFilesDir();
        if (externalMediaDirs.length != 0) {
            File firstExtDir = externalMediaDirs[0];
            if (firstExtDir != null && firstExtDir.exists()) {
                File subDir = new File(firstExtDir, getString(R.string.app_name));
                if (subDir.mkdirs() || subDir.exists()) {
                    return subDir;
                }
            }
        }
        return filesDir;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePhoto() {
        File outputFile = new File(imgDirectory, new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                String savedUri = Uri.fromFile(outputFile).toString();
                String msg = "Image " + savedUri + " saved successfully!";
                Log.d(TAG, msg);

                Intent intent = new Intent();
                intent.putExtra(INTENT_EXTRA_IMAGE_URI, savedUri);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
//                Toast.makeText(MainActivity.this, "Image " + outputFile.toString() + " could not be saved", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Image could not be saved: " + exception.getMessage());
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                PreviewView previewView = findViewById(R.id.viewFinder);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                //cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}