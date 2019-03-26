package com.webserver.core.servlet;

import com.webserver.core.User;
import com.webserver.core.http.Request;
import java.io.IOException;

public class ModifyPasswdServlet implements HttpServlet {

    private Request request;

    public ModifyPasswdServlet(Request request) {
        this.request = request;
    }

    @Override
    public void service() {
        User user = User.postUser(request.getMessage());
        if (user != null) {
            modifyPasswdServlet(user);
        }
    }

    private void modifyPasswdServlet(User user) {
        String message = request.getMessage();
        String requestURI = request.getRequestURI();
        if (requestURI.contains("modify_password")) {
            if (message.contains("&new_password=")) {
                boolean isModifyOK = false;
                String newPasswd = message.substring(message.indexOf("&new_password=") + 14);
                try {
                    isModifyOK = User.modifyPasswd(user, newPasswd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isModifyOK) {
                    request.setRequestURI(dir + "user/modify_successfully.html");
                } else {
                    request.setRequestURI(dir + "user/modify_failed.html");
                }
            }
        }
    }

}
