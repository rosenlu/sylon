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

public class FilesRecViewAdapter extends RecyclerView.Adapter<FilesRecViewAdapter.ViewHolder> {

    private List<SylonFile> fileList;
    private Activity context;
    private AppDatabase database;

    public FilesRecViewAdapter(Activity context, List<SylonFile> fileList) {
        this.fileList = fileList;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilesRecViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_files, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesRecViewAdapter.ViewHolder holder, int position) {
        database = AppDatabase.getInstance(context);
        SylonFile sylonFile = fileList.get(position);

        holder.txtFileName.setText(sylonFile.getName());

        holder.btnEdit.setOnClickListener(v -> {
            SylonFile sf = fileList.get(holder.getAdapterPosition());

            int sID = sf.getId();
            String sName = sf.getName();

            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_edit_file_name);
            int w = WindowManager.LayoutParams.MATCH_PARENT;
            int h = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(w, h);
            dialog.show();

            EditText edtText = dialog.findViewById(R.id.edtText);
            Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

            edtText.setText(sName);

            btnUpdate.setOnClickListener(dv -> {
                dialog.dismiss();

                String uName = edtText.getText().toString();
                database.fileDao().update(sID, uName);
//                fileList.clear();
//                fileList.addAll(database.fileDao().getAll());
//                notifyDataSetChanged();
                int pos = holder.getAdapterPosition();
                fileList.set(pos, database.fileDao().getById(sID));
                notifyItemChanged(pos);
            });
        });

        holder.btnDelete.setOnClickListener(v -> {
            SylonFile sf = fileList.get(holder.getAdapterPosition());

            int sID = sf.getId();
            String sName = sf.getName();

            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_delete_file);
            int w = WindowManager.LayoutParams.MATCH_PARENT;
            int h = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(w, h);
            dialog.show();

            TextView txtMessage = dialog.findViewById(R.id.txtMessage);
            Button btnDelete = dialog.findViewById(R.id.btnDelete);
            Button btnCancel = dialog.findViewById(R.id.btnCancel);

            // TODO: set text of txtMessage to show the current filename (sName)

            btnCancel.setOnClickListener(dv -> {
                dialog.dismiss();
            });

            btnDelete.setOnClickListener(dv -> {
                dialog.dismiss();

                database.fileDao().delete(sf);
                int pos = holder.getAdapterPosition();
                fileList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, fileList.size());
            });

        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
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
