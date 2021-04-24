package net.luisr.sylon.ui.acquisition;

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

        ImageView imgViewPage = findViewById(R.id.imgViewPage);

        //imageUri = Uri.parse("file:///storage/emulated/0/Android/media/net.luisr.sylon/Sylon/img/4FwK8NU (1).jpg");
        imageUri = Uri.parse(getIntent().getStringExtra(INTENT_EXTRA_IMAGE_URI));
        imgViewPage.setImageURI(imageUri);

        imgViewPage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            public void onGlobalLayout() {


                int height = imgViewPage.getHeight();
                int width = imgViewPage.getWidth();
                int x = imgViewPage.getLeft();
                int y = imgViewPage.getTop();

                Rect rt = imgViewPage.getDrawable().getBounds();

                int ih=imgViewPage.getMeasuredHeight();//height of imageView
                int iw=imgViewPage.getMeasuredWidth();//width of imageView
                int iH=imgViewPage.getDrawable().getIntrinsicHeight();//original height of underlying image
                int iW=imgViewPage.getDrawable().getIntrinsicWidth();//original width of underlying image


                Log.d(TAG, "CROPPER Height "+height+", Width "+width+", X "+x+", Y "+y);
                Log.d(TAG, "CROPPER Rect = "+rt);
                Log.d(TAG, "CROPPER ih "+ih+", iw "+iw+", iH "+iH+", iW "+iW);

                // remove listener
                imgViewPage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });

    }
}
