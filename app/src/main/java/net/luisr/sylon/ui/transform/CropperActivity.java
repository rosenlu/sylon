package net.luisr.sylon.ui.transform;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Page;

public class CropperActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_PAGE_ID = "net.luisr.sylon.page_id";

    private AppDatabase database;
    private int pageId;
    private Page page;

    public CropperActivity() {
        super(R.layout.activity_cropper);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imgViewPage = findViewById(R.id.imgViewPage);

        database = AppDatabase.getInstance(this);
        pageId = getIntent().getIntExtra(INTENT_EXTRA_PAGE_ID, -1);
        page = database.pageDao().getById(pageId);
        imgViewPage.setImageURI(Uri.parse(page.getImageUri()));

    }
}
