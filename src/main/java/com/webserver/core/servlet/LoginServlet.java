package com.webserver.core.servlet;

import com.webserver.core.User;
import com.webserver.core.http.Request;
import java.io.IOException;

public class LoginServlet implements HttpServlet {

    private Request request;

    public LoginServlet(Request request) {
        this.request = request;
    }

    @Override
    public void service() {
        User user = User.postUser(request.getMessage());
        if (user != null) {
            loginServlet(user);
        }
    }

    private void loginServlet(User user) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("login")) {
            boolean isLoginOk = false;
            try {
                isLoginOk = User.verifyUser(user);
            } catch (IOException e) {
                request.setRequestURI(dir + "user/login_failed.html");
                e.printStackTrace();
            }
            if (isLoginOk) {
                request.setRequestURI(dir + "user/login_successfully.html");
            } else {
                request.setRequestURI(dir + "user/login_failed.html");
            }
        }
    }
}
