package loader.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import loader.Main;
import loader.models.Cache;
import loader.models.Remote;
import loader.models.SyncFileInfo;
import loader.models.SyncFileInfoCollection;

import java.io.FileNotFoundException;
import java.util.*;

public class Synchronise {

    public static Service<Void> syncService(Cache cache, Remote remote) {
        return new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Get manifest
                        updateMessage("Load remote manifest"); Thread.sleep(1000);
                        remote.getManifest();

                        // Parse manifest
                        updateMessage("Parse remote manifest"); Thread.sleep(1000);
                        remote.parseManifest();

                        // Remote exists! Compare and download missing files
                        if (remoteResourceCollection != null) {
                            List<SyncFileInfo> resourcesToDownload = remote.resourceCollection.difference(cache.resourceCollection);
                            if (resourcesToDownload.size() == 0) {
                                updateMessage("Already up to date!"); Thread.sleep(1000);
                            }
                            else {
                                updateMessage(String.format("Downloading %d resources...", resourcesToDownload.size())); Thread.sleep(1000);
                                for (SyncFileInfo r : resourcesToDownload) {
                                    System.out.println(r.shareURL);
                                    updateMessage(r.name);
                                    cache.downloadResource(r, this::updateProgress);
                                }
                            }
                            localResourceCollection = remoteResourceCollection;
                            cache.writeLocalManifest(remoteManifest);
                        }
                        // Start the program
                        updateMessage("Starting application..."); Thread.sleep(1000);
                        cache.startApplication(localResourceCollection, Main.getInstance().stage);
                        return null;
                    }
                };
            }
        };
    }
}
