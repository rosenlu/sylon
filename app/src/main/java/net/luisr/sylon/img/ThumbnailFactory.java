package net.luisr.sylon.img;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import net.luisr.sylon.fs.DirManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class resizes an image to match a maximum width or height and then saves it to a file.
 *
 * It is partially based on Sami Eltamawy's answer to this stackoverflow post:
 * https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
 *
 * Credits: Sami Eltamawy, Jason Robinson, Felix
 */
public class ThumbnailFactory {

    private static final int MAX_SIZE = 100;

    /** The tag used for logging */
    private static final String TAG = "ThumbnailFactory";

    /**
     * Scale and rotate an image from URI so that after the rotation the width and height
     * constraints are fulfilled.
     *
     * @param selectedImage The URI of the selected image.
     * @param maxWidth the max width of the final, rotated image.
     * @param maxHeight the max height of the final, rotated image.
     * @return the scaled and rotated bitmap.
     * @throws IOException if URI does not exist.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static Bitmap getResizedAndRotatedBitmap(Context context, Uri selectedImage, int maxWidth, int maxHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        int degree = RotationHandler.getRotationInDegree(context, selectedImage);

        // Calculate inSampleSize
        if (degree == 90 || degree == 270) {
            options.inSampleSize = calculateInSampleSize(options, maxHeight, maxWidth);
        } else {
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        return RotationHandler.rotateImageIfRequired(img, degree);
    }

    /**
     * Scale an image (from URI) so that the longer side is {@link #MAX_SIZE} pixels long and save
     * it to files.
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return {@link Uri} path to resulting thumbnail
     * @throws IOException if URI does not exist
     */
    public static Uri makeThumbnail(Context context, Uri selectedImage)
            throws IOException {
        Bitmap img = getResizedAndRotatedBitmap(context, selectedImage, MAX_SIZE, MAX_SIZE);

        File thumbDir = DirManager.getThumbDirectory(context);

        // TODO make filename unique

        File thumb = new File(thumbDir, selectedImage.getLastPathSegment());

        FileOutputStream out = new FileOutputStream(thumb);
        img.compress(Bitmap.CompressFormat.JPEG, 90, out);
        Log.d(TAG, "Thumbnail created and saved to "+thumb.getAbsolutePath());

        return Uri.fromFile(thumb);
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or smaller than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of imageoptions
        final int height = options.outHeight;
        final int width = options.outWidth;

        // Calculate ratios of height and width to requested height and width
        final int heightRatio = Math.round((float) height / (float) reqHeight);
        final int widthRatio = Math.round((float) width / (float) reqWidth);

        // Choose the largest ratio as inSampleSize value, this will guarantee a final image
        // with both dimensions smaller than or equal to the requested height and width.
        return Math.max(1, Math.max(heightRatio, widthRatio));
    }
}
