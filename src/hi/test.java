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
        res.content = "welcome to hi-nginx-java\n";
        res.status = 200;
    }
}