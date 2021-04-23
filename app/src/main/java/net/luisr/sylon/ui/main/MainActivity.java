package net.luisr.sylon.ui.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.luisr.sylon.R;
import net.luisr.sylon.ui.acquisition.CameraFragment;

/**
 * The Main {@link android.app.Activity} of the app.
 * This activity is the launcher activity and shows a list of all {@link net.luisr.sylon.db.Document}s
 * in the {@link net.luisr.sylon.db.AppDatabase}.
 * Via a button, the user can add a Document to the AppDatabase.
 */
public class MainActivity extends AppCompatActivity {

    public static String DOCUMENT_LIST_FRAGMENT_ID = "net.luisr.sylon.document_list_fragment_id";
    public static String CAMERA_FRAGMENT_ID = "net.luisr.sylon.camera_fragment_id";

    /** The bottom navigation bar of the layout. */
    private BottomNavigationView bottomNavigationView;

    /** The {@link FragmentManager} managing the fragment of the fragment_container_view. */
    private FragmentManager fragmentManager;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get UI elements
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // start fragment transaction with default fragment (DocumentListFragment)
        if (savedInstanceState == null) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, DocumentListFragment.class, null, DOCUMENT_LIST_FRAGMENT_ID).commit();
        }

        // listen for clicks on the bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelectedCallback);
    }

    /**
     * The Callback function when a navigation item is selected in the {@link #bottomNavigationView}.
     * Starts a new {@link FragmentTransaction} to replace the fragment in the fragment container
     * accordingly.
     * @param item the {@link MenuItem} that was clicked.
     * @return boolean, weather the click was handled correctly.
     */
    private boolean onNavigationItemSelectedCallback(MenuItem item) {
        // get the id of the selected item
        int itemId = item.getItemId();

        // check which item was clicked and replace the fragment accordingly
        if (itemId == R.id.itemDocuments) {
            CameraFragment cameraFragment = (CameraFragment) fragmentManager.findFragmentByTag(CAMERA_FRAGMENT_ID);
            if (cameraFragment != null) {
                fragmentManager.beginTransaction().hide(cameraFragment).commit();
            }
            return true;
        } else if (itemId == R.id.itemCamera) {
            CameraFragment cameraFragment = (CameraFragment) fragmentManager.findFragmentByTag(CAMERA_FRAGMENT_ID);
            if (cameraFragment != null) {
                fragmentManager.beginTransaction().show(cameraFragment).commit();
            } else {
                fragmentManager.beginTransaction().add(R.id.fragment_container_view, CameraFragment.class, null, CAMERA_FRAGMENT_ID).commit();
            }
            return true;
        } else if (itemId == R.id.itemGallery) {
            // TODO: implement GalleryFragment
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }
}
