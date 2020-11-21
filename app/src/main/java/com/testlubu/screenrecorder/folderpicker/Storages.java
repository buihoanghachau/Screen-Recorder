package com.testlubu.screenrecorder.folderpicker;

public class Storages {
    private String path;
    private StorageType type;

    public enum StorageType {
        Internal,
        External
    }

    public Storages(String str, StorageType storageType) {
        this.path = str;
        this.type = storageType;
    }

    public String getPath() {
        return this.path;
    }

    public StorageType getType() {
        return this.type;
    }
}
