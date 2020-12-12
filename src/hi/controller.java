package hi;

import hi.servlet;
import hi.request;
import hi.response;
import hi.route;

public class controller implements hi.servlet {
    private static controller instance = null;

    public static controller get_instance() {
        if (controller.instance == null) {
            controller.instance = new controller();
        }
        return controller.instance;
    }

    public controller() {
    }

    public void handler(hi.request req, hi.response res) {
        hi.route.get_instance().run(req, res);
    }
}
