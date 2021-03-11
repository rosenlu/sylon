package net.luisr.sylon.ui.doc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import java.util.stream.Collectors;

public class DocumentActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DOCUMENT_ID = "net.luisr.sylon.document_id";
    private static final int CAMERA_REQUEST_CODE = 1;


    private List<Page> pageList = new ArrayList<>();
    private AppDatabase database;
    private Document document;
    private int documentId;
    private boolean isFabOpen;


    private RecyclerView pagesRecView;
    private FloatingActionButton btnAdd, btnAddByCamera, btnAddByGallery;
    private PagesRecViewAdapter adapter;
    private ItemTouchHelper pagesTouchHelper;
    private ItemTouchHelper.Callback pagesTouchCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        database = AppDatabase.getInstance(this);

        documentId = getIntent().getIntExtra(INTENT_EXTRA_DOCUMENT_ID, -1);
        pageList = database.pageDao().getPagesInDocument(documentId);

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

        btnAddByGallery.setOnClickListener(v -> {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
        });

        pagesTouchCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.START | ItemTouchHelper.END, 0) {

            int fromPosition;
            int toPosition;

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    if (viewHolder != null) {
                        fromPosition = viewHolder.getAdapterPosition();
                    }
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int localFromPosition = viewHolder.getAdapterPosition();
                toPosition = target.getAdapterPosition();
                PagesRecViewAdapter pagesAdapter = (PagesRecViewAdapter) recyclerView.getAdapter();
                if (pagesAdapter != null) {
                    pagesAdapter.notifyItemMoved(localFromPosition, toPosition);
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                if(fromPosition != -1 && toPosition != -1 && fromPosition != toPosition) {
                    movePage(fromPosition, toPosition);
                }
            }
        };

        pagesTouchHelper = new ItemTouchHelper(pagesTouchCallback);
        pagesTouchHelper.attachToRecyclerView(pagesRecView);
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

    private void insertPage(Page page) {
        pageList.add(page);
        page.setPageNumber(pageList.indexOf(page));
        adapter.notifyDataSetChanged();
    }

    private void movePage(int from, int to) {
        Page page = pageList.remove(from);
        pageList.add(to, page);
        if (from > to) {
            for (int i = to; i <= from; i++) {
                pageList.get(i).setPageNumber(i);
                pageList.get(i).setModified(true);
            }
        } else if (to > from) {
            for (int i = from; i <= to; i++) {
                pageList.get(i).setPageNumber(i);
                pageList.get(i).setModified(true);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Page page = Page.makeNew(documentId);
                page.setImageUri(data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_URI));
                insertPage(page);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        database.pageDao().update(pageList.stream().filter(Page::wasModified).toArray(Page[]::new));
        database.pageDao().insert(pageList.stream().filter(Page::getNew).toArray(Page[]::new));

        pageList.stream().filter(Page::wasModified).collect(Collectors.toList()).forEach(p -> p.setModified(false));
        pageList.stream().filter(Page::getNew).collect(Collectors.toList()).forEach(Page::clearNew);
    }
}
