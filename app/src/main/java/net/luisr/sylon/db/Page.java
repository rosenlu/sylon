package net.luisr.sylon.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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

    @Ignore
    private boolean modified, isNew;

    /**
     * Constructor for Page.
     * @param documentId the ID of the parent document.
     */
    public Page(int documentId) {
        this.documentId = documentId;
        modified = false;
        isNew = false;
    }

    public static Page makeNew(int documentId) {
        Page p = new Page(documentId);
        p.setNew();
        return p;
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

    private void setNew() {
        isNew = true;
    }
    public void clearNew() {
        isNew = false;
    }

    public boolean getNew() {
        return isNew;
    }

    public void setModified(boolean value) {
        modified = value;
    }

    public boolean wasModified() {
        return modified;
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
