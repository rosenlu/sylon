package net.luisr.sylon.ui.doc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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

/**
 * An activity showing the details of a document.
 * The activity shows the name of the document as well as all the pages inside.
 */
public class DocumentActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DOCUMENT_ID = "net.luisr.sylon.document_id";
    private static final int CAMERA_REQUEST_CODE = 1;

    /** The app's database containing all documents and pages. */
    private AppDatabase database;

    /** A list containing all pages. The list is passed to the pagesRecView. */
    private List<Page> pageList = new ArrayList<>();

    /** The ID of the document for which the activity was started. */
    private int documentId;

    /** The document for which the activity was started. */
    private Document document;

    /** A RecyclerView showing all the pages in the pageList. */
    private RecyclerView pagesRecView;

    /** The RecyclerView.Adapter for the pagesRecView. */
    private PagesRecViewAdapter adapter;

    /** The FloatingActionButtons (FABs) for the FAB menu */
    private FloatingActionButton btnAdd, btnAddByCamera, btnAddByGallery;

    /** Contains the opening state of the FAB menu. */
    private boolean isFabOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        // get UI elements
        pagesRecView = findViewById(R.id.pagesRecView);
        btnAdd = findViewById(R.id.btnAdd);
        btnAddByCamera = findViewById(R.id.btnAddByCamera);
        btnAddByGallery = findViewById(R.id.btnAddByGallery);
        isFabOpen = false;

        // get current document from the database and populate page list
        database = AppDatabase.getInstance(this);
        documentId = getIntent().getIntExtra(INTENT_EXTRA_DOCUMENT_ID, -1);
        document = database.docDao().getById(documentId);
        setTitle(document.getName());
        pageList = database.pageDao().getPagesInDocument(documentId);

        // set layout of the RecyclerView
        pagesRecView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PagesRecViewAdapter(DocumentActivity.this, pageList);
        pagesRecView.setAdapter(adapter);

        // set on click listeners for the FAB menu
        setFabMenuOnClickListeners();

        // attach ItemTouchHelper to RecyclerView to allow reordering of pages
        ItemTouchHelper pagesTouchHelper = new ItemTouchHelper(getPagesTouchCallback());
        pagesTouchHelper.attachToRecyclerView(pagesRecView);
    }

    /** Set on click listeners for the FAB menu. */
    private void setFabMenuOnClickListeners() {
        // toggle the FAB menu when btnAdd is clicked
        btnAdd.setOnClickListener(v -> {
            if (!isFabOpen) {
                showFabMenu();
            } else {
                closeFabMenu();
            }
        });

        // start CameraActivity when camera icon is clicked
        btnAddByCamera.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        });

        // start GalleryActivity when gallery icon is clicked (not implemented yet)
        btnAddByGallery.setOnClickListener(v ->
                Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show());
    }

    /** Open the FAB menu. */
    private void showFabMenu() {
        // toggle status of FAB menu
        isFabOpen = true;

        // get sizes and margin from resources
        float fabSizeDefault = getResources().getDimension(R.dimen.fab_size_default);
        float fabSizeSmall = getResources().getDimension(R.dimen.fab_size_small);
        float fabMargin = getResources().getDimension(R.dimen.fab_margin);

        // calculate the amount of translation for the smaller buttons
        float translationBtnAddByCamera = fabSizeDefault / (float) 2.0 + fabSizeSmall / (float) 2.0 + fabMargin;
        float translationBtnAddByGallery = translationBtnAddByCamera + fabSizeSmall + fabMargin;

        // animate buttons
        btnAdd.animate().rotation(45);
        btnAddByCamera.animate().translationY(- translationBtnAddByCamera);
        btnAddByGallery.animate().translationY(- translationBtnAddByGallery);
        btnAddByCamera.animate().alpha(1);
        btnAddByGallery.animate().alpha(1);
    }

    /** Close the FAB menu. */
    private void closeFabMenu() {
        // toggle status of FAB menu
        isFabOpen = false;

        // animate buttons
        btnAdd.animate().rotation(0);
        btnAddByCamera.animate().translationY(0);
        btnAddByGallery.animate().translationY(0);
        btnAddByCamera.animate().alpha(0);
        btnAddByGallery.animate().alpha(0);
    }

    /**
     * Get the the callback object for the pagesTouchHelper that enables reordering of pages.
     * @return the callback object
     */
    private ItemTouchHelper.Callback getPagesTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.START | ItemTouchHelper.END, 0) {

            // the position from which the item started
            int fromPosition;

            // the position at which the item was dropped
            int toPosition;

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                // set the fromPosition only when the dragging initially starts
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    if (viewHolder != null) {
                        fromPosition = viewHolder.getAdapterPosition();
                    }
                }
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // the localFromPosition changes every time the item is dragged (even when it was not dropped in between)
                int localFromPosition = viewHolder.getAdapterPosition();
                toPosition = target.getAdapterPosition();

                // move the item
                PagesRecViewAdapter pagesAdapter = (PagesRecViewAdapter) recyclerView.getAdapter();
                if (pagesAdapter != null) {
                    pagesAdapter.notifyItemMoved(localFromPosition, toPosition);
                }

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                // executed once the item was actually dropped
                // only move the page if it really changes positions
                if (fromPosition != -1 && toPosition != -1 && fromPosition != toPosition) {
                    movePage(fromPosition, toPosition);
                }
            }
        };
    }

    /**
     * Move a page withing the pageList.
     * @param from the position at which the page used to be
     * @param to the new position of the page
     */
    private void movePage(int from, int to) {
        Page page = pageList.remove(from);
        pageList.add(to, page);
        if (from > to) {
            for (int i = from; i >= to; i--) {
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

        // check if the result came from the CameraActivity
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                // create a new page with the recorded image
                Page page = Page.makeNew(documentId);
                page.setImageUri(data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_URI));

                // insert the page to the pageList
                pageList.add(page);
                page.setPageNumber(pageList.indexOf(page));
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // update modified pages and insert new pages to database
        database.pageDao().update(pageList.stream().filter(Page::hasBeenModified).toArray(Page[]::new));
        database.pageDao().insert(pageList.stream().filter(Page::isNew).toArray(Page[]::new));

        // reset values of "modified" and "isNew"
        pageList.stream().filter(Page::hasBeenModified).collect(Collectors.toList()).forEach(p -> p.setModified(false));
        pageList.stream().filter(Page::isNew).collect(Collectors.toList()).forEach(Page::clearNew);
    }
}
