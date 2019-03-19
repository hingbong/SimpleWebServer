package com.webserver.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;

class Response {

  private Socket socket;
  private File uriFile;
  private String acceptType;

  Response(Socket socket, Request request) {
    this.socket = socket;
    request.parseUri();
    String uri = request.getUri();
    if ("".equals(uri) || uri == null) {
      uri = "index.html";
    }
    acceptType = request.getAcceptType();
    uriFile = new File(Server.WEB_ROOT, uri);
//    System.out.println(uriFile);
  }

  void sendData() {
    // 发送数据
    byte[] data = readFile();
    OutputStream output = null;
    PrintWriter out = null;
    try {
      output = socket.getOutputStream();
      out = new PrintWriter(output);
      if (uriFile.exists()) {
        if (data != null) {
          response200(out);
          output.write(data);
          output.flush();
        } else {
          response404(out);
        }
      } else {
        response404(out);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (out != null) {
        out.close();
        try {
          output.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private byte[] readFile() {
    // 读取文件
    FileInputStream fis = null;
    byte[] data = null;
    if (uriFile.exists()) {
      if (uriFile.isDirectory()) {
        return null;
      }
      try {
        data = new byte[(int) uriFile.length()];
        fis = new FileInputStream(uriFile);
        //noinspection ResultOfMethodCallIgnored
        fis.read(data);
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return data;
  }

  private String getContentType() {
    // 获取MIME类型
    String type;
    try {
      type = Files.probeContentType(uriFile.toPath());
    } catch (IOException e) {
      e.printStackTrace();
      type = "text/plain";
    }
    return type;
  }

  private boolean isContentTypeIllegal(String contentType) {
    if (contentType == null) {
      return false;
    }
    return acceptType.contains(contentType) || acceptType.contains("*/*");
  }

  private void response200(PrintWriter out) {
    String contentType = getContentType();
    if (!isContentTypeIllegal(contentType)) {
      response404(out);
      return;
    }
    String head = "HTTP/1.1 200 OK\n" +
        "Date: " + new Date() + "\n" +
        "Content-type:" + contentType + "\n" +
        "Content-length: " + uriFile.length() + "\n\n";
    out.print(head);
    out.flush(); // flush character output stream buffer
  }

  private void response404(PrintWriter out) {
    out.println("HTTP/1.1 404 File Not Found\n");
    out.flush();
    out.print("<!DOCTYPE html>\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>404 File Not Found</title>\n" +
        "</head>\n" +
        "<body>404 File Not Found</body>\n" +
        "</html>");
    out.flush();
  }
}
