package com.webserver.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

class Request {

  private final InputStream in;
  private HashMap<String, String> httpHeader;
  private String uri;

  Request(InputStream in) {
    this.in = in;
  }

  void parseUri() {
    // 解析uri
    String message = "";
    byte[] data = new byte[2048];
    int len;
    try {
      len = in.read(data);
      if (len != -1) {
        message = new String(data, 0, len, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      e.printStackTrace();
      len = -1;
    }
//    System.out.print(message);
    storeHeader(message);
    System.out.println(httpHeader);
    if (len != -1) {
      int index1, index2;
      index1 = message.indexOf(' ');
      if (index1 != -1) {
        index2 = message.indexOf(' ', index1 + 1);
        if (index2 > index1) {
          uri = message.substring(index1 + 2, index2);
        }
      }
    }

    User user = postUser(message);
    if (user != null) {
      System.out.println("user:" + user);
    }
//    System.out.println(uri);
  }

  String getUri() {
    return uri;
  }

  String getAcceptType() {
    return httpHeader.get("Accept");
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
    for (int i = 0; i < subMessages.length; i++) {
      if (subMessages[i].contains(": ")) {
        httpHeader.put(subMessages[i].substring(0, subMessages[i].indexOf(":")),
            subMessages[i].substring(subMessages[i].indexOf(":") + 2));
      }
    }
  }

  private User postUser(String message) {
    // 传送用户
    if (message.contains("username=") && message.contains("password=")) {
      return new User(
          message.substring(message.indexOf("username=") + 9, message.indexOf("&password=")),
          message.substring(message.indexOf("&password=") + 10));
    }
    return null;
  }
}
