package com.webserver.core.http;

import com.webserver.core.Server;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

/**
 * @author Hingbong
 */
public class Response {

    private SocketChannel socketChannel;
    private File urlFile;
    private String acceptType;

    public Response(SocketChannel socketChannel, Request request) {
        this.socketChannel = socketChannel;
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
            String head =
                new StringBuilder()
                    .append("HTTP/1.1 200 OK\n")
                    .append("Date: ")
                    .append(LocalDateTime.now())
                    .append("\n")
                    .append("Content-type:")
                    .append(contentType)
                    .append("\n")
                    .append("Content-length: ")
                    .append(urlFile.length())
                    .append("\n\n")
                    .toString();
            socketChannel.write(ByteBuffer.wrap(head.getBytes(StandardCharsets.UTF_8)));
            fileChannel = FileChannel.open(urlFile.toPath(), StandardOpenOption.READ);
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

    private void response404(SocketChannel socketChannel) {
        FileChannel notFound = null;
        try {
            socketChannel.write(
                ByteBuffer.wrap(
                    (new StringBuilder()
                        .append("HTTP/1.1 404 Not Found\r\n")
                        .append("Date: ")
                        .append(LocalDateTime.now())
                        .append("\n")
                        .append("Content-ype:text/html\n\n")
                        .toString())
                        .getBytes(StandardCharsets.UTF_8)));
            File file = new File("web_root/404.html");
            notFound = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            notFound.transferTo(0, file.length(), socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (notFound != null) {
                try {
                    notFound.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
