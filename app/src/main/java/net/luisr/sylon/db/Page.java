package net.luisr.sylon.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "pages",
        foreignKeys = {
                @ForeignKey(entity = Document.class, parentColumns = "id", childColumns = "document_id", onDelete = ForeignKey.CASCADE),
        },
        indices = {
                @Index("document_id"),
        }
)
public class Page implements Serializable {

    public Page(int documentId) {
        this.documentId = documentId;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "document_id")
    private int documentId;

    @ColumnInfo(name = "page_number")
    private int pageNumber;

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

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", documentId=" + documentId +
                '}';
    }
}
