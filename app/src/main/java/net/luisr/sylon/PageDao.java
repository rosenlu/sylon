package net.luisr.sylon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class PageDao {

    int insert (Page page) {
        int documentId = page.getDocumentId();
        Page currentLastPage = _getLastPage(documentId);
        int newPageId = (int) _insert(page);
        if (currentLastPage != null) {
            setNextPageId(currentLastPage.getId(), newPageId);
        }

        return newPageId;
    }

    @Insert(onConflict = REPLACE)
    abstract long _insert(Page page);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND next_page_id ISNULL LIMIT 1")
    abstract Page _getLastPage(int documentId);

    @Delete
    abstract void delete(Page page);

    @Query("UPDATE pages SET image_path = :imagePath WHERE id = :id")
    abstract void setImagePath(int id, String imagePath);

    @Query("UPDATE pages SET next_page_id = :nextPageId WHERE id = :id")
    abstract void setNextPageId(int id, Integer nextPageId);

    @Query("SELECT * FROM pages")
    abstract List<Page> getAll();

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    abstract Page getById(int id);

}
