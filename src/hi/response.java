package hi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class response {

    public int status;
    public String content;
    public HashMap<String, ArrayList<String>> headers;
    public HashMap<String, String> session;
    public HashMap<String, String> cache;

    public response() {
        this.status = 404;
        this.content = new String("<p style='text-align:center;margin:100px;'>404 Not Found</p>");
        this.headers = new HashMap<String, ArrayList<String>>();
        this.headers.put("Content-Type", new ArrayList<String>(Arrays.asList(("text/html;charset=UTF-8"))));
        this.session = new HashMap<String, String>();
        this.cache = new HashMap<String, String>();
    }
}
