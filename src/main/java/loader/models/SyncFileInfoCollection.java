package loader.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.security.util.Length;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SyncFileInfoCollection {

    public HashMap<String, SyncFileInfo> resources = new HashMap<>();

    public SyncFileInfoCollection() {

    }

    public void addResource(SyncFileInfo resource) {
        resources.put(resource.unique_id, resource);
    }


    public ArrayList<SyncFileInfo> difference(SyncFileInfoCollection collection) {
        ArrayList<SyncFileInfo> updateList = new ArrayList<>();
        for (Map.Entry<String, SyncFileInfo> entry : this.resources.entrySet()) {
            // File is not in other resource
            if (!collection.resources.containsKey(entry.getKey())) {
                updateList.add(entry.getValue());
            }
            // File is different size
            else if (collection.resources.get(entry.getKey()).size != entry.getValue().size) {
                updateList.add(entry.getValue());
            }
        }
        return updateList;
    }


    public void summary() {
        for (Map.Entry<String, SyncFileInfo> entry : this.resources.entrySet()) {
            String s = String.format("%s:\n- %s\n- %d bytes\n- %s",
                    entry.getValue().unique_id,
                    entry.getValue().name,
                    entry.getValue().size,
                    entry.getValue().shareURL);
            System.out.println(s);
        }
    }

    public static SyncFileInfoCollection parseManifest(String manifest) throws ParseException {
        SyncFileInfoCollection resources = new SyncFileInfoCollection();
        JSONParser parse = new JSONParser();
        JSONObject base = (JSONObject) parse.parse(manifest);
        JSONArray arr = (JSONArray) base.get("children");
        for (Object a : arr) {
            JSONObject o = (JSONObject) a;
            SyncFileInfo resource = new SyncFileInfo(
                    (String) o.get("eTag"),
                    (String) o.get("name"),
                    (long) o.get("size"),
                    (String) o.get("webUrl"));
            resources.addResource(resource);
        }
        return resources;
    }
}
