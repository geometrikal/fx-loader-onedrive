package loader.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OnedriveResourceCollection {

    public HashMap<String, OnedriveResource> resources = new HashMap<>();

    public OnedriveResourceCollection() {

    }

    public void addResource(OnedriveResource resource) {
        resources.put(resource.eTag, resource);
    }

    public ArrayList<OnedriveResource> changedSince(OnedriveResourceCollection collection) {
        ArrayList<OnedriveResource> diffList = new ArrayList<>();
        for (Map.Entry<String, OnedriveResource> entry : this.resources.entrySet()) {
            if (!collection.resources.containsKey(entry.getKey())) {
                diffList.add(entry.getValue());
            }
        }
        return diffList;
    }

    public void summary() {
        for (Map.Entry<String, OnedriveResource> entry : this.resources.entrySet()) {
            String s = String.format("%s:\n- %s\n- %d bytes\n- %s",
                    entry.getValue().eTag,
                    entry.getValue().name,
                    entry.getValue().size,
                    entry.getValue().shareURL);
            System.out.println(s);
        }
    }
}
