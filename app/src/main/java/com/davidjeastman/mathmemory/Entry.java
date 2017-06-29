package com.davidjeastman.mathmemory;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Dave on 12/19/2015.
 */
public class Entry {
    public static final String TAG = "Entry";

    public static final String ENTRY_TYPE_DEFINITIONS = "Definition";
    public static final String ENTRY_TYPE_THEOREMS = "Theorem";
    public static final String ENTRY_TYPE_FORMULAS = "Formula";
    public static final String ENTRY_TYPE_METHODS = "Method";

    public static final String AUDIO_TYPE_DEFINITIONS = "Definition";
    public static final String AUDIO_TYPE_PROPERTIES = "Property";
    public static final String AUDIO_TYPE_THEOREMS = "Theorem";
    public static final String AUDIO_TYPE_FORMULAS = "Formula";
    public static final String AUDIO_TYPE_METHODS = "Method";
    public static final String AUDIO_TYPE_INTUITIONS = "Intuition";
    public static final String AUDIO_TYPE_PROOFS = "Proof";
    public static final String AUDIO_TYPE_GRAPHS = "Graph";
    public static final String AUDIO_TYPE_EXAMPLES = "Example";
    public static final String AUDIO_TYPE_NONEXAMPLES = "Non-Example";
    public static final String AUDIO_TYPE_NOTES = "Note";

    private UUID mId;
    private String safeId; // file-storage-safe-id
    private String mTitle;
    private String mTypeEntry;
    private Date mDate;
    private int mAudioCount;

    private List<String> mDefinitionAudioFiles;
    private List<String> mPropertyAudioFiles;
    private List<String> mTheoremAudioFiles;
    private List<String> mFormulaAudioFiles;
    private List<String> mMethodAudioFiles;
    private List<String> mIntuitionAudioFiles;
    private List<String> mProofAudioFiles;
    private List<String> mGraphAudioFiles;
    private List<String> mExampleAudioFiles;
    private List<String> mNonExampleAudioFiles;
    private List<String> mNoteAudioFiles;

    private String mTypeAudio;

    public Entry() {
        //Generate unique identifier
        this(UUID.randomUUID());
    }

    public Entry(UUID id) {
        safeId = id.toString().substring(id.toString().length() - 5);
        mAudioCount = 1;
        mId = id;
        mDate = new Date();
    }

    public void addAudio(String fileName) {
        //Log.i(TAG, "addAudio: "+ mTypeAudio);
        switch (mTypeAudio) {
            case AUDIO_TYPE_DEFINITIONS:
                getDefinitionAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_PROPERTIES:
                getPropertyAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_THEOREMS:
                getTheoremAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_FORMULAS:
                getFormulaAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_METHODS:
                getMethodAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_INTUITIONS:
                getIntuitionAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_PROOFS:
                getProofAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_GRAPHS:
                getGraphAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_EXAMPLES:
                getExampleAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_NONEXAMPLES:
                getNonExampleAudioFiles().add(fileName);
                break;
            case AUDIO_TYPE_NOTES:
                getNoteAudioFiles().add(fileName);
                break;
            default:
                break;
        }

        mAudioCount++;
    }

    public void deleteAudio(String typeAudio, String fileName) {
        File fileToDelete = new File (fileName);
        boolean deleted = fileToDelete.delete();
        if (!deleted) Log.i(TAG, "deleteAudio: didn't delete");
        switch (typeAudio) {
            case AUDIO_TYPE_DEFINITIONS:
                getDefinitionAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_PROPERTIES:
                getPropertyAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_THEOREMS:
                getTheoremAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_FORMULAS:
                getFormulaAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_METHODS:
                getMethodAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_INTUITIONS:
                getIntuitionAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_PROOFS:
                getProofAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_GRAPHS:
                getGraphAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_EXAMPLES:
                getExampleAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_NONEXAMPLES:
                getNonExampleAudioFiles().remove(fileName);
                break;
            case AUDIO_TYPE_NOTES:
                getNoteAudioFiles().remove(fileName);
                break;
            default:
                break;
        }
    }

    public String buildSoundFilePathName() {
        String entryID = safeId;
        String entryTitle = getTitle();
        //String entryType = getTypeEntry();
        String audioType = getTypeAudio();

        // Check for invalid character in entryTitle
        if (entryTitle.contains("/")) {
            entryTitle = entryTitle.replace('/', '-');
        }

        return audioType + "_" + entryTitle + "_" + entryID + "_" + mAudioCount + ".3gp";
    }

