package net.luisr.sylon;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Document.class, Page.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase database;
    private final static String DATABASE_NAME = "database";

    public synchronized static AppDatabase getInstance(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract DocumentDao docDao();
    public abstract PageDao pageDao();

}
