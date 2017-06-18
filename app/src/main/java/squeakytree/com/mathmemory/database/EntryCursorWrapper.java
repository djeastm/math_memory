package squeakytree.com.mathmemory.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import squeakytree.com.mathmemory.Entry;
import squeakytree.com.mathmemory.database.EntryDbSchema.EntryTable;

/**
 * Created by Dave on 12/21/2015.
 */
public class EntryCursorWrapper extends CursorWrapper {

    public EntryCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Entry getEntry() {
        String uuidString = getString(getColumnIndex(EntryTable.Cols.UUID));
        String title = getString(getColumnIndex(EntryTable.Cols.TITLE));
        String typeEntry = getString(getColumnIndex(EntryTable.Cols.TYPE_ENTRY));
        long date = getLong(getColumnIndex(EntryTable.Cols.DATE));
        int audioCount = getInt(getColumnIndex(EntryTable.Cols.AUDIO_COUNT));

        List<String> definitions = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.DEFINITIONS)));
        List<String> properties = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.PROPERTIES)));
        List<String> theorems = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.THEOREMS)));
        List<String> formulas = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.FORMULAS)));
        List<String> methods = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.METHODS)));
        List<String> intuitions = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.INTUITIONS)));
        List<String> proofs = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.PROOFS)));
        List<String> graphs = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.GRAPHS)));
        List<String> examples = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.EXAMPLES)));
        List<String> nonexamples = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.NONEXAMPLES)));
        List<String> notes = convertFromSQLString(getString(getColumnIndex(EntryTable.Cols.NOTES)));

        Entry entry = new Entry(UUID.fromString(uuidString));
        entry.setTitle(title);
        entry.setTypeEntry(typeEntry);
        entry.setDate(new Date(date));
        entry.setAudioCount(audioCount);

        entry.setDefinitionAudioFiles(definitions);
        entry.setPropertyAudioFiles(properties);
        entry.setTheoremAudioFiles(theorems);
        entry.setFormulaAudioFiles(formulas);
        entry.setMethodAudioFiles(methods);
        entry.setIntuitionAudioFiles(intuitions);
        entry.setProofAudioFiles(proofs);
        entry.setGraphAudioFiles(graphs);
        entry.setExampleAudioFiles(examples);
        entry.setNonExampleAudioFiles(nonexamples);
        entry.setNoteAudioFiles(notes);

        return entry;
    }

    private List<String> convertFromSQLString(String sqlString) {
        if (sqlString != null) {
            List<String> array = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONObject(sqlString).getJSONArray("itemList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    array.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return array;
        }
        else return new ArrayList<>();
    }

}
