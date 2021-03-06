package com.davidjeastman.mathmemory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.davidjeastman.mathmemory.database.EntryBaseHelper;
import com.davidjeastman.mathmemory.database.EntryCursorWrapper;
import com.davidjeastman.mathmemory.database.EntryDbSchema.EntryTable;

/**
 * Created by Dave on 12/21/2015.
 */
public class Library {
    public static final String APP_NAME = "MathMemory";
    public static final String APP_DIRECTORY = APP_NAME;

    private static Library sLibrary;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private Library(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new EntryBaseHelper(mContext)
                .getWritableDatabase();
    }

    public static Library get(Context context) {
        if (sLibrary == null) {
            sLibrary = new Library(context);
        }
        return sLibrary;
    }

    private static ContentValues getContentValues(Entry entry) {
        ContentValues values = new ContentValues();
        values.put(EntryTable.Cols.UUID, entry.getId().toString());
        values.put(EntryTable.Cols.TITLE, entry.getTitle());
        values.put(EntryTable.Cols.TYPE_ENTRY, entry.getTypeEntry());
        values.put(EntryTable.Cols.DATE, entry.getDate().getTime());
        values.put(EntryTable.Cols.IMAGE_COUNT, entry.getImageCounter());
        values.put(EntryTable.Cols.AUDIO_COUNT, entry.getAudioCount());

        List<String> imagesRaw = new ArrayList<>();
        if (entry.getImages() != null) {
            for (ImageMap i : entry.getImages()) {
                String str = i.getKey() + ":::" + i.getPath();
                imagesRaw.add(str);
            }
        }
        values.put(EntryTable.Cols.IMAGES, convertToJSON(imagesRaw));

        values.put(EntryTable.Cols.DEFINITIONS, convertToJSON(entry.getDefinitionAudioFiles()));
        values.put(EntryTable.Cols.PROPERTIES, convertToJSON(entry.getPropertyAudioFiles()));
        values.put(EntryTable.Cols.THEOREMS, convertToJSON(entry.getTheoremAudioFiles()));
        values.put(EntryTable.Cols.PROPOSITIONS, convertToJSON(entry.getPropositionAudioFiles()));
        values.put(EntryTable.Cols.FORMULAS, convertToJSON(entry.getFormulaAudioFiles()));
        values.put(EntryTable.Cols.METHODS, convertToJSON(entry.getMethodAudioFiles()));
        values.put(EntryTable.Cols.INTUITIONS, convertToJSON(entry.getIntuitionAudioFiles()));
        values.put(EntryTable.Cols.PROOFS, convertToJSON(entry.getProofAudioFiles()));
        values.put(EntryTable.Cols.GRAPHS, convertToJSON(entry.getGraphAudioFiles()));
        values.put(EntryTable.Cols.EXAMPLES, convertToJSON(entry.getExampleAudioFiles()));
        values.put(EntryTable.Cols.NONEXAMPLES, convertToJSON(entry.getNonExampleAudioFiles()));
        values.put(EntryTable.Cols.NOTES, convertToJSON(entry.getNoteAudioFiles()));

        return values;
    }

    private static String convertToJSON(List<String> list) {
        if (list != null) {
            JSONObject json = new JSONObject();
            try {
                json.put("itemList", new JSONArray(list));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        return null;
    }

    public void addEntry(Entry e) {
        ContentValues values = getContentValues(e);
        mDatabase.insert(EntryTable.NAME, null, values);
    }

    public void deleteEntry(Entry c) {
        String uuidString = c.getId().toString();
        mDatabase.delete(EntryTable.NAME, EntryTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public List<Entry> getEntries() {
        return getEntries(null);
    }

    public List<Entry> getEntries(String searchTerm) {
        String queryWhereClause;

        List<Entry> entries = new ArrayList<>();

        if (searchTerm != null) {
            queryWhereClause = "TITLE LIKE '%" + searchTerm + "%'";
        } else {
            queryWhereClause = null;
        }

        EntryCursorWrapper cursor = queryEntries(queryWhereClause, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                entries.add(cursor.getEntry());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return entries;
    }

    public Entry getEntry(UUID id) {
        EntryCursorWrapper cursor = queryEntries(
                EntryTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getEntry();
        } finally {
            cursor.close();
        }
    }

    public void updateEntry(Entry entry) {
        String uuidString = entry.getId().toString();
        ContentValues values = getContentValues(entry);
        mDatabase.update(EntryTable.NAME, values,
                EntryTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private EntryCursorWrapper queryEntries(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                EntryTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                "TITLE" // orderBy
        );
        return new EntryCursorWrapper(cursor);
    }

    public List<File> getPhotoFiles(Entry entry) {
        List<File> files = new ArrayList<>();
        File filesDir = mContext.getFilesDir();
        for (ImageMap i : entry.getImages()) {
            File f = new File(filesDir, entry.buildPhotoFilename(i.getKey()));
            files.add(f);
        }
        return files;
    }
}
