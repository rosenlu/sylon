package net.luisr.sylon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends AppCompatActivity {

    List<Page> pageList = new ArrayList<>();
    RecyclerView pagesRecView;
    FloatingActionButton btnAdd, btnAddByCamera, btnAddByGallery;
    boolean isFabOpen;
    FilesRecViewAdapter adapter;
    AppDatabase database;
    int documentId;
    Document document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        database = AppDatabase.getInstance(this);

        documentId = getIntent().getIntExtra("EXTRA_DOCUMENT_ID", -1);
        pageList = database.docDao().getPages(documentId);

        document = database.docDao().getById(documentId);
        setTitle(document.getName());

        pagesRecView = findViewById(R.id.pagesRecView);
        btnAdd = findViewById(R.id.btnAdd);
        btnAddByCamera = findViewById(R.id.btnAddByCamera);
        btnAddByGallery = findViewById(R.id.btnAddByGallery);
        isFabOpen = false;

        pagesRecView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new FilesRecViewAdapter(DocumentActivity.this, pageList);
        pagesRecView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            if (!isFabOpen) {
                showFabMenu();
            } else {
                closeFabMenu();
            }
        });

        btnAddByCamera.setOnClickListener(v -> {
            Page page = new Page(documentId);
            database.pageDao().insert(page);

            pageList.clear();
            pageList.addAll(database.docDao().getPages(documentId));
            adapter.notifyDataSetChanged();
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
}
