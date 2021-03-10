package net.luisr.sylon.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "documents")
public class Document implements Serializable {

    public Document(@NonNull String name) {
        // TODO: instead of simply deleting all illegal chars, show message to user when filename
        //       contains illegal chars and deactivate "add" button (also in setName method below)
        this.name = name.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public String getPath() {
        return name + ".pdf";
    }

    public void setName(String name) {
        this.name = name.replaceAll("[\\\\/:*?\"<>|]", "");
    }
}
