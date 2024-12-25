package mg.prom16;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;
    private Map<String, String> validationErrors;

    public ModelView() {
        this.data = new HashMap<>();
        this.validationErrors = new HashMap<>();
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

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
