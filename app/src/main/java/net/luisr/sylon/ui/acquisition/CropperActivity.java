package net.luisr.sylon.ui.acquisition;

import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Page;

public class CropperActivity extends AppCompatActivity {

    /** The tag used for logging */
    private static final String TAG = "CropperActivity";

    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri_to_crop";

    private Uri imageUri;

    public CropperActivity() {
        super(R.layout.activity_cropper);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CropperView cropperViewPage = findViewById(R.id.cropperViewPage);

        //imageUri = Uri.parse("file:///storage/emulated/0/Android/media/net.luisr.sylon/Sylon/img/4FwK8NU (1).jpg");
        imageUri = Uri.parse(getIntent().getStringExtra(INTENT_EXTRA_IMAGE_URI));
        cropperViewPage.setImageURI(imageUri);

        int imageWidth = cropperViewPage.getDrawable().getIntrinsicWidth();
        int imageHeight = cropperViewPage.getDrawable().getIntrinsicHeight();
        Point[] cornerPoints = new Point[4];
        cornerPoints[0] = new Point(    imageWidth / 4,     imageHeight / 4);
        cornerPoints[1] = new Point(    imageWidth / 4, 3 * imageHeight / 4);
        cornerPoints[2] = new Point(3 * imageWidth / 4, 3 * imageHeight / 4);
        cornerPoints[3] = new Point(3 * imageWidth / 4,     imageHeight / 4);
        cropperViewPage.setCornerPoints(cornerPoints);

    }
}
