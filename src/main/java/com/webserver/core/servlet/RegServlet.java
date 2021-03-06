package com.webserver.core.servlet;

import com.webserver.core.User;
import com.webserver.core.http.Request;
import java.io.IOException;

/**
 * @author Hingbong
 */
public class RegServlet implements HttpServlet {

    private Request request;

    public RegServlet(Request request) {
        this.request = request;
    }

    @Override
    public void service() {
        User user = User.postUser(request.getMessage());
        if (user != null) {
            regServlet(user);
        }
    }

    private void regServlet(User user) {
        String requestURI = request.getRequestURI();
        String reg = "reg";
        if (requestURI.contains(reg)) {
            boolean isNewUser = false;
            try {
                isNewUser = User.newUser(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (isNewUser) {
                request.setRequestURI(DIR + "user/reg_successfully.html");
            } else {
                request.setRequestURI(DIR + "user/reg_failed.html");
                System.out.println("NOT Ａ NEW USER");
            }
        }
    }
}
