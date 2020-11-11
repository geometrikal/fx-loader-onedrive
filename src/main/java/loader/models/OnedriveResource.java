package loader.models;

public class OnedriveResource {

    public String eTag = "";
    public String name = "";
    public long size = 0;
    public String shareURL = "";

    public OnedriveResource() {

    }

    public OnedriveResource(String eTag, String name, long size, String shareURL) {
        this.eTag = eTag;
        this.name = name;
        this.size = size;
        this.shareURL = shareURL;
    }
}
