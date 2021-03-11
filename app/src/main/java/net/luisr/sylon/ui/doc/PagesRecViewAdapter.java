package net.luisr.sylon.ui.doc;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.luisr.sylon.R;
import net.luisr.sylon.db.Page;

import java.util.List;


public class PagesRecViewAdapter extends RecyclerView.Adapter<PagesRecViewAdapter.ViewHolder> {

    private final List<Page> pageList;
    private Activity context;

    public PagesRecViewAdapter(Activity context, List<Page> pageList) {
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
        Page page = pageList.get(position);
        holder.txtPageNumber.setText(context.getResources().getString(R.string.page_number, position, page.getId()));
        String imgUri = page.getImageUri();
        if (imgUri != null) {
            holder.imgViewPage.setImageURI(Uri.parse(imgUri));
        }
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
