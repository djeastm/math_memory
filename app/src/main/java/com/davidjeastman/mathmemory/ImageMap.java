package com.davidjeastman.mathmemory;

/**
 * Created by David Eastman on 8/28/2017.
 */

public class ImageMap {
    private int key;
    private String path;

    public ImageMap(int k, String p) {
        key = k;
        path = p;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
