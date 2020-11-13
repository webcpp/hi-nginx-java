package hi;

import hi.servlet;
import hi.request;
import hi.response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class route {

    public interface run_t {
        void handler(request req, response res, Matcher m);
    }

    private class route_ele_t {
        public route_ele_t() {
        }

        public String pattern;
        public ArrayList<String> method;
        public Pattern regex;
        public route.run_t callback;
    }

    private static route instance = new route();

    private static HashMap<String, route_ele_t> map = new HashMap<String, route_ele_t>();

    private route() {
    }

    public static route get_instance() {
        return route.instance;
    }

    public void add(ArrayList<String> m, String p, route.run_t r) {
        if (!route.map.containsKey(p)) {
            route_ele_t ele = new route_ele_t();
            ele.pattern = p;
            ele.regex = Pattern.compile(p);
            ele.method = m;
            ele.callback = r;
            route.map.put(p, ele);
        }
    }

    public void get(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("GET")), p, r);
    }

    public void post(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("POST")), p, r);
    }

    public void put(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("PUT")), p, r);
    }

    public void head(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("HEAD")), p, r);
    }

    public void delete(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("DELETE")), p, r);
    }

    public void patch(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("PATCH")), p, r);
    }

    public void option(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("OPTION")), p, r);
    }

    public void all(String p, route.run_t r) {
        this.add(new ArrayList<String>(Arrays.asList("GET", "POST", "PUT", "HEAD", "DELETE", "PATCH", "OPTION")), p, r);
    }

    public void run(request req, response res) {
        Iterator<HashMap.Entry<String, route_ele_t>> iter = route.map.entrySet().iterator();
        Matcher m = null;
        while (iter.hasNext()) {
            route_ele_t ele = iter.next().getValue();
            if (ele.method.contains(req.method) && Pattern.matches(ele.pattern, req.uri)) {
                m = ele.regex.matcher(req.uri);
                ele.callback.handler(req, res, m);
                break;
            }
        }

    }
}
