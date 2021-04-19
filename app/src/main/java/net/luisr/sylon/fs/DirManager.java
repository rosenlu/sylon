package net.luisr.sylon.fs;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import net.luisr.sylon.R;

import java.io.File;

public class DirManager {

    /** The tag used for logging */
    private static final String TAG = "DirManager";


    /**
     * Retrieve either the first external media directory from
     * {@link ContextWrapper#getExternalMediaDirs()} if it is valid or the "files directory" from
     * {@link ContextWrapper#getFilesDir()}.
     * @return the resolved output directory
     */
    private static File getOutputDirectory(Context context) {
        File[] externalMediaDirs = context.getExternalMediaDirs();
        File filesDir = context.getFilesDir();
        if (externalMediaDirs.length != 0) {
            File firstExtDir = externalMediaDirs[0];
            if (firstExtDir != null && firstExtDir.exists()) {
                File subDir = new File(firstExtDir, context.getString(R.string.app_name));
                if (subDir.mkdirs() || subDir.exists()) {
                    return subDir;
                }
            }
        }
        return filesDir;
    }


    /**
     * Retrieve the img subdir of our output directory
     * @return the resolved image directory
     */
    public static File getImageDirectory(Context context) {
        // check for image directory and create if necessary
        File outputDirectory = getOutputDirectory(context);
        File imgDirectory = new File(outputDirectory, "img");
        if (!imgDirectory.mkdirs() || !imgDirectory.exists()) {
            // the directory does not exist and we could not create it
            Log.w(TAG, "Could not create img subdir.");
        }
        return imgDirectory;
    }


    /**
     * Retrieve the thumb subdir of our output directory
     * @return the resolved thumbnail directory
     */
    public static File getThumbDirectory(Context context) {
        // check for thumb directory and create if necessary
        File outputDirectory = getOutputDirectory(context);
        File thumbDirectory = new File(outputDirectory, "thumb");
        if (!thumbDirectory.mkdirs() || !thumbDirectory.exists()) {
            // the directory does not exist and we could not create it
            Log.w(TAG, "Could not create thumb subdir.");
        }
        return thumbDirectory;
    }
}
