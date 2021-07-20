package net.luisr.sylon.ui.acquisition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Page;
import net.luisr.sylon.fs.DirManager;
import net.luisr.sylon.img.RotationHandler;
import net.luisr.sylon.img.ThumbnailFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CropperActivity extends AppCompatActivity {

    /** The tag used for logging */
    private static final String TAG = "CropperActivity";

    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri_to_crop";

    private Uri imageUri;

    private Bitmap correctedBitmap;

    private CropperView cropperViewPage;

    /** Selected Resolution in DPI */
    private int targetDpi;

    /** Selected page size in inches */
    private double targetWidth, targetHeight;

    public CropperActivity() {
        super(R.layout.activity_cropper);

        // todo make adjustable
        targetDpi = 300;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Crop");

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

                        constraintLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        setBitmap(clWidth, clHeight);
                    }
                }
        );
    }

    private void setBitmap(int maxWidth, int maxHeight) {
        try {
            correctedBitmap = ThumbnailFactory.getResizedAndRotatedBitmap(this, imageUri, maxWidth, maxHeight);
            cropperViewPage.setImageBitmap(correctedBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int imageWidth = cropperViewPage.getDrawable().getIntrinsicWidth();
        int imageHeight = cropperViewPage.getDrawable().getIntrinsicHeight();
        Point[] cornerPoints = new Point[4];
        cornerPoints[0] = new Point(    imageWidth / 4,     imageHeight / 4);
        cornerPoints[1] = new Point(    imageWidth / 4, 3 * imageHeight / 4);
        cornerPoints[2] = new Point(3 * imageWidth / 4, 3 * imageHeight / 4);
        cornerPoints[3] = new Point(3 * imageWidth / 4,     imageHeight / 4);
        cropperViewPage.setCornerPoints(cornerPoints);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_cropper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemSaveCrop) {

            // hack FIXME delete me

            Point[] cropPoints = cropperViewPage.getCornerPoints();

            Matrix matrix = new Matrix();
            if (matrix.setPolyToPoly(
                    new float[] {
                            cropPoints[0].x, cropPoints[0].y,
                            cropPoints[1].x, cropPoints[1].y,
                            cropPoints[2].x, cropPoints[2].y,
                            cropPoints[3].x, cropPoints[3].y
                    },
                    0,
                    new float[] {
                            0f, 0f,
                            0f, 1000f,
                            1000f, 1000f,
                            1000f, 0f
                    },
                    0,
                    4 )) {


                Bitmap output = Bitmap.createBitmap(correctedBitmap, 0, 0, correctedBitmap.getWidth(), correctedBitmap.getHeight(), matrix, true);

                cropperViewPage.setImageBitmap(output);

            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
