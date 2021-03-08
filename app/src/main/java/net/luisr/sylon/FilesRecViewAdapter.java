package net.luisr.sylon;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class FilesRecViewAdapter extends RecyclerView.Adapter<FilesRecViewAdapter.ViewHolder> {

    private List<Page> pageList;
    private Activity context;
    private AppDatabase database;

    public FilesRecViewAdapter(Activity context, List<Page> pageList) {
        this.pageList = pageList;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_pages, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        database = AppDatabase.getInstance(context);
        Page page = pageList.get(position);


    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgViewPage;
        TextView txtPageNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewPage = itemView.findViewById(R.id.imgViewPage);
            txtPageNumber = itemView.findViewById(R.id.txtPageNumber);
        }
    }
}
