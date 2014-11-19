package org.aslak.github.merge.model;

import java.io.File;

public class LocalStorage {

    private File location;
 
    public LocalStorage(File location) {
        this.location = location;
    }
    
    public File getLocation() {
        return location;
    }
}
