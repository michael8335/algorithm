package com.fly.practice.apachecommonsio;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.IOCase;
/**
 * Utils类基本功能示例
 * @author fly
 *
 */
public final class UtilityExample {
    
    //文件绝对路径
    private static final String EXAMPLE_TXT_PATH =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/commonio.txt";
    //文件父目录
    private static final String PARENT_DIR =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio";

    public static void runExample() throws IOException {
        System.out.println("Utility Classes example...");
        
        
        // FilenameUtils类示例
        //获取完整路径
        System.out.println("Full path of exampleTxt: " +
                FilenameUtils.getFullPath(EXAMPLE_TXT_PATH));
        //获取名称
        System.out.println("Full name of exampleTxt: " +
                FilenameUtils.getName(EXAMPLE_TXT_PATH));
      //获取扩展名
        System.out.println("Extension of exampleTxt: " +
                FilenameUtils.getExtension(EXAMPLE_TXT_PATH));
        
        System.out.println("Base name of exampleTxt: " +
                FilenameUtils.getBaseName(EXAMPLE_TXT_PATH));
        
        
        // FileUtils类示例
        
        // 通过FileUtils.getFile(String)创建File对象，然后根据FileUtils.lineIterator(File)
        //获取行迭代器
        File exampleFile = FileUtils.getFile(EXAMPLE_TXT_PATH);
        LineIterator iter = FileUtils.lineIterator(exampleFile);
        
        System.out.println("Contents of exampleTxt...");
        while (iter.hasNext()) {
            System.out.println("\t" + iter.next());
        }
        iter.close();
        
        // 目录是否已经包含文件
        File parent = FileUtils.getFile(PARENT_DIR);
        System.out.println("Parent directory contains exampleTxt file: " +
                FileUtils.directoryContains(parent, exampleFile));
        
        
        // IOCase类示例
        
        String str1 = "This is a new String.";
        String str2 = "This is another new String, yes!";
        
        System.out.println("Ends with string (case sensitive): " +
                IOCase.SENSITIVE.checkEndsWith(str1, "string."));
        System.out.println("Ends with string (case insensitive): " +
                IOCase.INSENSITIVE.checkEndsWith(str1, "string."));
        
        System.out.println("String equality: " +
                IOCase.SENSITIVE.checkEquals(str1, str2));
        
        
        // FileSystemUtils类示例
        System.out.println("Free disk space (in KB): " + FileSystemUtils.freeSpaceKb("/Users"));
        System.out.println("Free disk space (in MB): " + FileSystemUtils.freeSpaceKb("/Users") / 1024);
    }
}
