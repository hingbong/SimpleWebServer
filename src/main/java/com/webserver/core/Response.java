package com.webserver.core;

import java.io.*;
import java.nio.file.Files;
import java.util.Date;

class Response {
    private final OutputStream output;
    private final File uriFile;

    Response(OutputStream output, Request request) {
        this.output = output;
        request.parseUri();
        String uri = request.getUri();
        if ("".equals(uri))
            uri = "index.html";
        uriFile = new File(Server.WEB_ROOT, uri);
    }

    void sendData() {
        System.out.println(uriFile);
        byte[] data = readFile();
        try {
            PrintWriter out = new PrintWriter(output);
            if (uriFile.exists()) {
                response200(out);
                output.write(data);
                output.flush();
            } else {
                response404(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] readFile() {
        FileInputStream fis = null;
        byte[] data = null;
        if (uriFile.exists()) {
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
        String type;
        try {
            type = Files.probeContentType(uriFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            type = "text/plain";
        }
        System.out.println(type);
        return type;
    }

    private void response200(PrintWriter out) {
        String content = getContentType();
        String head = "HTTP/1.1 200 OK\n" +
                "Date: " + new Date() + "\n" +
                "Content-type:" + content + ";charset=utf-8\n" +
                "Content-length: " + uriFile.length() + "\n\n";
        out.print(head);
        out.flush(); // flush character output stream buffer
    }

    private void response404(PrintWriter out) {
        out.println("HTTP/1.1 404 File Not Found\n");
        out.flush();
        out.print("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>404 File Not Found</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "404 File Not Found\n" +
                "</body>\n" +
                "</html>");
        out.flush();
    }
}
