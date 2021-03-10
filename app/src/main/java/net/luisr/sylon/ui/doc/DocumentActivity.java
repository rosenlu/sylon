package net.luisr.sylon.ui.doc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Document;
import net.luisr.sylon.db.Page;
import net.luisr.sylon.ui.acquisition.CameraActivity;

import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DOCUMENT_ID = "net.luisr.sylon.document_id";
    private static final int CAMERA_REQUEST_CODE = 1;


    private List<Page> pageList = new ArrayList<>();
    private RecyclerView pagesRecView;
    private FloatingActionButton btnAdd, btnAddByCamera, btnAddByGallery;
    private boolean isFabOpen;
    private PagesRecViewAdapter adapter;
    private AppDatabase database;
    private int documentId;
    private Document document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        database = AppDatabase.getInstance(this);

        documentId = getIntent().getIntExtra(INTENT_EXTRA_DOCUMENT_ID, -1);
        pageList = getOrderedPageList();

        document = database.docDao().getById(documentId);
        setTitle(document.getName());

        pagesRecView = findViewById(R.id.pagesRecView);
        btnAdd = findViewById(R.id.btnAdd);
        btnAddByCamera = findViewById(R.id.btnAddByCamera);
        btnAddByGallery = findViewById(R.id.btnAddByGallery);
        isFabOpen = false;

        pagesRecView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PagesRecViewAdapter(DocumentActivity.this, pageList);
        pagesRecView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            if (!isFabOpen) {
                showFabMenu();
            } else {
                closeFabMenu();
            }
        });

        btnAddByCamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(DocumentActivity.INTENT_EXTRA_DOCUMENT_ID, document.getId());
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        });

        btnAddByGallery.setOnClickListener(v -> Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show());
    }

    private void showFabMenu() {
        isFabOpen = true;
        btnAddByCamera.setVisibility(View.VISIBLE);
        btnAddByGallery.setVisibility(View.VISIBLE);

        float fabSizeDefault = getResources().getDimension(R.dimen.fab_size_default);
        float fabSizeSmall = getResources().getDimension(R.dimen.fab_size_small);
        float fabMargin = getResources().getDimension(R.dimen.fab_margin);

        float translationBtnAddByCamera = fabSizeDefault / (float) 2.0 + fabSizeSmall / (float) 2.0 + fabMargin;
        float translationBtnAddByGallery = translationBtnAddByCamera + fabSizeSmall + fabMargin;

        btnAdd.animate().rotation(45);

        btnAddByCamera.animate().translationY(- translationBtnAddByCamera);
        btnAddByGallery.animate().translationY(- translationBtnAddByGallery);

        btnAddByCamera.animate().alpha(1);
        btnAddByGallery.animate().alpha(1);
    }

    private void closeFabMenu() {
        isFabOpen = false;
        btnAdd.animate().rotation(0);

        btnAddByCamera.animate().translationY(0);
        btnAddByGallery.animate().translationY(0);

        btnAddByCamera.animate().alpha(0);
        btnAddByGallery.animate().alpha(0);
    }

    private List<Page> getOrderedPageList() {
        List<Page> orderedPageList = new ArrayList<>();
        Integer next = database.pageDao().getFirstPageIdInDocument(documentId);

        Page pg;

        while (next != null) {
            pg = database.pageDao().getById(next);
            orderedPageList.add(pg);
            next = pg.getNextPageId();
        }

        return orderedPageList;
    }

    private void insertPage(Page page) {
        Page currentLastPage = database.pageDao().getLastPage(documentId);
        int newPageId = (int) database.pageDao().insert(page);
        if (currentLastPage != null) {
            database.pageDao().setNextPageId(currentLastPage.getId(), newPageId);
        }

        page.setId(newPageId);
        pageList.add(page);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Page page = new Page(documentId);
                page.setImagePath(data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_URI));
                insertPage(page);
            }
        }
    }
}
