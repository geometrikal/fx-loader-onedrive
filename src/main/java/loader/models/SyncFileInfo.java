package loader.models;

public class SyncFileInfo {

    public String unique_id = "";
    public String name = "";
    public long size = 0;
    public String shareURL = "";

    public SyncFileInfo() {

    }

    public SyncFileInfo(String unique_id, String name, long size, String shareURL) {
        this.unique_id = unique_id;
        this.name = name;
        this.size = size;
        this.shareURL = shareURL;
    }
}
