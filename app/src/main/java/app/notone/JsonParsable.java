package app.notone;

import org.json.JSONObject;

public interface JsonParsable {
    public String stringify();

    public void parse(String json);
}
