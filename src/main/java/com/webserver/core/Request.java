package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class Request {
    private String uri;
    private final InputStream in;

    Request(InputStream in) {
        this.in = in;
    }

    void parseUri() {
        String message = "";
        byte[] data = new byte[2048];
        int len;
        try {
            len = in.read(data);
            message = new String(data, 0, len, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            len = -1;
        }
        System.out.print(message);
        if (len != -1) {
            int index1, index2;
            index1 = message.indexOf(' ');
            if (index1 != -1) {
                index2 = message.indexOf(' ', index1 + 1);
                if (index2 > index1)
                    uri = message.substring(index1 + 2, index2);
            }
        }
        System.out.println(uri);
    }

    String getUri() {
        return uri;
    }
}
