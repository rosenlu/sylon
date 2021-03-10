package net.luisr.sylon.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The app's database.
 *
 * The database has two tables, documents and pages, that are related in a one-to-many relationship.
 *
 * Each of the tables has it's own DAO, docDao and pageDao, that is responsible for getting and
 * setting data in the respective table.
 */
@Database(entities = {Document.class, Page.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /** an instance of AppDatabase */
    private static AppDatabase database;

    /** the name of the database */
    private final static String DATABASE_NAME = "database";

    /**
     * Gets the instance of AppDatabase.
     * @param context a context, usually the activity you are currently in
     * @return an instance of AppDatabase
     */
    public synchronized static AppDatabase getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    /** the DAO for the documents table */
    public abstract DocumentDao docDao();

    /** the DAO for the pages table */
    public abstract PageDao pageDao();

}
