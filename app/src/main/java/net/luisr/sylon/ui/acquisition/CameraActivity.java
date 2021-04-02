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
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
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
    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.extra_image_uri";

    /** The fragment manager used to begin the fragment transaction and listen for results of the {@link CameraFragment}. */
    private FragmentManager fragmentManager;

    public CameraActivity() {
        super(R.layout.activity_camera);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();

        // start fragment transaction with default fragment (CameraFragment)
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.replace(R.id.fragment_container_view, CameraFragment.class, null, CAMERA_FRAGMENT_ID);
            fragmentTransaction.commit();
        }

        listenForFragmentResult();
        // set fragment result listener to receive data from the camera fragment

    }

    /**
     * Listen for a result of the {@link CameraFragment}. If a result is received, set the result
     * of this activity accordingly and finish.
     */
    private void listenForFragmentResult() {
        fragmentManager.setFragmentResultListener(CameraFragment.REQUEST_KEY_CAMERA_FRAGMENT, this, (requestKey, result) -> {
            // get uri from the bundle
            String savedUri = result.getString(CameraFragment.BUNDLE_KEY_IMAGE_URI);

            if (savedUri.isEmpty()) {  // something went wrong (usually camera permissions denied)
                // show Toast message and set result to canceled
                Toast.makeText(CameraActivity.this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
            } else {  // everything is fine
                // create an intent and set result
                Intent intent = new Intent();
                intent.putExtra(CameraActivity.INTENT_EXTRA_IMAGE_URI, savedUri);
                setResult(Activity.RESULT_OK, intent);
            }

            // End activity
            finish();
        });
    }
}