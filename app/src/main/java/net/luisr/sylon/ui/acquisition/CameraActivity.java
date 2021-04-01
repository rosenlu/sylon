package net.luisr.sylon.ui.acquisition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;

import net.luisr.sylon.R;
import net.luisr.sylon.ui.doc.PageListActivity;
import net.luisr.sylon.ui.main.DocumentListFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for taking photos. No cropping or other editing is done.
 * Typically launched from a {@link PageListActivity}. If the activity finishes because a photo was
 * taken, the result is an {@link Intent} with code {@link android.app.Activity#RESULT_OK} and
 * the extra {@link CameraActivity#INTENT_EXTRA_IMAGE_URI} containing the photo URI.
 * @see PageListActivity
 */
public class CameraActivity extends AppCompatActivity {
    public static String CAMERA_FRAGMENT_ID = "net.luisr.sylon.camera_fragment_id";
    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri";

    /** The fragment transaction used to replace the current fragment with another one. */
    private FragmentTransaction fragmentTransaction;

    public CameraActivity() {
        super(R.layout.activity_camera);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // start fragment transaction with default fragment (CameraFragment)
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.replace(R.id.fragment_container_view, CameraFragment.class, null, CAMERA_FRAGMENT_ID);
            fragmentTransaction.commit();
        }
    }
}