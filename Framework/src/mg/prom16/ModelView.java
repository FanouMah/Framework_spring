package mg.prom16;

import java.util.HashMap;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    public ModelView() {
        data = new HashMap<>();
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    public void addObject(String key, Object value){
        data.put(key, value);
    }
    public HashMap<String, Object> getData() {
        return data;
    }
}
