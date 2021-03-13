package net.luisr.sylon.fs;

import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** Manages files on the devices hard drive. */
public class FileManager {

    /**
     * Remove a file from the file system, if it exists.
     * @param uri the URI of the file to delete.
     * @return true, if the file was successfully deleted.
     */
    public static boolean rm(Uri uri) {
        // get the file
        File f = new File(uri.getPath());

        // delete the file, if it exists
        if (f.exists()) {
            return f.delete();
        } else {
            return false;
        }
    }

    /**
     * Copy a file from one URI to another.
     * @param srcUri the URI of the file to copy.
     * @param dstUri the URI to which the file will be copied.
     * @return true, if the file was successfully copied.
     */
    public static boolean cp(Uri srcUri, Uri dstUri) {
        // get the files
        File srcFile = new File(srcUri.getPath());
        File dstFile = new File(dstUri.getPath());

        // make sure we don't overwrite anything
        if (srcFile.exists() && !dstFile.exists()) {
            // try blocks to make sure the streams are closed in case of an exception
            try (InputStream inputStream = new FileInputStream(srcFile)) {
                try (OutputStream outputStream = new FileOutputStream(dstFile)) {
                    // transfer bytes from inputStream to outputStream
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }
}