    public String getPhotoFilename() {
        String entryID = safeId;
        String entryTitle = getTitle();
        //String entryType = getTypeEntry();

        // Check for invalid character in entryTitle
//        if (entryTitle.contains("/")) {
//            entryTitle = entryTitle.replace('/', '-');
//        }

        return "IMG_" + entryID + ".3gp";
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTypeEntry() {
        return mTypeEntry;
    }

    public void setTypeEntry(String type) {
        mTypeEntry = type;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public int getAudioCount() {
        return mAudioCount;
    }


    public void setAudioCount(int audioCount) {
        mAudioCount = audioCount;
    }

    public List<String> getAllSounds() {
        List<String> allSounds = new ArrayList<>();
        allSounds.addAll(getDefinitionAudioFiles());
        allSounds.addAll(getPropertyAudioFiles());
        allSounds.addAll(getTheoremAudioFiles());
        allSounds.addAll(getFormulaAudioFiles());
        allSounds.addAll(getMethodAudioFiles());
        allSounds.addAll(getIntuitionAudioFiles());
        allSounds.addAll(getProofAudioFiles());
        allSounds.addAll(getGraphAudioFiles());
        allSounds.addAll(getExampleAudioFiles());
        allSounds.addAll(getNonExampleAudioFiles());
        allSounds.addAll(getNoteAudioFiles());

        return allSounds;
    }

    public List<String> getDefinitionAudioFiles() {
        return mDefinitionAudioFiles;
    }

    public void setDefinitionAudioFiles(List<String> definitionAudioFiles) {
        mDefinitionAudioFiles = definitionAudioFiles;
    }

    public List<String> getPropertyAudioFiles() {
        return mPropertyAudioFiles;
    }

    public void setPropertyAudioFiles(List<String> propertyAudioFiles) {
        mPropertyAudioFiles = propertyAudioFiles;
    }

    public List<String> getTheoremAudioFiles() {
        return mTheoremAudioFiles;
    }

    public void setTheoremAudioFiles(List<String> theoremAudioFiles) {
        mTheoremAudioFiles = theoremAudioFiles;
    }

    public List<String> getFormulaAudioFiles() {
        return mFormulaAudioFiles;
    }

    public void setFormulaAudioFiles(List<String> formulaAudioFiles) {
        mFormulaAudioFiles = formulaAudioFiles;
    }

    public List<String> getMethodAudioFiles() {
        return mMethodAudioFiles;
    }

    public void setMethodAudioFiles(List<String> methodAudioFiles) {
        mMethodAudioFiles = methodAudioFiles;
    }

    public List<String> getIntuitionAudioFiles() {
        return mIntuitionAudioFiles;
    }

    public void setIntuitionAudioFiles(List<String> intuitionAudioFiles) {
        mIntuitionAudioFiles = intuitionAudioFiles;
    }

    public List<String> getProofAudioFiles() {
        return mProofAudioFiles;
    }

    public void setProofAudioFiles(List<String> proofAudioFiles) {
        mProofAudioFiles = proofAudioFiles;
    }

    public List<String> getGraphAudioFiles() {
        return mGraphAudioFiles;
    }

    public void setGraphAudioFiles(List<String> graphAudioFiles) {
        mGraphAudioFiles = graphAudioFiles;
    }

    public List<String> getExampleAudioFiles() {
        return mExampleAudioFiles;
    }

    public void setExampleAudioFiles(List<String> exampleAudioFiles) {
        mExampleAudioFiles = exampleAudioFiles;
    }

    public List<String> getNonExampleAudioFiles() {
        return mNonExampleAudioFiles;
    }

    public void setNonExampleAudioFiles(List<String> nonExampleAudioFiles) {
        mNonExampleAudioFiles = nonExampleAudioFiles;
    }

    public List<String> getNoteAudioFiles() {
        return mNoteAudioFiles;
    }

    public void setNoteAudioFiles(List<String> noteAudioFiles) {
        mNoteAudioFiles = noteAudioFiles;
    }

    public String getTypeAudio() {
        return mTypeAudio;
    }

    public void setTypeAudio(String typeAudio) {
        this.mTypeAudio = typeAudio;
    }
}
