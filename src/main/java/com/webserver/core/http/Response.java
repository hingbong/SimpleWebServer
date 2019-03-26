package com.webserver.core.http;

import com.webserver.core.Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Response {

    private SocketChannel socketChannel;
    private CharsetEncoder encoder;
    private File urlFile;
    private String acceptType;

    public Response(SocketChannel socketChannel, Request request) {
        this.socketChannel = socketChannel;
        encoder = StandardCharsets.UTF_8.newEncoder();
        String uri = request.getRequestURI();
        if ("".equals(uri) || uri == null) {
            uri = "index.html";
        }
        uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        acceptType = request.getAcceptType();
        urlFile = new File(Server.WEB_ROOT, uri);
    }

    void sendData() {
        // 发送数据
        try {
            if (urlFile.exists() && !urlFile.isDirectory()) {
                response200(socketChannel);
            } else {
                response404(socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socketChannel != null) {
                try {
                    socketChannel.close();
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

    private void response200(SocketChannel socketChannel) {
        FileChannel fileChannel = null;
        try {
            String contentType = getContentType();
            if (!isContentTypeIllegal(contentType)) {
                response404(socketChannel);
                return;
            }
            String head = "HTTP/1.1 200 OK\n" +
                "Date: " + new Date() + "\n" +
                "Content-type:" + contentType + "\n" +
                "Content-length: " + urlFile.length() + "\n";
            socketChannel.write(encoder.encode(CharBuffer.wrap(head + "\n")));
            fileChannel = new FileInputStream(urlFile).getChannel();
            fileChannel.transferTo(0, urlFile.length(), socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void response404(SocketChannel socketChannel) throws IOException {
        socketChannel.write(encoder.encode(CharBuffer.wrap(("HTTP/1.1 404 File Not Found\n" +
            "Date: " + new Date() + "\n"))));
        socketChannel.write(encoder.encode(CharBuffer.wrap("<!DOCTYPE html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>404 File Not Found</title>\n" +
            "</head>\n" +
            "<body>404 File Not Found</body>\n" +
            "</html>")));
    }
}
