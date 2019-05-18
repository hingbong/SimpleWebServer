package com.webserver.core.servlet;

import com.webserver.core.User;
import com.webserver.core.http.Request;
import java.io.IOException;

/**
 * @author Hingbong
 */
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
        String modifyPassword = "modify_password";
        String newPassword = "&new_password=";
        if (requestURI.contains(modifyPassword)) {
            if (message.contains(newPassword)) {
                boolean isModifyOK = false;
                String newPasswd = message.substring(message.indexOf("&new_password=") + 14);
                try {
                    isModifyOK = User.modifyPasswd(user, newPasswd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isModifyOK) {
                    request.setRequestURI(DIR + "user/modify_successfully.html");
                } else {
                    request.setRequestURI(DIR + "user/modify_failed.html");
                }
            }
        }
    }
}
