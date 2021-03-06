package net.luisr.sylon;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SylonFileDao {

    @Insert(onConflict = REPLACE)
    void insert(SylonFile sylonFile);

    @Delete
    void delete(SylonFile sylonFile);

    @Delete
    void reset(List<SylonFile> sylonFileList);

    @Query("UPDATE table_file SET name = :sName WHERE id = :sID")
    void update(int sID, String sName);

    @Query("SELECT * FROM table_file")
    List<SylonFile> getAll();

    @Query("SELECT * FROM table_file WHERE id = :sID LIMIT 1")
    SylonFile getById(int sID);

}
