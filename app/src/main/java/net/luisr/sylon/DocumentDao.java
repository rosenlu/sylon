package net.luisr.sylon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class DocumentDao {

    @Insert(onConflict = REPLACE)
    abstract void insert(Document document);

    @Delete
    abstract void delete(Document document);

    @Query("UPDATE documents SET name = :name WHERE id = :id")
    abstract void setName(int id, String name);

    @Query("SELECT * FROM documents")
    abstract List<Document> getAll();

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    abstract Document getById(int id);

    @Query("SELECT * FROM pages WHERE document_id = :documentId")
    abstract List<Page> getPages(int documentId);

    @Query("SELECT COUNT(*) FROM pages WHERE document_id = :documentId")
    abstract int getNumberOfPages(int documentId);

    @Query("SELECT * FROM pages WHERE document_id = :documentId AND next_page_id ISNULL LIMIT 1")
    abstract Page getLastPage(int documentId);

}
