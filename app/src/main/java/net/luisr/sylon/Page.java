package net.luisr.sylon;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "pages")
public class Page implements Serializable {

    public Page(int documentId) {
        this.documentId = documentId;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ForeignKey(entity = Document.class, parentColumns =  "id", childColumns = "document_id", onDelete = ForeignKey.CASCADE)
    @ColumnInfo(name = "document_id")
    private int documentId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

}
