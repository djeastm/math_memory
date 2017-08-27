package com.davidjeastman.mathmemory.database;

/**
 * Created by Dave on 12/21/2015.
 */
public class EntryDbSchema {

    public static final class EntryTable {
        public static final String NAME = "entries";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String TYPE_ENTRY = "type_entry";
            public static final String DATE = "date";
            public static final String IMAGE_COUNT = "image_count";
            public static final String AUDIO_COUNT = "audio_count";

            public static final String IMAGES = "images";

            public static final String DEFINITIONS = "definitions";
            public static final String PROPERTIES = "properties";
            public static final String THEOREMS = "theorems";
            public static final String PROPOSITIONS = "propositions";
            public static final String FORMULAS = "formulas";
            public static final String METHODS = "methods";
            public static final String INTUITIONS = "intuitions";
            public static final String PROOFS = "proofs";
            public static final String GRAPHS = "graphs";
            public static final String EXAMPLES = "examples";
            public static final String NONEXAMPLES = "nonexamples";
            public static final String NOTES = "notes";
        }
    }
}
