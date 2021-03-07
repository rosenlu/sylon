package net.luisr.sylon;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocsRecViewAdapter extends RecyclerView.Adapter<DocsRecViewAdapter.ViewHolder> {

    private List<Document> fileList;
    private Activity context;
    private AppDatabase database;

    public DocsRecViewAdapter(Activity context, List<Document> fileList) {
        this.fileList = fileList;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocsRecViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_files, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocsRecViewAdapter.ViewHolder holder, int position) {
        database = AppDatabase.getInstance(context);
        Document document = fileList.get(position);

        holder.txtFileName.setText(document.getName());

        holder.btnEdit.setOnClickListener(v -> btnEditCallback(document, position));
        holder.btnDelete.setOnClickListener(v -> btnDeleteCallback(document, position));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private void btnEditCallback(Document document, int position) {
        int sID = document.getId();
        String sName = document.getName();

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit_file_name);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        EditText edtText = dialog.findViewById(R.id.edtText);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        edtText.setText(sName);

        edtText.setOnEditorActionListener((textView, i, keyEvent) -> {
            dialog.dismiss();
            btnUpdateCallback(sID, edtText.getText().toString().trim(), position);
            return false;
        });
        btnUpdate.setOnClickListener(dv -> {
            dialog.dismiss();
            btnUpdateCallback(sID, edtText.getText().toString().trim(), position);
        });
    }

    private void btnUpdateCallback(int id, String name, int position) {
        database.docDao().update(id, name);
        fileList.set(position, database.docDao().getById(id));
        notifyItemChanged(position);
    }

    private void btnDeleteCallback(Document document, int position) {
        int sID = document.getId();
        String sName = document.getName();

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_delete_file);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        TextView txtMessage = dialog.findViewById(R.id.txtMessage);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        txtMessage.setText(context.getResources().getString(R.string.dialog_delete_file_msg_name, sName));

        btnCancel.setOnClickListener(dv -> dialog.dismiss());

        btnDelete.setOnClickListener(dv -> {
            dialog.dismiss();

            database.docDao().delete(document);
            fileList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, fileList.size());
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFileName;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtFileName = itemView.findViewById(R.id.txtFileName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
