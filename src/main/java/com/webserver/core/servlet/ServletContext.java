package com.webserver.core.servlet;

import java.util.HashMap;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author Hingbong
 */
public class ServletContext {

    private static HashMap<String, String> selvletMap = new HashMap<>();

    static {
        initServletMap();
    }

    private static void initServletMap() {
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read("conf/servlet.xml");
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                selvletMap
                    .put(element.elementTextTrim("path"), element.elementTextTrim("servlet-class"));
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static String get(String requestURI) {
        return selvletMap.get(requestURI);
    }
}
