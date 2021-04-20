package net.luisr.sylon.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * The Data Access Object ({@link Dao}) for the {@link Page} class.
 * @see AppDatabase
 */
@Dao
public interface PageDao {

    /**
     * Insert one or more {@link Page} entities into the database.
     * @param pages the Page(s) to insert.
     * @return the ID of the newly added Page.
     */
    @Insert(onConflict = REPLACE)
    long[] insert(Page... pages);

    /**
     * Delete one or more {@link Page} entities from the database.
     * @param pages the Page(s) to delete.
     */
    @Delete
    void delete(Page... pages);

    /**
     * Update one or more {@link Page} entities in the database.
     * @param pages the Page(s) to update.
     */
    @Update
    void update(Page... pages);

    /**
     * Get a {@link Page} entity by its ID.
     * @param id the ID of the Page.
     * @return the Page.
     */
    @Query("SELECT * FROM pages WHERE id=:id LIMIT 1")
    Page getById(int id);

    /**
     * Get the number of {@link Page} entities inside a {@link Document}.
     * @param documentId the ID of the Document.
     * @return the number of Pages in the Document.
     */
    @Query("SELECT COUNT(*) FROM pages WHERE document_id = :documentId")
    int getNumberOfPagesInDocument(int documentId);

    /**
     * Get all {@link Page} entities in a {@link Document}.
     * @param documentId the ID of the Document.
     * @return a list of Pages in the Document.
     */
    @Query("SELECT * FROM pages WHERE document_id = :documentId ORDER BY page_number ASC")
    List<Page> getPagesInDocument(int documentId);

    /**
     * Get the first {@link Page} in a {@link Document}.
     * @param documentId the ID of the Document.
     * @return the first Page in the Document.
     */
    @Query("SELECT * FROM pages WHERE document_id = :documentId AND page_number = 0 LIMIT 1")
    Page getFirstPageInDocument(int documentId);

}
