package net.luisr.sylon.db;

import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Defines the structure of the pages table in the {@link AppDatabase}.
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

    /** The URI of the thumbnail of the image associated with the page. */
    @ColumnInfo(name = "thumb_uri")
    private String thumbUri;

    /** The ID of the parent document. */
    @ColumnInfo(name = "document_id")
    private int documentId;

    /** The number of the page in the document. Should be unique in each document. */
    @ColumnInfo(name = "page_number")
    private int pageNumber;

    /* Coordinates of crop corners in intrinsic pixels */
    @ColumnInfo(name = "crop_corner_0_X") private int cropCorner0X;
    @ColumnInfo(name = "crop_corner_0_Y") private int cropCorner0Y;
    @ColumnInfo(name = "crop_corner_1_X") private int cropCorner1X;
    @ColumnInfo(name = "crop_corner_1_Y") private int cropCorner1Y;
    @ColumnInfo(name = "crop_corner_2_X") private int cropCorner2X;
    @ColumnInfo(name = "crop_corner_2_Y") private int cropCorner2Y;
    @ColumnInfo(name = "crop_corner_3_X") private int cropCorner3X;
    @ColumnInfo(name = "crop_corner_3_Y") private int cropCorner3Y;

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

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(String thumbUri) {
        this.thumbUri = thumbUri;
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

    public Point[] getCropCorners() {
        return new Point[] {
                new Point(cropCorner0X, cropCorner0Y),
                new Point(cropCorner1X, cropCorner1Y),
                new Point(cropCorner2X, cropCorner2Y),
                new Point(cropCorner3X, cropCorner3Y)
        };
    }

    public int getCropCorner0X() {
        return cropCorner0X;
    }
    public int getCropCorner0Y() {
        return cropCorner0Y;
    }
    public int getCropCorner1X() {
        return cropCorner1X;
    }
    public int getCropCorner1Y() {
        return cropCorner1Y;
    }
    public int getCropCorner2X() {
        return cropCorner2X;
    }
    public int getCropCorner2Y() {
        return cropCorner2Y;
    }
    public int getCropCorner3X() {
        return cropCorner3X;
    }
    public int getCropCorner3Y() {
        return cropCorner3Y;
    }

    public void setCropCorners(Point[] c) {
        cropCorner0X = c[0].x;
        cropCorner0Y = c[0].y;
        cropCorner1X = c[1].x;
        cropCorner1Y = c[1].y;
        cropCorner2X = c[2].x;
        cropCorner2Y = c[2].y;
        cropCorner3X = c[3].x;
        cropCorner3Y = c[3].y;
    }


    public void setCropCorner0X(int cropCorner0X) {
        this.cropCorner0X = cropCorner0X;
    }
    public void setCropCorner0Y(int cropCorner0Y) {
        this.cropCorner0Y = cropCorner0Y;
    }
    public void setCropCorner1X(int cropCorner1X) {
        this.cropCorner1X = cropCorner1X;
    }
    public void setCropCorner1Y(int cropCorner1Y) {
        this.cropCorner1Y = cropCorner1Y;
    }
    public void setCropCorner2X(int cropCorner2X) {
        this.cropCorner2X = cropCorner2X;
    }
    public void setCropCorner2Y(int cropCorner2Y) {
        this.cropCorner2Y = cropCorner2Y;
    }
    public void setCropCorner3X(int cropCorner3X) {
        this.cropCorner3X = cropCorner3X;
    }
    public void setCropCorner3Y(int cropCorner3Y) {
        this.cropCorner3Y = cropCorner3Y;
    }
}
