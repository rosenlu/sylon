package net.luisr.sylon.ui.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.luisr.sylon.R;

/**
 * The Main {@link android.app.Activity} of the app.
 * This activity is the launcher activity and shows a list of all {@link net.luisr.sylon.db.Document}s
 * in the {@link net.luisr.sylon.db.AppDatabase}.
 * Via a button, the user can add a Document to the AppDatabase.
 */
public class MainActivity extends AppCompatActivity {

    public static String DOCUMENT_LIST_FRAGMENT_ID = "net.luisr.sylon.document_list_fragment_id";

    /** The bottom navigation bar of the layout. */
    private BottomNavigationView bottomNavigationView;

    /** The fragment transaction used to replace the current fragment with another one. */
    private FragmentTransaction fragmentTransaction;

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
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.replace(R.id.fragment_container_view, DocumentListFragment.class, null, DOCUMENT_LIST_FRAGMENT_ID);
            fragmentTransaction.commit();
        }
    }
}