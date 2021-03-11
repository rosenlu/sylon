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

    @Delete
    void delete(Page page);

    @Query("UPDATE pages SET image_path = :imagePath WHERE id = :id")
    void setImagePath(int id, String imagePath);

    @Query("UPDATE pages SET page_number = :pageNumber WHERE id = :id")
    void setPageNumber(int id, Integer pageNumber);

    @Query("SELECT * FROM pages")
    List<Page> getAll();

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    Page getById(int id);

    @Query("SELECT COUNT(*) FROM pages WHERE document_id = :documentId")
    int getNumberOfPagesInDocument(int documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId ORDER BY page_number ASC")
    List<Page> getPagesInDocument(int documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND page_number = 1 LIMIT 1")
    Page getFirstPageInDocument(int documentId);

    @Query("UPDATE pages SET page_number = page_number + 1 WHERE document_id = :documentId AND page_number >= :startPageNumber AND page_number < :endPageNumber")
    void incrementPageNumbersInDocumentByOne(int documentId, int startPageNumber, int endPageNumber);

    @Query("UPDATE pages SET page_number = page_number - 1 WHERE document_id = :documentId AND page_number >= :startPageNumber AND page_number < :endPageNumber")
    void reducePageNumbersInDocumentByOne(int documentId, int startPageNumber, int endPageNumber);
}
