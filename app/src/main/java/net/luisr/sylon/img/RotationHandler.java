package net.luisr.sylon.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;




/**
 * This class handles the different rotations of images on different phones.
 *
 * It is based on Sami Eltamawy's answer to this stackoverflow post:
 * https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
 *
 * Credits: Sami Eltamawy, Jason Robinson, Felix
 */
public class RotationHandler {

    /**
     * Get the rotation of an image in degree. Can either be 0, 90, 180 or 270.
     *
     * @param selectedImage the URI of the selected image
     * @return The rotation in degree.
     */
    public static int getRotationInDegree(Context context, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        ei = new ExifInterface(input);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Rotate an image if required.
     *
     * @param img    The image bitmap
     * @param degree The rotation of the image in degree as returned by {@link #getRotationInDegree(Context, Uri)}.
     * @return The resulted {@link Bitmap} after manipulation
     */
    public static Bitmap rotateImageIfRequired(Bitmap img, int degree) throws IOException {
        if (degree == 0) {
            return img;
        } else {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        }
    }
}
