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
 * Defines the structure of the pages table in the AppDatabase.
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

    /** Indicate whether a Page instance has been modified or newly created. */
    @Ignore
    private boolean modified, isNew;

    /**
     * Static method to get a new page with it's 'isNew' attribute set to true.
     * @param documentId the ID of the parent document.
     * @return the newly created page.
     */
    public static Page makeNew(int documentId) {
        Page p = new Page(documentId);
        p.setNew();
        return p;
    }

    /**
     * Constructor for Page.
     * @param documentId the ID of the parent document.
     */
    public Page(int documentId) {
        this.documentId = documentId;
        modified = false;
        isNew = false;
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

    /** Set the isNew attribute of the Page to true. */
    private void setNew() {
        isNew = true;
    }

    /** Set the isNew attribute of the Page to false. */
    public void clearNew() {
        isNew = false;
    }

    /** Getter for the 'isNew' attribute */
    public boolean isNew() {
        return isNew;
    }

    /** Setter for the 'modified' attribute */
    public void setModified(boolean value) {
        modified = value;
    }

    /** Getter for the 'modified' attribute */
    public boolean hasBeenModified() {
        return modified;
    }

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
