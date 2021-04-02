package net.luisr.sylon.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * The Data Access Object ({@link Dao}) for the {@link Document} class.
 * @see AppDatabase
 */
@Dao
public interface DocumentDao {

    /**
     * Insert a {@link Document}  entity to the database.
     * @param document the Document to insert.
     * @return the ID of the newly added Document.
     */
    @Insert(onConflict = REPLACE)
    long insert(Document document);

    /**
     * Delete a {@link Document}  entity in the database.
     * @param document the Document to delete.
     */
    @Delete
    void delete(Document document);

    /**
     * Get all {@link Document} entities in the documents table.
     * @return a List of all Documents.
     */
    @Query("SELECT * FROM documents")
    List<Document> getAll();

    /**
     * Get a {@link Document} entity by its ID.
     * @param id the ID of the Document.
     * @return the Document.
     */
    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    Document getById(int id);

    /**
     * Update the name of a {@link Document} in the database.
     * @param id the ID of the Document.
     * @param name the new name of the Document.
     */
    @Query("UPDATE documents SET name = :name WHERE id = :id")
    void setName(int id, String name);
}
