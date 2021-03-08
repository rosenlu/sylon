package net.luisr.sylon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DocumentDao {

    @Insert(onConflict = REPLACE)
    void insert(Document document);

    @Delete
    void delete(Document document);

    @Query("UPDATE documents SET name = :name WHERE id = :id")
    void setName(int id, String name);

    @Query("SELECT * FROM documents")
    List<Document> getAll();

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    Document getById(int id);

    @Query("SELECT * FROM pages WHERE document_id = :documentId")
    List<Page> getPages(int documentId);

    @Query("SELECT COUNT(*) FROM pages WHERE document_id = :documentId")
    int getNumberOfPages(int documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND next_page_id ISNULL LIMIT 1")
    Page getLastPage(int documentId);

}
