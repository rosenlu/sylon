package net.luisr.sylon.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface PageDao {

    @Insert(onConflict = REPLACE)
    long insert(Page page);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND next_page_id ISNULL LIMIT 1")
    Page getLastPage(int documentId);

    @Delete
    void delete(Page page);

    @Query("UPDATE pages SET image_path = :imagePath WHERE id = :id")
    void setImagePath(int id, String imagePath);

    @Query("UPDATE pages SET next_page_id = :nextPageId WHERE id = :id")
    void setNextPageId(int id, Integer nextPageId);

    @Query("SELECT * FROM pages")
    List<Page> getAll();

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    Page getById(int id);

    @Query("SELECT COUNT(*) FROM pages WHERE document_id = :documentId")
    int getNumberOfPagesInDocument(int documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND next_page_id ISNULL LIMIT 1")
    Page getLastPageInDocument(int documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId")
    List<Page> getPagesInDocument(int documentId);

    @Query("SELECT id FROM pages WHERE document_id = :documentId")
    List<Integer> getIDsOfPagesInDocument(int documentId);

    @Query("SELECT next_page_id FROM pages WHERE document_id = :documentId")
    List<Integer> getNextIDsOfPagesInDocument(int documentId);

}
