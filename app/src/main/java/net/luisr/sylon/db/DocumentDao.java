package net.luisr.sylon.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * The Data Access Object (DAO) for the Document class.
 * @see Document
 * @see AppDatabase
 */
@Dao
public interface DocumentDao {

    /**
     * Insert a Document entity to the database.
     * @param document the Document to insert.
     * @return the ID of the newly added Document.
     */
    @Insert(onConflict = REPLACE)
    long insert(Document document);

    /**
     * Delete a Document entity in the database.
     * @param document the Document to delete.
     */
    @Delete
    void delete(Document document);

    /**
     * Get all Documents in the documents table.
     * @return a List of all Documents.
     */
    @Query("SELECT * FROM documents")
    List<Document> getAll();

    /**
     * Get a Document entity by its ID.
     * @param id the ID of the Document.
     * @return the Document.
     */
    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    Document getById(int id);

    /**
     * Update the name of a Document in the database.
     * @param id the ID of the Document.
     * @param name the new name of the Document.
     */
    @Query("UPDATE documents SET name = :name WHERE id = :id")
    void setName(int id, String name);
}
