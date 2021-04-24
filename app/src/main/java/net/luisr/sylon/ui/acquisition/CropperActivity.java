package net.luisr.sylon.ui.acquisition;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Page;

public class CropperActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_IMAGE_URI = "net.luisr.sylon.image_uri_to_crop";

    private Uri imageUri;

    public CropperActivity() {
        super(R.layout.activity_cropper);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imgViewPage = findViewById(R.id.imgViewPage);

        imageUri = Uri.parse(getIntent().getStringExtra(INTENT_EXTRA_IMAGE_URI));
        imgViewPage.setImageURI(imageUri);

        RelativeLayout layoutDonuts = findViewById(R.id.layoutDonuts);

    }
}
