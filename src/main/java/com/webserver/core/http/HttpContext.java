package com.webserver.core.http;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

class HttpContext {

    private static final HashMap<String, String> MIME_TYPE = new HashMap<>();

    static {
        initMimeType();
    }

    private static void initMimeType() {
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read("conf/web.xml");
            Element root = document.getRootElement();
            List<Element> elements = root.elements("mime-mapping");
            for (Element element : elements) {
                MIME_TYPE.put(element.elementTextTrim("extension"),
                    element.elementTextTrim("mime-type"));
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    static String getMimeType(File file) {
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return MIME_TYPE.get(extension);
    }
}
