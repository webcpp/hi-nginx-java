package hi;

import hi.request;
import hi.response;
import hi.route;
import java.util.regex.Matcher;

public class test implements hi.route.run_t {
    public test() {
    }

    public void handler(hi.request req, hi.response res, Matcher m) {
        res.set_content_type("text/plain;charset=utf-8");
        StringBuilder builder = new StringBuilder("welcome to hi-nginx-java\n");
        builder.append("java version: ");
        builder.append(System.getProperty("java.version"));
        builder.append("\n");
        res.content = builder.toString();
        res.status = 200;
    }
}