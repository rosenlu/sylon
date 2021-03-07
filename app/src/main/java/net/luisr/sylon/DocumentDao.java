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

    @Delete
    void reset(List<Document> documentList);

    @Query("UPDATE table_file SET name = :sName WHERE id = :sID")
    void update(int sID, String sName);

    @Query("SELECT * FROM table_file")
    List<Document> getAll();

    @Query("SELECT * FROM table_file WHERE id = :sID LIMIT 1")
    Document getById(int sID);

}
