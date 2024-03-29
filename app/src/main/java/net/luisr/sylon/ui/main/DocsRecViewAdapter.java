package net.luisr.sylon.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Document;
import net.luisr.sylon.db.Page;
import net.luisr.sylon.fs.FileManager;
import net.luisr.sylon.ui.doc.PageListActivity;

import java.util.List;

/**
 * The {@link RecyclerView.Adapter} for the {@link DocsRecViewAdapter} of the {@link DocumentListFragment}.
 *
 * Each item represents one instance of the {@link Document} class.
 * The UI contains a preview of the first page, the name of the Document and a button for more
 * options. These options are changing the name of or deleting the Document instance in the database.
 * @see MainActivity
 * @see DocumentListFragment
 */
public class DocsRecViewAdapter extends RecyclerView.Adapter<DocsRecViewAdapter.ViewHolder> {
    /** The tag used for logging */
    private static final String TAG = "DocsRecViewAdapter";

    /** The activity from which the adapter was created. */
    private final Activity context;

    /** A list containing all the documents to be shown in the {@link RecyclerView}. */
    private final List<Document> docList;

    /** The app's database. */
    private AppDatabase database;

    /**
     * Constructor for the {@link DocsRecViewAdapter}.
     * @param context the activity from which the adapter was created.
     * @param docList a list containing all the documents to be shown in the RecyclerView.
     */
    public DocsRecViewAdapter(Activity context, List<Document> docList) {
        this.database = AppDatabase.getInstance(context);
        this.context = context;
        this.docList = docList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocsRecViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_docs, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocsRecViewAdapter.ViewHolder holder, int position) {
        // get the document at the current position
        Document document = docList.get(position);

        // show the name of the document
        holder.txtDocName.setText(document.getName());

        // show a preview of the first page in the document, if it exists
        Page firstPage = database.pageDao().getFirstPageInDocument(document.getId());
        if (firstPage != null) {
            Uri thumbUri = Uri.parse(firstPage.getThumbUri());
            if (thumbUri != null) {
                holder.imgViewFirstPage.setImageURI(thumbUri);
            }
        } else {
            holder.imgViewFirstPage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_launcher_background));
        }

        // set an OnClickListener for the txtViewOptions
        holder.imgViewMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.imgViewMoreOptions);
            popup.inflate(R.menu.options_menu_document);
            popup.setOnMenuItemClickListener(item -> itemClickCallback(item, document, position));
            popup.show();
        });

        // set an OnClickListener for the card as a whole
        holder.cardViewParent.setOnClickListener(v -> {
            Intent intent = new Intent(context.getBaseContext(), PageListActivity.class);
            intent.putExtra(PageListActivity.INTENT_EXTRA_DOCUMENT_ID, document.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    /**
     * Callback function for when an item in the txtViewOptions popup menu is clicked.
     * @param item the item that was clicked.
     * @param document the document at the current position.
     * @param position the current position of the ViewHolder.
     * @return true, if the click was consumed and the popup should be closed.
     */
    private boolean itemClickCallback(MenuItem item, Document document, int position) {
        int itemId = item.getItemId();
        if (itemId == R.id.itemEdit) {
            itemEditCallback(document, position);
            return true;
        } else if (itemId == R.id.itemDelete) {
            itemDeleteCallback(document, position);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Callback function for when the popup item to edit the document name was clicked.
     * @param document the document at the current position.
     * @param position the current position of the ViewHolder.
     */
    private void itemEditCallback(Document document, int position) {
        // get id and name of the document
        int documentId = document.getId();
        String documentName = document.getName();

        // show dialog in which the user can enter a new name for the document
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_doc_name);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        // get UI elements of dialog
        EditText edtText = dialog.findViewById(R.id.edtText);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        // set the text of the edtText to the current name of the document
        edtText.setText(documentName);

        // set listeners for the enter key and the update button
        edtText.setOnEditorActionListener((textView, i, keyEvent) -> {
            dialog.dismiss();
            btnUpdateCallback(documentId, edtText.getText().toString().trim(), position);
            return false;
        });
        btnUpdate.setOnClickListener(dv -> {
            dialog.dismiss();
            btnUpdateCallback(documentId, edtText.getText().toString().trim(), position);
        });
    }

    /**
     * Callback function for the update button of the dialog created by the itemEdit of the popup menu.
     * @param id the ID of the document to update.
     * @param name the new name of the document.
     * @param position the current position of the ViewHolder.
     */
    private void btnUpdateCallback(int id, String name, int position) {
        // name should not be empty
        if (!name.equals("")) {
            // change name in database
            database.docDao().setName(id, name);

            // update doc in docList and notify adapter
            docList.set(position, database.docDao().getById(id));
            notifyItemChanged(position);
        } else {
            // show message that the doc name cannot be empty
            Toast.makeText(context, context.getString(R.string.empty_doc_name_msg), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback function for when the popup item to delete the document name was clicked.
     * @param document the document at the current position.
     * @param position the current position of the ViewHolder.
     */
    private void itemDeleteCallback(Document document, int position) {
        // get the name of the document
        String documentName = document.getName();

        // create dialog to show a warning to the user
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_delete_doc);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        // get UI elements of dialog
        TextView txtMessage = dialog.findViewById(R.id.txtMessage);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // show warning to the user
        txtMessage.setText(context.getResources().getString(R.string.dialog_delete_doc_msg_name, documentName));

        // set onClickListeners for the cancel and delete (i.e. confirm) button
        btnCancel.setOnClickListener(dv -> dialog.dismiss());
        btnDelete.setOnClickListener(dv -> {
            dialog.dismiss();
            confirmDeleteCallback(document, position);
        });
    }

    /**
     * Callback function for when the user confirms the warning dialog created by
     * {@link #itemDeleteCallback(Document, int)}.
     * @param document the document at the current position.
     * @param position the current position of the ViewHolder.
     */
    private void confirmDeleteCallback(Document document, int position) {
        // delete all source images for the pages in the document
        List<Page> pages = database.pageDao().getPagesInDocument(document.getId());
        for (Page p : pages) {
            Uri uri = Uri.parse(p.getImageUri());
            if (!FileManager.rm(uri)) {
                Log.e(TAG, "Could not delete source image: " + uri);
            }
            Uri thumbUri = Uri.parse(p.getThumbUri());
            if (!FileManager.rm(thumbUri)) {
                Log.e(TAG, "Could not delete thumb image: " + thumbUri);
            }
        }

        // delete document from database and docList
        // pages are automatically deleted from database when parent document is deleted
        database.docDao().delete(document);
        docList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, docList.size());

        // show hints, if docList is empty
        if (docList.isEmpty()) {
            DocumentListFragment documentListFragment = (DocumentListFragment) ((MainActivity) context).getSupportFragmentManager().findFragmentByTag(DocumentListFragment.FRAGMENT_ID);
            if (documentListFragment != null) {
                documentListFragment.getGroupNoDocuments().setVisibility(View.VISIBLE);
            }
        }
    }

    /** The {@link ViewHolder} class extracts the UI elements from the layout. */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDocName;
        MaterialCardView cardViewParent;
        ImageView imgViewFirstPage, imgViewMoreOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDocName = itemView.findViewById(R.id.txtDocName);
            imgViewMoreOptions = itemView.findViewById(R.id.imgViewMoreOptions);
            cardViewParent = itemView.findViewById(R.id.cardViewParent);
            imgViewFirstPage = itemView.findViewById(R.id.imgViewFirstPage);
        }
    }
}
