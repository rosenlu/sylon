package net.luisr.sylon.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Defines the structure of the documents table in the {@link AppDatabase}.
 */
@Entity(tableName = "documents")
public class Document implements Serializable {

    /**
     * Constructor for Document.
     * @param name The name of the document.
     */
    public Document(@NonNull String name) {
        // TODO: instead of simply deleting all illegal chars, show message to user when filename
        //       contains illegal chars and deactivate "add" button (also in setName method below)
        this.name = name.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    /** Unique ID of the Document. */
    @PrimaryKey(autoGenerate = true)
    private int id;

    /** The name of the Document. */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /**
     * Get the path at which the final PDF file is saved
     * @return the path of the PDF, relative to the pdfDirectory (i.e. only the file name)
     */
    public String getPath() {
        return name + ".pdf";
    }

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

    public void setName(String name) {
        this.name = name.replaceAll("[\\\\/:*?\"<>|]", "");
    }

    @Override
    @NonNull
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
