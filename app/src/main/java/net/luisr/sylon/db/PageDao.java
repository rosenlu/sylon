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
     * Get a Page entity by its ID.
     * @param id the ID of the page.
     * @return the Page.
     */
    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    Page getById(int id);

    /**
     * Update the URI of the image associated with the Page.
     * @param id the ID of the page.
     * @param imageUri the new URI of the image.
     */
    @Query("UPDATE pages SET image_uri = :imageUri WHERE id = :id")
    void setImageUri(int id, String imageUri);

    /**
     * Update the number of a page in the document.
     * @param id the ID of the page.
     * @param pageNumber the new number of the page.
     */
    @Query("UPDATE pages SET page_number = :pageNumber WHERE id = :id")
    void setPageNumber(int id, Integer pageNumber);

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

    /**
     * Increment the page numbers of Pages in a Document between two positions by one.
     * Convenience function used to update page numbers when moving a page to a lower position.
     * @param documentId the ID of the Document.
     * @param fromPosition the position from which the Page is moved.
     * @param toPosition the position to which the Page is moved.
     */
    @Query("UPDATE pages SET page_number = page_number + 1 WHERE document_id = :documentId AND page_number >= :toPosition AND page_number < :fromPosition")
    void incrementPageNumbersInDocumentByOne(int documentId, int fromPosition, int toPosition);

    /**
     * Reduce the page numbers of Pages in a Document between two positions by one.
     * Convenience function used to update page numbers when moving a page to a higher position.
     * @param documentId the ID of the Document.
     * @param fromPosition the position from which the Page is moved.
     * @param toPosition the position to which the Page is moved.
     */
    @Query("UPDATE pages SET page_number = page_number - 1 WHERE document_id = :documentId AND page_number > :fromPosition AND page_number <= :toPosition")
    void reducePageNumbersInDocumentByOne(int documentId, int fromPosition, int toPosition);
}
