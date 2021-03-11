package net.luisr.sylon.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Page. Class that defines the structure of the pages table in the AppDatabase.
 * @see AppDatabase
 */
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

    /**
     * Constructor for Page.
     * @param documentId the ID of the parent document.
     */
    public Page(int documentId) {
        this.documentId = documentId;
    }

    /** Unique ID of the Page. */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /** The URI of the image associated with the page. */
    @ColumnInfo(name = "image_uri")
    private String imageUri;

    /** The ID of the parent document. */
    @ColumnInfo(name = "document_id")
    private int documentId;

    /** The number of the page in the document. Should be unique in each document. */
    @ColumnInfo(name = "page_number")
    private int pageNumber;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
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
    @NonNull
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", documentId=" + documentId +
                '}';
    }
}
