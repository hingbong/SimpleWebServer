package com.webserver.core.http;

import com.webserver.core.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Request {

    private final InputStream in;
    private HashMap<String, String> httpHeader;
    private HashMap<String, String> parameters = new HashMap<>();
    private String url;
    private String requestLine;
    private String protocol;
    private String method;
    private String requestURI;
    private String queryString;

    public Request(InputStream in) {
        this.in = in;
    }

    public void start() {
        String message = getMessage();
        if (message == null) {
            return;
        }
        setRequestLine(message);
        parseURL();
        parseURI();
        parseParam();
        storeHeader(message);
        User user = postUser(message);
        if (user != null) {
            if (url.contains("reg")) {
                boolean isNewUser = false;
                try {
                    isNewUser = User.newUser(user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isNewUser) {
                    System.out.println("user:" + user);
                    requestURI = "user/reg_successfully.html";
                } else {
                    requestURI = "user/reg_failed.html";
                    System.out.println("NOT Ａ NEW USER");
                }
            }
            if (url.contains("index")) {
                boolean isLoginOk = false;
                try {
                    isLoginOk = User.verifyUser(user);
                } catch (IOException e) {
                    requestURI = "user/login_failed.html";
                    e.printStackTrace();
                }
                if (isLoginOk) {
                    requestURI = "user/login_successfully.html";
                } else requestURI = "user/login_failed.html";
            }
            if (url.contains("modify_password")) {
                if (message.contains("&new_password=")) {
                    boolean isModifyOK = false;
                    String newPasswd = message.substring(message.indexOf("&new_password=") + 14);
                    System.out.println(newPasswd);
                    try {
                        isModifyOK = User.modifyPasswd(user, newPasswd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isModifyOK) {
                        requestURI = "user/modify_successfully.html";
                    } else requestURI = "user/modify_failed.html";
                }
            }
        }
//        System.out.println(requestURI);
//        System.out.println(url);
    }

    String getMessage() {
        // 解析uri
        String message = "";
        byte[] data = new byte[2048];
        int len;
        try {
            len = in.read(data);
            if (len != -1) {
                message = new String(data, 0, len, StandardCharsets.UTF_8);
            }
            return message;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setRequestLine(String message) {
        if (message != null) {
            if (message.toUpperCase().contains("GET") || message.toUpperCase().contains("POST")
                    || !message.contains("&confirm_password=")) {
                requestLine = message.substring(0, message.indexOf("\n"));
                protocol = requestLine.substring(message.indexOf("HTTP"));
                if (requestLine.toUpperCase().contains("GET")) {
                    method = "GET";
                }
                if (requestLine.toUpperCase().contains("POST")) {
                    method = "POST";
                }
            }
        }
    }

    private void parseURL() {
        int index1, index2;
        index1 = requestLine.indexOf(' ');
        if (index1 != -1) {
            index2 = requestLine.indexOf(' ', index1 + 1);
            if (index2 > index1) {
                url = requestLine.substring(index1 + 2, index2);
            }
        }
    }

    private void parseURI() {
        if (url.contains("?") && url.contains("=")) {
            String[] strings = url.split("\\?");
            requestURI = strings[0];
            queryString = strings[1];
        } else {
            requestURI = url;
        }
    }

    private void parseParam() {
        if (queryString != null) {
            if (queryString.contains("&")) {
                String[] strs = queryString.split("&");
                for (String str : strs) {
                    String[] params = str.split("=");
                    parameters.put(URLDecoder.decode(params[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(params[1], StandardCharsets.UTF_8));
                }
            }
        }
//        System.out.println(parameters);
    }

    private void storeHeader(String message) {
        // 拆分http请求头,HashMap存储
        httpHeader = new HashMap<>();
        if (message == null) {
            return;
        }
//    System.out.println(message);
        if (!message.contains("\r\n")) {
            return;
        }
        String subMessage = message.substring(message.indexOf("\r\n")).trim();
        String[] subMessages = subMessage.split("\\r\\n");
        for (String s : subMessages) {
            if (s.contains(": ")) {
                httpHeader.put(s.substring(0, s.indexOf(":")),
                        s.substring(s.indexOf(":") + 2));
            }
        }
    }

    private User postUser(String message) {
        // 传送用户
        if (message.contains("username=") && message.contains("password=")) {
            message = URLDecoder.decode(message, StandardCharsets.UTF_8);
            String name = message
                    .substring(message.indexOf("username=") + 9, message.indexOf("&password="));
            String passwd;
            if (message.contains("&confirm_password=")) {
                passwd = message
                        .substring(message.indexOf("&password=") + 10, message.indexOf("&confirm_password="));
            } else if (message.contains("&new_password=")) {
                passwd = message
                        .substring(message.indexOf("&password=") + 10, message.indexOf("&new_password="));

            } else {
                passwd = message.substring(message.indexOf("&password=") + 10);
            }
            return new User(name, passwd);
        }
        return null;
    }

    String getRequestURI() {
        return requestURI;
    }

    String getAcceptType() {
        return httpHeader.get("Accept");
    }
}
