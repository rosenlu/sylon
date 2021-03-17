package net.luisr.sylon.ui.doc;

import android.app.Activity;
import android.net.Uri;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

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

    /** A SelectionTracker tracking the selection of one or multiple pages. */
    private SelectionTracker<Long> selectionTracker;

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

    /**
     * Setter for the selection tracker.
     * @param selectionTracker the selection tracker.
     */
    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
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

        // bind the page to the view holder
        holder.bind(page, position);
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }

    /** The ViewHolder class extracts the UI elements from the layout and fills them with content. */
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final Details details;
        private final ImageView imgViewPage;
        private final TextView txtPageNumber;
        private final MaterialCardView cardViewParent;

        /**
         * Constructor for the view holder. Gets all the UI elements from the layout.
         * @param itemView an item view.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgViewPage = itemView.findViewById(R.id.imgViewPage);
            txtPageNumber = itemView.findViewById(R.id.txtPageNumber);
            cardViewParent = itemView.findViewById(R.id.cardViewParent);
            details = new Details();
        }

        /**
         * Bind a page to its position. Show the page number and the preview image to the user.
         * @param page the page.
         * @param position the position of the page.
         */
        private void bind(Page page, int position) {
            // set the position in the item details, needed for selectionTracker
            details.position = position;

            // show the page number
            txtPageNumber.setText(context.getResources().getString(R.string.page_number, position, page.getId()));

            // set the image preview
            String imgUri = page.getImageUri();
            if (imgUri != null) {
                imgViewPage.setImageURI(Uri.parse(imgUri));
            }

            // set the selected pages as checked
            if (selectionTracker != null) {
                cardViewParent.setChecked(selectionTracker.isSelected(details.getSelectionKey()));
            }
        }

        /**
         * Getter for the item details.
         * @return the item details.
         */
        ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return details;
        }

    }

    /**
     * The KeyProvider class maps the keys in the selection to the position in the pageList.
     * The key and position are identical.
     */
    public static class KeyProvider extends ItemKeyProvider<Long> {
        KeyProvider() {
            super(ItemKeyProvider.SCOPE_MAPPED);
        }

        @Nullable
        @Override
        public Long getKey(int position) {
            return (long) position;
        }

        @Override
        public int getPosition(@NonNull Long key) {
            long value = key;
            return (int) value;
        }
    }

    /** The DetailsLookup class gets the details of the item that was selected by the user. */
    public static class DetailsLookup extends ItemDetailsLookup<Long> {

        private final RecyclerView recyclerView;

        DetailsLookup(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
                if (viewHolder instanceof ViewHolder) {
                    return ((ViewHolder) viewHolder).getItemDetails();
                }
            }

            return null;
        }
    }

    /** The Details class defines the details of a selected item. */
    private static class Details extends ItemDetailsLookup.ItemDetails<Long> {

        long position;

        @Override
        public int getPosition() {
            return (int) position;
        }

        @Nullable
        @Override
        public Long getSelectionKey() {
            return position;
        }

        @Override
        public boolean inSelectionHotspot(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public boolean inDragRegion(@NonNull MotionEvent e) {
            return true;
        }
    }

}
