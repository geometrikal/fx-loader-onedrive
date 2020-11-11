package loader.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import loader.Main;
import loader.models.Cache;
import loader.models.OnedriveResource;
import loader.models.OnedriveResourceCollection;

import java.io.FileNotFoundException;
import java.util.*;

public class Loader {

    public static Service<Void> syncService(Cache cache) {
        return new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        updateMessage("Resolve cache directory"); Thread.sleep(1000);
                        cache.createOrResolveCacheDir();
                        updateMessage("Load local manifest"); Thread.sleep(1000);
                        String localManifest = cache.getLocalManifest();
                        OnedriveResourceCollection localResourceCollection;
                        if (localManifest != null) {
                            updateMessage("Load local resources"); Thread.sleep(1000);
                            localResourceCollection = cache.parseResourceManifest(localManifest);
                        }
                        else {
                            updateMessage("No local resources"); Thread.sleep(1000);
                            localResourceCollection = new OnedriveResourceCollection();
                        }
                        updateMessage("Load remote manifest"); Thread.sleep(1000);
                        String remoteManifest = cache.getRemoteManifest();
                        updateMessage("Load remote resources"); Thread.sleep(1000);
                        OnedriveResourceCollection remoteResourceCollection = null;
                        if (remoteManifest != null) {
                            remoteResourceCollection = cache.parseResourceManifest(remoteManifest);
                        }
                        // No resource is present
                        if (localManifest == null && remoteManifest == null) {
                            throw new FileNotFoundException("Local copy of program not found, and could not connect to remote OneDrive copy. Please check your internet and try again.");
                        }
                        // Remote exists! Compare and download missing files
                        if (remoteResourceCollection != null) {
                            List<OnedriveResource> resourcesToDownload = remoteResourceCollection.changedSince(localResourceCollection);
                            if (resourcesToDownload.size() == 0) {
                                updateMessage("Already up to date!"); Thread.sleep(1000);
                            }
                            else {
                                updateMessage(String.format("Downloading %d resources...", resourcesToDownload.size())); Thread.sleep(1000);
                                for (OnedriveResource r : resourcesToDownload) {
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
