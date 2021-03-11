package net.luisr.sylon.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * The Data Access Object (DAO) for the Page class.
 * @see Page
 * @see AppDatabase
 */
@Dao
public interface PageDao {

    /**
     * Insert one or more Page entities into the database.
     * @param pages the Page(s) to insert.
     * @return the ID of the newly added Page.
     */
    @Insert(onConflict = REPLACE)
    long[] insert(Page... pages);

    /**
     * Delete one or more Page entities from the database.
     * @param pages the Page(s) to delete.
     */
    @Delete
    void delete(Page... pages);

    @Update
    void update(Page... pages);

    /**
     * Get the number of Pages inside a Document.
     * @param documentId the ID of the Document.
     * @return the number of Pages in the Document.
     */
    @Query("SELECT COUNT(*) FROM pages WHERE document_id = :documentId")
    int getNumberOfPagesInDocument(int documentId);

    /**
     * Get all Pages in a Document.
     * @param documentId the ID of the Document.
     * @return a list of Pages in the Document.
     */
    @Query("SELECT * FROM pages WHERE document_id = :documentId ORDER BY page_number ASC")
    List<Page> getPagesInDocument(int documentId);

    /**
     * Get the first Page in a Document.
     * @param documentId the ID of the Document.
     * @return the first Page in the Document.
     */
    @Query("SELECT * FROM pages WHERE document_id = :documentId AND page_number = 0 LIMIT 1")
    Page getFirstPageInDocument(int documentId);

}
