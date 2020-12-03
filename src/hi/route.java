package hi;

import hi.servlet;
import hi.request;
import hi.response;
import hi.lrucache;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.lang.Class;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class route {

    public interface run_t {
        public void handler(request req, response res, Matcher m);
    }

    private class route_ele_t {
        public route_ele_t() {
        }

        public String pattern;
        public ArrayList<String> method;
        public Pattern regex;
        public route.run_t callback;
    }

    protected class reflect_t {
        public reflect_t() {
            this.cls = null;
            this.cls_method = null;
        }

        public Class<?> cls;
        public Method cls_method;
    }

    private static route instance = new route();

    private static HashMap<String, route_ele_t> map = new HashMap<String, route_ele_t>();

    private static lrucache<String, reflect_t> reflect_map = new lrucache<String, reflect_t>(1024);

    private route() {
    }

    public static route get_instance() {
        return route.instance;
    }

    private void defualt_callback(request req, response res, Matcher mt, String input_class_name) {
        String class_name = input_class_name.isEmpty() || input_class_name.isBlank() ? req.uri.substring(1)
                : input_class_name;
        if (class_name.endsWith("/")) {
            class_name = class_name.substring(0, class_name.length() - 1);
        }
        class_name = class_name.replace('/', '.');
        reflect_t ref = new reflect_t();
        if (route.reflect_map.containsKey(class_name)) {
            ref = route.reflect_map.get(class_name);
            try {
                ref.cls_method.invoke(ref.cls.getConstructor().newInstance(), req, res, mt);
            } catch (Exception e) {
                route.reflect_map.remove(class_name);
                res.set_content_type("text/plain;charset=UTF-8");
                res.content = "callback is failed: " + e.getMessage();
                res.status = 500;
            }
        } else {
            try {
                ref.cls = Class.forName(class_name);
                try {
                    ref.cls_method = ref.cls.getMethod("handler", hi.request.class, hi.response.class, Matcher.class);
                    try {
                        ref.cls_method.invoke(ref.cls.getConstructor().newInstance(), req, res, mt);
                        route.reflect_map.put(class_name, ref);
                    } catch (Exception e) {
                        res.set_content_type("text/plain;charset=UTF-8");
                        res.content = "callback is failed: " + e.getMessage();
                        res.status = 500;
                    }
                } catch (Exception e) {
                    res.set_content_type("text/plain;charset=UTF-8");
                    res.content = "NoSuchMethodException: " + e.getMessage();
                    res.status = 404;
                }
            } catch (Exception e) {
                res.set_content_type("text/plain;charset=UTF-8");
                res.content = "ClassNotFoundException: " + e.getMessage();
                res.status = 404;
            }
        }
    };

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

    public void add(ArrayList<String> m, String p, String class_name) {
        if (!route.map.containsKey(p)) {
            route_ele_t ele = new route_ele_t();
            ele.pattern = p;
            ele.regex = Pattern.compile(p);
            ele.method = m;
            ele.callback = (request req, response res, Matcher mt) -> {
                this.defualt_callback(req, res, mt, class_name);
            };
            route.map.put(p, ele);
        }
    }

    public void add(ArrayList<String> m) {
        String p = "(.*)";
        if (!route.map.containsKey(p)) {
            route_ele_t ele = new route_ele_t();
            ele.pattern = p;
            ele.regex = Pattern.compile(p);
            ele.method = m;
            ele.callback = (request req, response res, Matcher mt) -> {
                this.defualt_callback(req, res, mt, "");
            };
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

    public void all() {
        this.add(new ArrayList<String>(Arrays.asList("GET", "POST", "PUT", "HEAD", "DELETE", "PATCH", "OPTION")));
    }

    public void run(request req, response res) {
        Iterator<HashMap.Entry<String, route_ele_t>> iter = route.map.entrySet().iterator();
        Matcher m = null;
        boolean ok = false;
        while (iter.hasNext()) {
            route_ele_t ele = iter.next().getValue();
            if (ele.method.contains(req.method) && Pattern.matches(ele.pattern, req.uri)) {
                m = ele.regex.matcher(req.uri);
                ele.callback.handler(req, res, m);
                ok = true;
                break;
            }
        }
        if (!ok) {
            this.defualt_callback(req, res, Pattern.compile("(.*)").matcher(req.uri), "");
        }

    }
}
