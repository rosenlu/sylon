package net.luisr.sylon.ui.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Document;
import net.luisr.sylon.ui.acquisition.CameraFragment;
import net.luisr.sylon.ui.doc.PageListActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link Fragment} showing a list of all {@link Document}s in the {@link AppDatabase}.
 * This fragment is the default fragment of the {@link MainActivity}. Via a button, the user can add a
 * Document to the database. All Documents that are available are listed in a {@link RecyclerView}.
 */
public class DocumentListFragment extends Fragment {

    public static final String FRAGMENT_ID = "net.luisr.sylon.document_list_fragment_id";

    /** The app's database containing all documents and pages. */
    private AppDatabase database;

    /** A list containing all documents. The list is passed to the {@link #docsRecView}. */
    private List<Document> docList = new ArrayList<>();

    /** A {@link RecyclerView} showing all the documents in the {@link #docList}. */
    private RecyclerView docsRecView;

    /** The {@link RecyclerView.Adapter} for the {@link #docsRecView}. */
    private DocsRecViewAdapter adapter;

    /** A button to create a new {@link Document} */
    private FloatingActionButton btnAdd;

    /** A group with all text and image views containing hints when no {@link Document} is found. */
    private Group groupNoDocuments;

    /** The tag used for logging */
    private static final String TAG = "DocumentListFragment";

    /** Constructor, defining the layout file of the {@link Fragment}. */
    public DocumentListFragment() {
        super(R.layout.fragment_document_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get UI elements
        docsRecView = view.findViewById(R.id.docsRecView);
        btnAdd = view.findViewById(R.id.btnAdd);
        groupNoDocuments = view.findViewById(R.id.groupNoDocuments);

        // get database and fill docList
        database = AppDatabase.getInstance(view.getContext());
        docList = database.docDao().getAll();

        // show hints, if docList is empty
        if (docList.isEmpty()) {
            groupNoDocuments.setVisibility(View.VISIBLE);
        }

        // set layout and adapter for docsRecView
        docsRecView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new DocsRecViewAdapter((MainActivity) view.getContext(), docList);
        docsRecView.setAdapter(adapter);

        // set OnClickListener for btnAdd
        btnAdd.setOnClickListener(v -> btnAddCallback());

        // listen for a result from one of the other fragments of the MainActivity
        listenForFragmentResult();
    }

    /**
     * Callback function for the {@link #btnAdd} {@link android.view.View.OnClickListener}.
     *
     * Creates a {@link Dialog} in which the user has to enter the name of the document.
     * After pressing a confirm button, the document is added to the database and shown in the
     * {@link #docsRecView}.
     */
    private void btnAddCallback() {
        // open dialog to enter document name
        Dialog dialog = new Dialog(requireView().getContext());
        dialog.setContentView(R.layout.dialog_add_doc);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        // get UI elements of dialog
        EditText edtText = dialog.findViewById(R.id.edtText);
        Button btnConfirm = dialog.findViewById(R.id.btnAdd);

        // set listeners for the enter key and the confirm button
        edtText.setOnEditorActionListener((textView, i, keyEvent) -> {
            dialog.dismiss();
            btnConfirmCallback(edtText.getText().toString().trim());
            return false;
        });
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            btnConfirmCallback(edtText.getText().toString().trim());
        });
    }

    /**
     * Callback function for the confirm button of the {@link #btnAddCallback()}.
     *
     * Creates a new {@link Document} with the name passed by the user and saves it into the
     * {@link #database} as well as the {@link #docList}.
     * @param name the name of the Document.
     */
    private void btnConfirmCallback(String name) {
        // name should not be empty
        if (!name.equals("")) {
            addDocumentAndStartPageListActivity(name, null);
        } else {
            // show message that the doc name cannot be empty
            Toast.makeText(requireView().getContext(), getString(R.string.empty_doc_name_msg), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Listen for a Result on the parent {@link FragmentManager}. This result can either come from
     * the {@link CameraFragment} or from the (not yet existing) GalleryFragment.
     * TODO: Add link to GalleryFragment
     */
    private void listenForFragmentResult() {
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.setFragmentResultListener(CameraFragment.RESULT_REQUEST_KEY, this, (requestKey, result) -> {
            // get uri from the bundle
            String savedUri = result.getString(CameraFragment.BUNDLE_KEY_IMAGE_URI);

            if (savedUri.isEmpty()) {  // something went wrong (usually camera permissions denied)
                // show Toast message and set result to canceled
                Toast.makeText(requireContext(), R.string.permissions_denied, Toast.LENGTH_SHORT).show();
            } else {  // everything is fine
                // hide the camera fragment
                fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(CameraFragment.FRAGMENT_ID));

                // get document name and add it to the database
                String documentName = getDocumentName();
                requireActivity().runOnUiThread(() -> addDocumentAndStartPageListActivity(documentName, savedUri));
            }
        });

        // TODO: setFragmentResultListener for the GalleryFragment
    }

    /**
     * Get a unique name for a newly create {@link Document}.
     * @return the unique Document name
     */
    private String getDocumentName() {
        String datetimeFormat = "yyyy-MM-dd";

        String baseDocName = new SimpleDateFormat(datetimeFormat, Locale.US).format(System.currentTimeMillis()) + " New Document";
        String finalDocName = baseDocName;
        List<Document> docsWithSameName = database.docDao().getAllByName(finalDocName);
        int i = 1;
        while (docsWithSameName.size() > 0) {
            finalDocName = baseDocName + " (" + i++ + ")";
            docsWithSameName = database.docDao().getAllByName(finalDocName);
        }

        return finalDocName;
    }

    /**
     * Add a new {@link Document} to both the {@link #database} and the {@link #docList}. After the
     * Document is created, the {@link PageListActivity} is started.
     * @param name the name of the new Document.
     * @param firstPageImgUri the image URI of the first page, can be null.
     */
    private void addDocumentAndStartPageListActivity(@NonNull String name, String firstPageImgUri) {
        // instantiate Document
        Document doc = new Document(name);

        // insert doc to database
        int docId = (int) database.docDao().insert(doc);
        doc.setId(docId);

        // dismiss hints, if docList was empty
        if (docList.isEmpty()) {
            groupNoDocuments.setVisibility(View.GONE);
        }

        // insert doc to docList and notify DocsRecViewAdapter
        docList.add(doc);
        adapter.notifyDataSetChanged();

        Context context = requireContext();
        Intent intent = new Intent(context, PageListActivity.class);
        intent.putExtra(PageListActivity.INTENT_EXTRA_DOCUMENT_ID, docId);
        intent.putExtra(PageListActivity.INTENT_EXTRA_FIRST_PAGE_IMG_URI, firstPageImgUri);
        context.startActivity(intent);
    }

    /**
     * Getter for the groupNoDocuments {@link Group}.
     * @return the groupNoDocuments
     */
    public Group getGroupNoDocuments() {
        return groupNoDocuments;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); // force rebind
    }
}

