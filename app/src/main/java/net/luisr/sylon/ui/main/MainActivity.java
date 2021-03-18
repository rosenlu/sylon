package net.luisr.sylon.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * The Main Activity of the app.
 * This activity is the launcher activity and shows a list of all documents in the database.
 * Via a button, the user can add a document to the database.
 */
public class MainActivity extends AppCompatActivity {

    /** The app's database containing all documents and pages. */
    private AppDatabase database;

    /** A list containing all documents. The list is passed to the docsRecView. */
    private List<Document> docList = new ArrayList<>();

    /** A RecyclerView showing all the documents in the docList. */
    private RecyclerView docsRecView;

    /** The RecyclerView.Adapter for the docsRecView. */
    private DocsRecViewAdapter adapter;

    /** A button to create a new document */
    private FloatingActionButton btnAdd;

    /**
     * A group with all text and image views containing hints when no document is found.
     * Is package-private, as DocsRecViewAdapter needs access to it.
     */
    Group groupNoDocuments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get UI elements
        docsRecView = findViewById(R.id.docsRecView);
        btnAdd = findViewById(R.id.btnAdd);
        groupNoDocuments = findViewById(R.id.groupNoDocuments);

        // get database and fill docList
        database = AppDatabase.getInstance(this);
        docList = database.docDao().getAll();

        // show hints, if docList is empty
        if (docList.isEmpty()) {
            groupNoDocuments.setVisibility(View.VISIBLE);
        }

        // set layout and adapter for docsRecView
        docsRecView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocsRecViewAdapter(MainActivity.this, docList);
        docsRecView.setAdapter(adapter);

        // set OnClickListener for btnAdd
        btnAdd.setOnClickListener(v -> btnAddCallback());
    }

    /**
     * Callback function for the btnAdd OnClickListener.
     *
     * Creates a dialog in which the user has to enter the name of the document.
     * After pressing a confirm button, the document is added to the database and shown in the
     * docsRecView.
     */
    private void btnAddCallback() {
        // open dialog to enter document name
        Dialog dialog = new Dialog(this);
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
     * Callback function for the btnConfirm OnClickListener.
     *
     * Creates a new Document with the name passed by the user and saves it into the database as
     * well as the docList.
     * @param name the name of the Document.
     */
    private void btnConfirmCallback(String name) {
        // name should not be empty
        if (!name.equals("")) {
            // instantiate Document
            Document doc = new Document(name);

            // insert doc to database
            doc.setId((int) database.docDao().insert(doc));

            // dismiss hints, if docList was empty
            if (docList.isEmpty()) {
                groupNoDocuments.setVisibility(View.GONE);
            }

            // insert doc to docList and notify DocsRecViewAdapter
            docList.add(doc);
            adapter.notifyDataSetChanged();
        } else {
            // show message that the doc name cannot be empty
            Toast.makeText(this, getString(R.string.empty_doc_name_msg), Toast.LENGTH_SHORT).show();
        }
    }
}