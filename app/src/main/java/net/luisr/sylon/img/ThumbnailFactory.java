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
     * Scale an image (from URI) so that the shorter side is {@link #MAX_SIZE} pixels long. Also
     * calls {@link RotationHandler#rotateImageIfRequired(Context, Bitmap, Uri)} to correct for
     * possible EXIF rotation.
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return {@link Uri} path to resulting thumbnail
     * @throws IOException if URI does not exist
     */
    public static Uri makeThumbnail(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = MAX_SIZE;
        int MAX_WIDTH = MAX_SIZE;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = RotationHandler.rotateImageIfRequired(context, img, selectedImage);

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
     * height equal to or larger than the requested width and height. This implementation does not
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
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


}
