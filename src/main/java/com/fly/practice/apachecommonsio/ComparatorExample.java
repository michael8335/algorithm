package com.fly.practice.apachecommonsio;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

public final class ComparatorExample {
    
    private static final String PARENT_DIR =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio";
    
    private static final String FILE_1 =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/commonio.txt";
    
    private static final String FILE_2 =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/test.xml";
    
    public static void runExample() {
        System.out.println("Comparator example...");
        
        //NameFileComparator:根据文件名称排序，IOCase表示是否大小写敏感
        
        File parentDir = FileUtils.getFile(PARENT_DIR);
        NameFileComparator comparator = new NameFileComparator(IOCase.SENSITIVE);
        File[] sortedFiles = comparator.sort(parentDir.listFiles());
        
        System.out.println("Sorted by name files in parent directory: ");
        for (File file: sortedFiles) {
            System.out.println("\t"+ file.getAbsolutePath());
        }
        
        
        // SizeFileComparator：根据文件大小排序，小文件在前，其构造器支持传一个boolean类型的参数，
        //true表示需要计算该目录下的目录大小
        //flase表示不需要计算该目录下的目录大小（0）
        
        SizeFileComparator sizeComparator = new SizeFileComparator(true);
        File[] sizeFiles = sizeComparator.sort(parentDir.listFiles());
        
        System.out.println("Sorted by size files in parent directory: ");
        for (File file: sizeFiles) {
            System.out.println("\t"+ file.getName() + " with size (kb): " + file.length());
        }
        
        
        // LastModifiedFileComparator：根据修改时间排序，最新修改排前
        LastModifiedFileComparator lastModified = new LastModifiedFileComparator();
        File[] lastModifiedFiles = lastModified.sort(parentDir.listFiles());
        
        System.out.println("Sorted by last modified files in parent directory: ");
        for (File file: lastModifiedFiles) {
            Date modified = new Date(file.lastModified());
            System.out.println("\t"+ file.getName() + " last modified on: " + modified);
        }
    }
}