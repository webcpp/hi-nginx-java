package hi;

import hi.servlet;
import hi.request;
import hi.response;
import hi.route;

public class controller implements hi.servlet {
    public controller() {
    }

    public void handler(hi.request req, hi.response res) {
        hi.route.get_instance().run(req, res);
    }
}
