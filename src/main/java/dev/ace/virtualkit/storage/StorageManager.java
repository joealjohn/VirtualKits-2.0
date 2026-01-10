package dev.ace.virtualkit.storage;

import dev.ace.virtualkit.storage.exceptions.StorageConnectionException;
import dev.ace.virtualkit.storage.exceptions.StorageOperationException;

public interface StorageManager {


    boolean isConnected();

    void connect() throws StorageConnectionException;

    void init() throws StorageOperationException;

    void close() throws StorageConnectionException;

    void keepAlive() throws StorageConnectionException;

    void saveKitDataByID(String kitID, String data);

    String getKitDataByID(String kitID);

    boolean doesKitExistByID(String kitID);

    void deleteKitByID(String kitID);

}


