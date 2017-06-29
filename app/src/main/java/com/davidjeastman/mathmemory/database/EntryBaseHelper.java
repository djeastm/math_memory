package com.davidjeastman.mathmemory.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

import com.davidjeastman.mathmemory.Library;
import com.davidjeastman.mathmemory.database.EntryDbSchema.EntryTable;

/**
 * Created by Dave on 12/21/2015.
 */
public class EntryBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String FILE_DIR = Library.APP_DIRECTORY;
    private static final String DATABASE_NAME = "entryBase.db";

    public EntryBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //Environment.getExternalStorageDirectory() + File.separator + FILE_DIR + File.separator +

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + EntryTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                EntryTable.Cols.UUID + ", " +
                EntryTable.Cols.TITLE + ", " +
                EntryTable.Cols.TYPE_ENTRY + ", " +
                EntryTable.Cols.DATE + ", " +
                EntryTable.Cols.AUDIO_COUNT + ", " +

                EntryTable.Cols.DEFINITIONS + ", " +
                EntryTable.Cols.PROPERTIES + ", " +
                EntryTable.Cols.THEOREMS + ", " +
                EntryTable.Cols.FORMULAS + ", " +
                EntryTable.Cols.METHODS + ", " +
                EntryTable.Cols.INTUITIONS + ", " +
                EntryTable.Cols.PROOFS + ", " +
                EntryTable.Cols.GRAPHS + ", " +
                EntryTable.Cols.EXAMPLES + ", " +
                EntryTable.Cols.NONEXAMPLES + ", " +
                EntryTable.Cols.NOTES +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
