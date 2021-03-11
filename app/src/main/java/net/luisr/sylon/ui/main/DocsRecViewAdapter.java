package net.luisr.sylon.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import net.luisr.sylon.R;
import net.luisr.sylon.db.AppDatabase;
import net.luisr.sylon.db.Document;
import net.luisr.sylon.db.Page;
import net.luisr.sylon.ui.doc.DocumentActivity;

import java.util.List;

public class DocsRecViewAdapter extends RecyclerView.Adapter<DocsRecViewAdapter.ViewHolder> {

    private List<Document> docList;
    private Activity context;
    private AppDatabase database;

    public DocsRecViewAdapter(Activity context, List<Document> fileList) {
        this.docList = fileList;
        this.context = context;
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
        database = AppDatabase.getInstance(context);
        Document document = docList.get(position);

        holder.txtFileName.setText(document.getName());

        Page firstPage = database.pageDao().getFirstPageInDocument(document.getId());
        if (firstPage != null) {
            String imgPath = firstPage.getImageUri();
            if (imgPath != null) {
                holder.imgViewFirstPage.setImageURI(Uri.parse(imgPath));
            }
        }

        holder.txtViewOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.txtViewOptions);
            popup.inflate(R.menu.options_menu_document);
            popup.setOnMenuItemClickListener(item -> {
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
            });
            popup.show();
        });

        holder.cardViewParent.setOnClickListener(v -> {
            Intent intent = new Intent(context.getBaseContext(), DocumentActivity.class);
            intent.putExtra(DocumentActivity.INTENT_EXTRA_DOCUMENT_ID, document.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    private void itemEditCallback(Document document, int position) {
        int documentId = document.getId();
        String documentName = document.getName();

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_doc_name);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        EditText edtText = dialog.findViewById(R.id.edtText);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        edtText.setText(documentName);

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

    private void btnUpdateCallback(int id, String name, int position) {
        database.docDao().setName(id, name);
        docList.set(position, database.docDao().getById(id));
        notifyItemChanged(position);
    }

    private void itemDeleteCallback(Document document, int position) {
        String documentName = document.getName();

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_delete_doc);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        TextView txtMessage = dialog.findViewById(R.id.txtMessage);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        txtMessage.setText(context.getResources().getString(R.string.dialog_delete_file_msg_name, documentName));

        btnCancel.setOnClickListener(dv -> dialog.dismiss());

        btnDelete.setOnClickListener(dv -> {
            dialog.dismiss();

            database.docDao().delete(document);
            docList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, docList.size());
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFileName, txtViewOptions;
        MaterialCardView cardViewParent;
        ImageView imgViewFirstPage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFileName = itemView.findViewById(R.id.txtFileName);
            txtViewOptions = itemView.findViewById(R.id.txtViewOptions);
            cardViewParent = itemView.findViewById(R.id.cardViewParent);
            imgViewFirstPage = itemView.findViewById(R.id.imgViewFirstPage);
        }
    }
}
