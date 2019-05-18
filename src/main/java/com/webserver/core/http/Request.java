package com.webserver.core.http;

import com.webserver.core.servlet.HttpServlet;
import com.webserver.core.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @author Hingbong
 */
public class Request {

    private final SocketChannel socketChannel;
    private ByteBuffer buffer;
    private HashMap<String, String> httpHeader = new HashMap<>();
    private HashMap<String, String> parameters = new HashMap<>();
    private String url;
    private String requestLine;
    private String message;
    private String protocol;
    private String method;
    private String requestURI;
    private String queryString;

    public Request(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public synchronized void start() {
        readData();
        message = parseMessage();
        setRequestLine(message);
        parseURL();
        parseURI();
        parseParam();
        storeHeader(message);
        String function = "function";
        String html = ".html";
        if (requestURI.contains(function) && requestURI.endsWith(html)) {
            String className = ServletContext.get(requestURI);
            try {
                Class<?> clazz = Class
                    .forName("com.webserver.core.servlet." + className + "Servlet");
                Constructor<?> constructor = clazz.getConstructor(Request.class);
                HttpServlet httpServlet = (HttpServlet) constructor.newInstance(this);
                httpServlet.service();
            } catch (ClassNotFoundException
                | NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void readData() {
        int pos = 0;
        try {
            buffer = ByteBuffer.allocate(1024);
            socketChannel.read(buffer);
            buffer.limit(buffer.capacity());
            int read = socketChannel.read(buffer);
            if (read == -1) {
                return;
            }
            buffer.flip();
            buffer.position(pos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseMessage() {
        return new String(buffer.array(), 0, buffer.limit());
    }

    private void setRequestLine(String message) {
        String get = "GET";
        String post = "POST";
        if (message != null) {
            if (message.toUpperCase().contains(get)
                || message.toUpperCase().contains(post)
                || !message.contains("&confirm_password=")) {
                requestLine = message.substring(0, message.indexOf("\n"));
                protocol = requestLine.substring(message.indexOf("HTTP"));
                if (requestLine.toUpperCase().contains(get)) {
                    method = "GET";
                }
                if (requestLine.toUpperCase().contains(post)) {
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
        String questionMark = "?";
        String equalSign = "=";
        if (url.contains(questionMark) && url.contains(equalSign)) {
            String[] strings = url.split("\\?");
            requestURI = strings[0];
            queryString = strings[1];
        } else {
            requestURI = url;
        }
    }

    private void parseParam() {
        if (queryString != null) {
            String andSign = "&";
            if (queryString.contains(andSign)) {
                String[] strs = queryString.split("&");
                for (String str : strs) {
                    String[] params = str.split("=");
                    parameters.put(
                        URLDecoder.decode(params[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(params[1], StandardCharsets.UTF_8));
                }
            }
        }
        //        System.out.println(parameters);
    }

    private void storeHeader(String message) {
        // 拆分http请求头,HashMap存储
        if (message == null) {
            return;
        }
        String enter = "\r\n";
        if (!message.contains(enter)) {
            return;
        }
        String subMessage = message.substring(message.indexOf("\r\n")).trim();
        String[] subMessages = subMessage.split("\\r\\n");
        for (String s : subMessages) {
            if (s.contains(": ")) {
                httpHeader.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 2));
            }
        }
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    String getAcceptType() {
        return httpHeader.get("Accept");
    }

    public String getMessage() {
        return message;
    }
}
