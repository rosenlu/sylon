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
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import net.luisr.sylon.R;
import net.luisr.sylon.ui.doc.DocumentActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for taking photos. No cropping or other editing is done.
 * Typically launched from a {@link DocumentActivity}. If the activity finishes because a photo was
 * taken, the result is an {@link Intent} with code {@link android.app.Activity#RESULT_OK} and
 * the extra {@link CameraActivity#INTENT_EXTRA_IMAGE_URI} containing the photo URI.
 * @see DocumentActivity
 */
public class CameraActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri";

    /** Permissions request code for camera access */
    private static final int REQUEST_CODE_CAMERA = 10;

    /** Format of photo file names */
    private final String FILENAME_FORMAT = "yyyy-MM-dd_HHmmss-SSS";

    /** Instance for takiing a photo */
    private ImageCapture imageCapture = null;

    /** Photo directory */
    private File imgDirectory;

    /** Executor for {@link ImageCapture#takePicture(ImageCapture.OutputFileOptions, Executor, ImageCapture.OnImageSavedCallback)} */
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // set up capture
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        // check for permission and request if necessary
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CODE_CAMERA);
        }

        // bind snapshot button
        findViewById(R.id.camera_capture_button).setOnClickListener(v -> takePhoto());

        // check for image directory and create if necessary
        File outputDirectory = getOutputDirectory();
        imgDirectory = new File(outputDirectory, "img");
        if (!imgDirectory.mkdirs() || !imgDirectory.exists()) {
            // the directory does not exist and we could not create it
            System.out.println("Could not create img subdir.");
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieve either the first external media directory from
     * {@link ContextWrapper#getExternalMediaDirs()} if it is valid or the "files directory" from
     * {@link ContextWrapper#getFilesDir()}.
     * @return the resolved output directory
     */
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
                // Start preview on the UI
                startCamera();
            } else {
                // User just declined permission(s)
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Take a photo, saving it to {@link CameraActivity#imgDirectory}. If successful, finish the
     * {@link CameraActivity} with the extra {@link CameraActivity#INTENT_EXTRA_IMAGE_URI} set and
     * {@link android.app.Activity#RESULT_OK} as the result value.
     */
    private void takePhoto() {
        // Format the time and create a File within the image dir
        File outputFile = new File(imgDirectory, new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        // Options object
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        // Take picture with the executor
        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Retrieve saved location
                String savedUri = Uri.fromFile(outputFile).toString();
                String msg = "Image " + savedUri + " saved successfully!";
                System.out.println(msg);

                Intent intent = new Intent();
                intent.putExtra(INTENT_EXTRA_IMAGE_URI, savedUri);
                setResult(RESULT_OK, intent);
                // End activity
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
//                Toast.makeText(MainActivity.this, "Image " + outputFile.toString() + " could not be saved", Toast.LENGTH_SHORT).show();
                System.out.println("Image could not be saved: " + exception.getMessage());
            }
        });
    }

    /**
     * Starts the preview inside the layout's {@link PreviewView}.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // make new preview
                Preview preview = new Preview.Builder().build();

                // select the back camera
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                // get our view
                PreviewView previewView = findViewById(R.id.viewFinder);
                // set the surface provider to the UI's surface provider
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                //cameraProvider.unbindAll();

                // finally use the provider to bind our selected camera to this object, the capture object and the UI preview
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Checks if the activity has the required permissions
     * @return true if the user previously granted all permissions
     */
    private boolean allPermissionsGranted() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}