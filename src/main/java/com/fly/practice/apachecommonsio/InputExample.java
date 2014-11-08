package com.fly.practice.apachecommonsio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.XmlStreamReader;


public final class InputExample {
    
    private static final String XML_PATH =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/test.xml";
    
    public static void runExample() {
        System.out.println("Input example...");
        XmlStreamReader xmlReader = null;
        try {
            
            // XmlStreamReader:读取一个xml文件，提供了过去文件编码API
            File xml = FileUtils.getFile(XML_PATH);
            xmlReader = new XmlStreamReader(xml);
            System.out.println("XML encoding: " + xmlReader.getEncoding());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { xmlReader.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}