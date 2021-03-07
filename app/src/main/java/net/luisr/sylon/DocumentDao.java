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

    @Query("UPDATE documents SET name = :sName WHERE id = :sID")
    void update(int sID, String sName);

    @Query("SELECT * FROM documents")
    List<Document> getAll();

    @Query("SELECT * FROM documents WHERE id = :sID LIMIT 1")
    Document getById(int sID);

}
