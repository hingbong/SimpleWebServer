package com.webserver.core.http;

import com.webserver.core.Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Response {

  private OutputStream output;
  private File urlFile;
  private String acceptType;

  public Response(OutputStream output, Request request) {
    this.output = output;
    String uri = request.getRequestURI();
    if ("".equals(uri) || uri == null) {
      uri = "index.html";
    }
    uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
    acceptType = request.getAcceptType();
    urlFile = new File(Server.WEB_ROOT, uri);
//    System.out.println(urlFile);
  }

  public void sendData() {
    // 发送数据
    PrintStream out = null;
    try {
      out = new PrintStream(output, true);
      if (urlFile.exists() && !urlFile.isDirectory()) {
        response200(out);
      } else {
        response404(out);
      }
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

  private String getContentType() {
    // 获取MIME类型
    String type;
    type = HttpContext.getMimeType(urlFile);
    return type;
  }

  private boolean isContentTypeIllegal(String contentType) {
    if (acceptType == null || contentType == null) {
      return false;
    }
    return acceptType.contains(contentType) || acceptType.contains("*/*");
  }

  private void response200(PrintStream out) {
    String contentType = getContentType();
    if (!isContentTypeIllegal(contentType)) {
      response404(out);
      return;
    }
    int len;
    byte[] data = new byte[1024 * 64];
    FileInputStream fileInputStream = null;
    String head = "HTTP/1.1 200 OK\n" +
        "Date: " + new Date() + "\n" +
        "Content-type:" + contentType + "\n" +
        "Content-length: " + urlFile.length() + "\n";
    out.println(head);
    try {
      fileInputStream = new FileInputStream(urlFile);
      while ((len = fileInputStream.read(data)) != -1) {
        out.write(data, 0, len);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void response404(PrintStream out) {
    out.println("HTTP/1.1 404 File Not Found\n" +
        "Date: " + new Date() + "\n");
    out.print("<!DOCTYPE html>\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>404 File Not Found</title>\n" +
        "</head>\n" +
        "<body>404 File Not Found</body>\n" +
        "</html>");
  }
}
