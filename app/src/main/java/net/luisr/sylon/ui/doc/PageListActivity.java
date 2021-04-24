package net.luisr.sylon.ui.doc;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.view.ActionMode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Document;
import net.luisr.sylon.db.Page;
import net.luisr.sylon.fs.FileManager;
import net.luisr.sylon.img.ThumbnailFactory;
import net.luisr.sylon.ui.acquisition.CameraActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An activity showing the details of a {@link Document}.
 * The activity shows the name of the Document as well as all the pages inside.
 */
public class PageListActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_DOCUMENT_ID = "net.luisr.sylon.page_list_document_id";
    public static final String INTENT_EXTRA_FIRST_PAGE_IMG_URI = "net.luisr.sylon.first_page_img_uri";
    public static final String PAGES_SELECTION_ID = "net.luisr.sylon.pages_selection_id";

    /** The tag used for logging */
    private static final String TAG = "PageListActivity";

    /** The app's database containing all documents and pages. */
    private AppDatabase database;

    /** A list containing all pages. The list is passed to the {@link PagesRecViewAdapter}. */
    private List<Page> pageList = new ArrayList<>();

    /** A list keeping track of all pages the user has deleted while the activity was running. */
    private List<Page> deletedPageList = new ArrayList<>();

    /** The ID of the document for which the activity was started. */
    private int documentId;

    /** The document for which the activity was started. */
    private Document document;

    /** A {@link RecyclerView} showing all the pages in the pageList. */
    private RecyclerView pagesRecView;

    /** The {@link RecyclerView.Adapter} for the {@link PagesRecViewAdapter}. */
    private PagesRecViewAdapter adapter;

    /** The {@link SelectionTracker} keeping track of selected pages. */
    SelectionTracker<Long> pagesSelectionTracker;

    /** The {@link ActionMode} that is activated when one or multiple pages are selected. */
    private ActionMode actionMode;

    /** The {@link FloatingActionButton}s (FABs) for the FAB menu */
    private FloatingActionButton btnAdd, btnAddByCamera, btnAddByGallery;

    /** Contains the opening state of the FAB menu. */
    private boolean isFabOpen;

    /** A {@link Group} with all text and image views containing hints when no {@link Page} is found. */
    private Group groupNoPages;

    /**
     * An activity result launcher to launch the {@link CameraActivity} and get the URI of the saved
     * image back as a result. The contract is a {@link androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult}
     * object and callback will handle adding a new {@link Page} with the respective image URI to
     * the {@link AppDatabase}.
     */
    private ActivityResultLauncher<Intent> cameraActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_page_list);

        // get UI elements
        pagesRecView = findViewById(R.id.pagesRecView);
        btnAdd = findViewById(R.id.btnAdd);
        btnAddByCamera = findViewById(R.id.btnAddByCamera);
        btnAddByGallery = findViewById(R.id.btnAddByGallery);
        isFabOpen = false;
        groupNoPages = findViewById(R.id.groupNoPages);

        // get current document from the database and populate page list
        database = AppDatabase.getInstance(this);
        documentId = getIntent().getIntExtra(INTENT_EXTRA_DOCUMENT_ID, -1);
        document = database.docDao().getById(documentId);
        setTitle(document.getName());
        pageList = database.pageDao().getPagesInDocument(documentId);

        // show hints, if pageList is empty
        if (pageList.isEmpty()) {
            groupNoPages.setVisibility(View.VISIBLE);
        }

        // set layout of the RecyclerView
        pagesRecView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PagesRecViewAdapter(PageListActivity.this, pageList);
        pagesRecView.setAdapter(adapter);

        // see if a first page image uri was passed and create first page
        String firstPageImageUri = getIntent().getStringExtra(INTENT_EXTRA_FIRST_PAGE_IMG_URI);
        if (firstPageImageUri != null) {
            addPage(firstPageImageUri);
        }

        // initialize the ActivityResultLauncher for the CameraActivity
        cameraActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() == RESULT_OK && data != null) {
                        addPage(data.getStringExtra(CameraActivity.INTENT_EXTRA_IMAGE_URI));
                    }
                }
        );

        // set on click listeners for the FAB menu
        setFabMenuOnClickListeners();

        // attach ItemTouchHelper to RecyclerView to allow reordering of pages
        ItemTouchHelper pagesTouchHelper = new ItemTouchHelper(getPagesTouchCallback());
        pagesTouchHelper.attachToRecyclerView(pagesRecView);

        // create and attach the SelectionTracker to Recycler view to allow selection pages
        createAndAttachSelectionTracker();
    }

    /** Set on {@link android.view.View.OnClickListener}s for the FAB menu. */
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
            cameraActivityLauncher.launch(intent);
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
     * Create the {@link SelectionTracker}, attach it to the {@link PagesRecViewAdapter} and add a
     * {@link androidx.recyclerview.selection.SelectionTracker.SelectionObserver}.
     */
    private void createAndAttachSelectionTracker() {
        pagesSelectionTracker = new SelectionTracker.Builder<>(
                PAGES_SELECTION_ID,
                pagesRecView,
                new PagesRecViewAdapter.KeyProvider(),
                new PagesRecViewAdapter.DetailsLookup(pagesRecView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build();
        adapter.setSelectionTracker(pagesSelectionTracker);
        pagesSelectionTracker.addObserver(getSelectionObserver());
    }

    /**
     * Get the {@link androidx.recyclerview.selection.SelectionTracker.SelectionObserver} for the
     * {@link SelectionTracker}.
     * @return the selection observer.
     */
    private SelectionTracker.SelectionObserver<Long> getSelectionObserver() {
        return new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onSelectionChanged() {
                super.onSelectionChanged();
                if (pagesSelectionTracker.getSelection().size() > 0) {
                    if (actionMode == null) {
                        actionMode = startSupportActionMode(getActionModeCallback());
                    } else {
                        actionMode.setTitle(getResources().getString(R.string.selection_title, pagesSelectionTracker.getSelection().size()));
                    }
                } else if (actionMode != null) {
                    actionMode.finish();
                }
            }
        };
    }

    /**
     * Get the the {@link ActionMode.Callback} object for the pagesTouchHelper that enables selecting
     * one or multiple pages.
     * @return the callback object
     */
    private ActionMode.Callback getActionModeCallback() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = new MenuInflater(PageListActivity.this);
                menuInflater.inflate(R.menu.options_menu_page, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();
                PagesRecViewAdapter pagesAdapter = (PagesRecViewAdapter) pagesRecView.getAdapter();

                // check if delete button has been pressed
                if (id == R.id.itemDelete) {
                    // get the currently selected items
                    Selection<Long> selection = pagesSelectionTracker.getSelection();

                    // arrange the keys in descending order to avoid conflicts with already deleted pages
                    ArrayList<Long> keysToDelete = new ArrayList<>();
                    for (Long key : selection) {
                        keysToDelete.add(key);
                    }
                    keysToDelete.sort(Collections.reverseOrder());

                    // iterate over keys and delete corresponding pages
                    for (Long key : keysToDelete) {
                        int position = (int) (long) key;

                        // delete page in pageList, add it to deletedPageList and delete the source image
                        Page page = pageList.remove(position);
                        if (!page.isNew()) {
                            deletedPageList.add(page);
                        }
                        Uri uri = Uri.parse(page.getImageUri());
                        if (!FileManager.rm(uri)) {
                            Log.e(TAG, "Could not delete source image: " + uri);
                        }
                        Uri thumbUri = Uri.parse(page.getThumbUri());
                        if (!FileManager.rm(thumbUri)) {
                            Log.e(TAG, "Could not delete thumb image: " + thumbUri);
                        }

                        // update page numbers for all following pages
                        for (int i = position; i < pageList.size(); i++) {
                            pageList.get(i).setPageNumber(i);
                            pageList.get(i).setModified(true);
                        }

                        // notify the adapter
                        if (pagesAdapter != null) {
                            pagesAdapter.notifyItemRemoved(position);
                        }
                    }

                    // show hints, if pageList now is empty
                    if (pageList.isEmpty()) {
                        groupNoPages.setVisibility(View.VISIBLE);
                    }

                    // clear the selection and stop the actionMode
                    pagesSelectionTracker.clearSelection();
                    actionMode = null;
                }

                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                pagesSelectionTracker.clearSelection();
                actionMode = null;
            }
        };
    }

    /**
     * Get the the {@link ItemTouchHelper.Callback} object for the pagesTouchHelper that enables
     * reordering of pages.
     * @return the callback object
     */
    private ItemTouchHelper.Callback getPagesTouchCallback() {
        return new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.START | ItemTouchHelper.END, 0) {

            // the position from which the item started
            int fromPosition;

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
                int localToPosition = target.getAdapterPosition();

                // move the item
                PagesRecViewAdapter pagesAdapter = (PagesRecViewAdapter) recyclerView.getAdapter();
                if (pagesAdapter != null) {
                    pageList.add(localToPosition, pageList.remove(localFromPosition));
                    pagesAdapter.notifyItemMoved(localFromPosition, localToPosition);
                }

                // clear the selection
                pagesSelectionTracker.clearSelection();
                actionMode = null;

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                // executed once the item was actually dropped
                movePage(fromPosition, viewHolder.getAdapterPosition());
                PagesRecViewAdapter pagesAdapter = (PagesRecViewAdapter) recyclerView.getAdapter();
                if (pagesAdapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        };
    }

    /**
     * Move a {@link Page} withing the {@link #pageList}.
     * @param from the position at which the page used to be
     * @param to the new position of the page
     */
    private void movePage(int from, int to) {
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
    }

    /**
     * Add a page to the {@link #database} as well as the {@link #pageList}.
     * @param imagePath the URI of the image of the first page.
     */
    private void addPage(String imagePath) {
        // create a new page with the recorded image
        Page page = Page.makeNew(documentId);
        page.setImageUri(imagePath);

        // dismiss hints, if pageList was empty
        if (pageList.isEmpty()) {
            groupNoPages.setVisibility(View.GONE);
        }

        try {
            page.setThumbUri(ThumbnailFactory.makeThumbnail(this, Uri.parse(imagePath)).toString());
        } catch (IOException e) {
            Log.e(TAG, "Error creating thumbnail: " + Arrays.toString(e.getStackTrace()));
        }

        // insert the page to the pageList
        pageList.add(page);
        page.setPageNumber(pageList.indexOf(page));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // update modified pages, insert new pages to database and remove deleted pages from database
        long[] newIds = database.pageDao().insert(pageList.stream().filter(Page::isNew).toArray(Page[]::new));
        database.pageDao().update(pageList.stream().filter(Page::hasBeenModified).toArray(Page[]::new));
        database.pageDao().delete(deletedPageList.toArray(new Page[0]));

        // update new ids
        int idx = 0;
        for (Page p : pageList) {
            if (p.isNew()) {
                p.setId((int) newIds[idx++]);
            }
        }

        // reset values of "modified" and "isNew"
        pageList.stream().filter(Page::hasBeenModified).collect(Collectors.toList()).forEach(p -> p.setModified(false));
        pageList.stream().filter(Page::isNew).collect(Collectors.toList()).forEach(Page::clearNew);
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();
    }
}
