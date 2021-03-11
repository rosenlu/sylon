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
                @ForeignKey(entity = Page.class, parentColumns = "id", childColumns = "next_page_id")
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

    @ColumnInfo(name = "next_page_id")
    private Integer nextPageId;

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

    public Integer getNextPageId() {
        return nextPageId;
    }

    public void setNextPageId(Integer nextPageId) {
        this.nextPageId = nextPageId;
    }

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", documentId=" + documentId +
                '}';
    }
}
