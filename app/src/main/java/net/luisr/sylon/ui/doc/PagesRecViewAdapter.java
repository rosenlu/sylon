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
import net.luisr.sylon.db.Document;
import net.luisr.sylon.db.Page;
import net.luisr.sylon.ui.main.MainActivity;

import java.util.List;

/**
 * The RecyclerView.Adapter for the pagesRecView of the DocumentActivity.
 *
 * Each item represents one instance of the Page class.
 * The UI contains a preview of the page and the page number.
 * @see DocumentActivity
 * @see Page
 */
public class PagesRecViewAdapter extends RecyclerView.Adapter<PagesRecViewAdapter.ViewHolder> {

    /** The activity from which the adapter was created. */
    private Activity context;

    /** A list containing all the pages to be shown in the RecyclerView. */
    private final List<Page> pageList;

    /**
     * Constructor for the PagesRecViewAdapter.
     * @param context the activity from which the adapter was created.
     * @param pageList a list containing all the pages to be shown in the RecyclerView.
     */
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
        // get the page at the current position
        Page page = pageList.get(position);

        // show the page number
        holder.txtPageNumber.setText(context.getResources().getString(R.string.page_number, position, page.getId()));

        // set the image preview
        String imgUri = page.getImageUri();
        if (imgUri != null) {
            holder.imgViewPage.setImageURI(Uri.parse(imgUri));
        }
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    /** The ViewHolder class extracts the UI elements from the layout. */
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
