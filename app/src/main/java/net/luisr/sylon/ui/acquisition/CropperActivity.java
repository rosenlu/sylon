package net.luisr.sylon.ui.acquisition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import net.luisr.sylon.R;
import net.luisr.sylon.img.ThumbnailFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CropperActivity extends AppCompatActivity {

    /** The tag used for logging */
    private static final String TAG = "CropperActivity";

    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri_to_crop";

    private Uri imageUri;

    private Bitmap displayedBitmap;
    private int displayedBitmapWidth;

    private int resizeRatio;

    private CropperView cropperViewPage;

    private MagnifierView magnifierView;

    private int magnifierWidth, magnifierHeight;

    private final ExecutorService imageLoadExecutor;

    /** Selected Resolution in DPI */
    private int targetDpi;

    /** Selected page size in inches */
    private double targetWidth, targetHeight;

    public CropperActivity() {
        super(R.layout.activity_cropper);
        imageLoadExecutor = Executors.newSingleThreadExecutor();

        // todo make adjustable
        targetDpi = 300;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Crop");

        magnifierView = findViewById(R.id.magnifierView);

        cropperViewPage = findViewById(R.id.cropperViewPage);
        ConstraintLayout constraintLayout = findViewById(R.id.cropperConstraintLayout);


        //imageUri = Uri.parse("file:///storage/emulated/0/Android/media/net.luisr.sylon/Sylon/img/4FwK8NU (1).jpg");
        imageUri = Uri.parse(getIntent().getStringExtra(INTENT_EXTRA_IMAGE_URI));

        constraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int clWidth = constraintLayout.getMeasuredWidth();
                        int clHeight = constraintLayout.getMeasuredHeight();

                        magnifierWidth = magnifierView.getMeasuredWidth();
                        magnifierHeight = magnifierView.getMeasuredHeight();

                        constraintLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        setupImages(clWidth, clHeight);
                    }
                }
        );
    }

    private void setupImages(int layoutWidth, int layoutHeight) {

        Context context = this;
        imageLoadExecutor.execute(() -> {
            try {
                ThumbnailFactory.ResizeResult resizeResult = ThumbnailFactory.getResizedAndRotatedBitmap(context, imageUri, layoutWidth, layoutHeight);
                displayedBitmap = resizeResult.bitmap;
                resizeRatio = resizeResult.inSampleSize;
                runOnUiThread(() -> {
                    cropperViewPage.setImageBitmap(displayedBitmap);
                    resetCornerPointsToDefault();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        imageLoadExecutor.execute(() -> {
            try {
                Bitmap magnifierBitmap = ThumbnailFactory.getResizedAndRotatedBitmap(context, imageUri, Integer.MAX_VALUE, Integer.MAX_VALUE).bitmap;
                runOnUiThread(() -> {
                    magnifierView.setImageBitmap(magnifierBitmap);
                    cropperViewPage.setDragCallback(this::onDrag);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        imageLoadExecutor.shutdown();

    }

    private void resetCornerPointsToDefault() {
        displayedBitmapWidth = cropperViewPage.getDrawable().getIntrinsicWidth();
        int displayedBitmapHeight = cropperViewPage.getDrawable().getIntrinsicHeight();
        Point[] cornerPoints = new Point[4];
        cornerPoints[0] = new Point(    displayedBitmapWidth / 4,     displayedBitmapHeight / 4);
        cornerPoints[1] = new Point(    displayedBitmapWidth / 4, 3 * displayedBitmapHeight / 4);
        cornerPoints[2] = new Point(3 * displayedBitmapWidth / 4, 3 * displayedBitmapHeight / 4);
        cornerPoints[3] = new Point(3 * displayedBitmapWidth / 4,     displayedBitmapHeight / 4);
        cropperViewPage.setCornerPoints(cornerPoints);
    }


    private void onDrag(Point dragPoint) {
        if (dragPoint.equals(CropperView.POINT_NOT_DRAGGING)) {
            magnifierView.setVisibility(View.INVISIBLE);
        } else {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) magnifierView.getLayoutParams();
            if (dragPoint.x < (displayedBitmapWidth * 0.5f)) {
                params.horizontalBias = 1.0f - ResourcesCompat.getFloat(getResources(), R.dimen.magnifier_bias);
            } else {
                params.horizontalBias = ResourcesCompat.getFloat(getResources(), R.dimen.magnifier_bias);
            }
            magnifierView.setLayoutParams(params);

            magnifierView.setVisibility(View.VISIBLE);
            Matrix m = magnifierView.getImageMatrix();
            m.reset();
            m.postTranslate(-1f * resizeRatio * dragPoint.x + magnifierWidth * 0.5f,
                    -1f * resizeRatio * dragPoint.y + magnifierHeight * 0.5f);
            magnifierView.invalidate();
            magnifierView.setImageMatrix(m);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_cropper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemSaveCrop && imageLoadExecutor.isShutdown()) {

            // hack FIXME delete me

            Bitmap input = displayedBitmap;
            Bitmap output;
            // reminder: for transforming original image, scale corner points by downsample ratio
            Point[] cropPoints = cropperViewPage.getCornerPoints();
            int targetWidth = 1000; // calculate later with target size & dpi
            int targetHeight = 1000;

            // get min/max of the cropPoints in order to reduce number of pixels that need to undergo transformation
            int startX = Math.min(cropPoints[0].x, cropPoints[1].x);
            int startY = Math.min(cropPoints[0].y, cropPoints[3].y);
            int endX = Math.max(cropPoints[2].x, cropPoints[3].x);
            int endY = Math.max(cropPoints[1].y, cropPoints[2].y);

            // get the transformation matrix
            Matrix matrix = new Matrix();
            if (matrix.setPolyToPoly(
                    new float[] {
                            cropPoints[0].x-startX, cropPoints[0].y-startY,
                            cropPoints[1].x-startX, cropPoints[1].y-startY,
                            cropPoints[2].x-startX, cropPoints[2].y-startY,
                            cropPoints[3].x-startX, cropPoints[3].y-startY
                    },
                    0,
                    new float[] {
                            0f, 0f,
                            0f, (float) targetHeight,
                            (float) targetWidth, (float) targetHeight,
                            (float) targetWidth, 0f
                    },
                    0,
                    4 )) {

                // get transformed coordinates of the edges of the bitmap
                // (see Answer #1 in https://www.py4u.net/discuss/636133)
                float[] mappedTopLeft = new float[] { 0, 0 };
                matrix.mapPoints(mappedTopLeft);
                int maptlx = Math.round(mappedTopLeft[0]);
                int maptly = Math.round(mappedTopLeft[1]);

                float[] mappedTopRight = new float[] { endX-startX, 0 };
                matrix.mapPoints(mappedTopRight);
                int maptrx = Math.round(mappedTopRight[0]);
                int maptry = Math.round(mappedTopRight[1]);

                float[] mappedLowerLeft = new float[] { 0, endY-startY };
                matrix.mapPoints(mappedLowerLeft);
                int mapllx = Math.round(mappedLowerLeft[0]);
                int maplly = Math.round(mappedLowerLeft[1]);

                int shiftX = Math.max(-maptlx, -mapllx);
                int shiftY = Math.max(-maptry, -maptly);

                Bitmap transformedBitmap = Bitmap.createBitmap(input, startX, startY, endX-startX, endY-startY, matrix, true);
                output = Bitmap.createBitmap(transformedBitmap, shiftX, shiftY, targetWidth, targetHeight, null, true);


                // for testing inside cropper
                displayedBitmap = output;
                cropperViewPage.setImageBitmap(displayedBitmap);
                resetCornerPointsToDefault();
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
