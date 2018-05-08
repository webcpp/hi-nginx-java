package hi;

import java.util.HashMap;

public class request {

    public String client;
    public String user_agent;
    public String method;
    public String uri;
    public String param;
    public HashMap<String, String> headers;
    public HashMap<String, String> form;
    public HashMap<String, String> cookies;
    public HashMap<String, String> session;
    public HashMap<String, String> cache;

    public request() {
        this.client = new String();
        this.user_agent = new String();
        this.method = new String();
        this.uri = new String();
        this.param = new String();
        this.headers = new HashMap<String, String>();
        this.form = new HashMap<String, String>();
        this.cookies = new HashMap<String, String>();
        this.session = new HashMap<String, String>();
        this.cache = new HashMap<String, String>();
    }
}
