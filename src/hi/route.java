package hi;

import hi.servlet;
import hi.request;
import hi.response;
import hi.lrucache;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.FileReader;

import java.lang.Class;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;

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

    private class reflect_t {
        public reflect_t() {
            this.t = System.currentTimeMillis();
            this.cls = null;
            this.cls_method = null;
        }

        public long t;
        public Class<?> cls;
        public Method cls_method;
    }

    public static long lrucache_reflect_expires = 300;
    public static int lrucache_reflect_size = 1024;
    public static String default_uri_pattern = new String("(.*)");
    private static String lrucache_reflect_expires_config_path = new String("route.lrucache.reflect.expires");
    private static String lrucache_reflect_size_config_path = new String("route.lrucache.reflect.size");
    private static String template_directory_config_path = new String("template.directory");
    private static String template_directory_config_default_path = new String("java/templates");
    private static String error_40x_config_path = new String("/404.html");
    private static String error_50x_config_path = new String("/50x.html");

    private static route instance = new route();

    private static HashMap<String, route_ele_t> map = new HashMap<String, route_ele_t>();

    private static lrucache<String, reflect_t> reflect_map = new lrucache<String, reflect_t>(
            route.lrucache_reflect_size);

    public static ArrayList<String> http_get_method() {
        return new ArrayList<String>(Arrays.asList("GET"));
    }

    public static ArrayList<String> http_post_method() {
        return new ArrayList<String>(Arrays.asList("POST"));
    }

    public static ArrayList<String> http_put_method() {
        return new ArrayList<String>(Arrays.asList("PUT"));
    }

    public static ArrayList<String> http_delete_method() {
        return new ArrayList<String>(Arrays.asList("DELETE"));
    }

    public static ArrayList<String> http_patch_method() {
        return new ArrayList<String>(Arrays.asList("PATCH"));
    }

    public static ArrayList<String> http_head_method() {
        return new ArrayList<String>(Arrays.asList("HEAD"));
    }

    public static ArrayList<String> http_option_method() {
        return new ArrayList<String>(Arrays.asList("OPTION"));
    }

    public static ArrayList<String> http_all_method() {
        return new ArrayList<String>(Arrays.asList("GET", "POST", "PUT", "HEAD", "DELETE", "PATCH", "OPTION"));
    }

    private Config config;
    private TemplateLoader loader;
    private Compiler compiler;

    private route() {
        this.config = ConfigFactory.load();
        this.loader = (String name) -> {
            if (this.config.hasPath(route.template_directory_config_path)) {
                return new FileReader(new File(this.config.getString(route.template_directory_config_path), name));
            }
            return new FileReader(new File(route.template_directory_config_default_path, name));
        };
        this.compiler = Mustache.compiler().withLoader(this.loader);

        if (this.config.hasPath(route.lrucache_reflect_expires_config_path)) {
            route.lrucache_reflect_expires = this.config.getLong(route.lrucache_reflect_expires_config_path);
        }

        if (this.config.hasPath(route.lrucache_reflect_size_config_path)) {
            route.lrucache_reflect_size = this.config.getInt(route.lrucache_reflect_size_config_path);
        }
        if (this.config.hasPath(route.error_40x_config_path)) {
            route.error_40x_config_path = this.config.getString(route.error_40x_config_path);
        }
        if (this.config.hasPath(route.error_50x_config_path)) {
            route.error_50x_config_path = this.config.getString(route.error_50x_config_path);
        }

    }

    public static route get_instance() {
        return route.instance;
    }

    public Config get_config() {
        return this.config;
    }

    public TemplateLoader get_loader() {
        return this.loader;
    }

    public Compiler get_compiler() {
        return this.compiler;
    }

    private void default_callback(request req, response res, Matcher mt) {
        reflect_t ref = new reflect_t();
        if (route.reflect_map.containsKey(req.uri)) {
            ref = route.reflect_map.get(req.uri);
            if ((System.currentTimeMillis() - ref.t) / 1000 <= route.lrucache_reflect_expires) {
                try {
                    ref.cls_method.invoke(ref.cls.getConstructor().newInstance(), req, res, mt);
                } catch (Exception e) {
                    route.reflect_map.remove(req.uri);
                    // res.set_content_type("text/plain;charset=UTF-8");
                    // res.content = String.format("callback is failed: %s\n" , e.getMessage());
                    // res.status = 500;
                    res.status = 301;
                    res.set_header("Location", route.error_50x_config_path);
                }
            } else {
                route.reflect_map.remove(req.uri);
                this.update_reflect_map(ref, req, res, mt);
            }
        } else {
            this.update_reflect_map(ref, req, res, mt);
        }
    }

    private void update_reflect_map(reflect_t ref, request req, response res, Matcher mt) {
        String class_name = req.uri.substring(1);
        if (class_name.endsWith("/")) {
            class_name = class_name.substring(0, class_name.length() - 1);
        }
        class_name = class_name.replace('/', '.');
        try {
            ref.cls = Class.forName(class_name);
            try {
                ref.cls_method = ref.cls.getMethod("handler", hi.request.class, hi.response.class, Matcher.class);
                try {
                    ref.cls_method.invoke(ref.cls.getConstructor().newInstance(), req, res, mt);
                    route.reflect_map.put(req.uri, ref);
                } catch (Exception e) {
                    // res.set_content_type("text/plain;charset=UTF-8");
                    // res.content = String.format("callback is failed: %s\n" , e.getMessage());
                    // res.status = 500;
                    res.status = 301;
                    res.set_header("Location", route.error_50x_config_path);
                }
            } catch (Exception e) {
                // res.set_content_type("text/plain;charset=UTF-8");
                // res.content = "NoSuchMethodException: " + e.getMessage();
                // res.status = 404;
                res.status = 301;
                res.set_header("Location", route.error_40x_config_path);
            }
        } catch (Exception e) {
            // res.set_content_type("text/plain;charset=UTF-8");
            // res.content = "ClassNotFoundException: " + e.getMessage();
            // res.status = 404;
            res.status = 301;
            res.set_header("Location", route.error_40x_config_path);
        }
    }

    public void add(ArrayList<String> m, String p, route.run_t r) {
        if (!route.map.containsKey(p)) {
            route_ele_t ele = new route_ele_t();
            ele.pattern = p;
            ele.regex = Pattern.compile(p);
            ele.method = m;
            ele.callback = r;
            route.map.put(p, ele);
        } else if (!route.map.get(p).method.equals(m)) {
            route_ele_t ele = new route_ele_t();
            ele.pattern = p;
            ele.regex = Pattern.compile(p);
            ele.method = m;
            ele.callback = r;
            route.map.put(p + m.toString(), ele);
        }
    }

    public void get(String p, route.run_t r) {
        this.add(route.http_get_method(), p, r);
    }

    public void get(String p) {
        this.add(route.http_get_method(), p, this::default_callback);
    }

    public void post(String p, route.run_t r) {
        this.add(route.http_post_method(), p, r);
    }

    public void post(String p) {
        this.add(route.http_post_method(), p, this::default_callback);
    }

    public void put(String p, route.run_t r) {
        this.add(route.http_put_method(), p, r);
    }

    public void put(String p) {
        this.add(route.http_put_method(), p, this::default_callback);
    }

    public void head(String p, route.run_t r) {
        this.add(route.http_head_method(), p, r);
    }

    public void head(String p) {
        this.add(route.http_head_method(), p, this::default_callback);
    }

    public void delete(String p, route.run_t r) {
        this.add(route.http_delete_method(), p, r);
    }

    public void delete(String p) {
        this.add(route.http_delete_method(), p, this::default_callback);
    }

    public void patch(String p, route.run_t r) {
        this.add(route.http_patch_method(), p, r);
    }

    public void patch(String p) {
        this.add(route.http_patch_method(), p, this::default_callback);
    }

    public void option(String p, route.run_t r) {
        this.add(route.http_option_method(), p, r);
    }

    public void option(String p) {
        this.add(route.http_option_method(), p, this::default_callback);
    }

    public void all(String p, route.run_t r) {
        this.add(route.http_all_method(), p, r);
    }

    public void all(String p) {
        this.add(route.http_all_method(), p, this::default_callback);
    }

    public void all() {
        this.add(route.http_all_method(), route.default_uri_pattern, this::default_callback);
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
            this.default_callback(req, res, Pattern.compile(route.default_uri_pattern).matcher(req.uri));
        }

    }

    public String to_string() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("size = %s\n", route.map.size()));
        for (HashMap.Entry<String, route.route_ele_t> item : route.map.entrySet()) {
            buffer.append(
                    String.format("pattern = %s,\tmethod =%s\n", item.getKey(), item.getValue().method.toString()));
        }
        return buffer.toString();
    }
}
