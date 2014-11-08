package com.fly.practice.apachecommonsio;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.io.monitor.FileEntry;

/**
 * 文件和目录监控示例
 * @author fly
 *
 */
public final class FileMonitorExample {
    
    private static final String EXAMPLE_PATH =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/commonio.txt";
    
    private static final String PARENT_DIR =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio";
    
    private static final String NEW_DIR =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/newDir";
    
    private static final String NEW_FILE =
            "/Users/fly/work/GitHub/algorithm/src/main/java/com/fly/practice/apachecommonsio/newFile.txt";

    public static void runExample() {
        System.out.println("File Monitor example...");
        
        
        // FileEntry
        
        // We can monitor changes and get information about files
        // using the methods of this class.
        FileEntry entry = new FileEntry(FileUtils.getFile(EXAMPLE_PATH));
        
        System.out.println("File monitored: " + entry.getFile());
        System.out.println("File name: " + entry.getName());
        System.out.println("Is the file a directory?: " + entry.isDirectory());
        
        
        // 文件（目录）监控
        
        // 给指定目录创建观察者，并添加监听器，对事件作出响应
        File parentDir = FileUtils.getFile(PARENT_DIR);
        
        FileAlterationObserver observer = new FileAlterationObserver(parentDir);
        observer.addListener(new FileAlterationListenerAdaptor() {
            
                @Override
                public void onFileCreate(File file) {
                    System.out.println("File created: " + file.getName());
                }
                
                @Override
                public void onFileDelete(File file) {
                    System.out.println("File deleted: " + file.getName());
                }
                
                @Override
                public void onDirectoryCreate(File dir) {
                    System.out.println("Directory created: " + dir.getName());
                }
                
                @Override
                public void onDirectoryDelete(File dir) {
                    System.out.println("Directory deleted: " + dir.getName());
                }
        });
        
        // 创建一个每隔500毫秒就做一次检查的监控
        FileAlterationMonitor monitor = new FileAlterationMonitor(500, observer);
        try {
            monitor.start();
            //启动监控后，对监控目录做一些文件操作，触发监听器
            File newDir = new File(NEW_DIR);
            File newFile = new File(NEW_FILE);
            
            newDir.mkdirs();
            newFile.createNewFile();
                
            Thread.sleep(1000);
            
            FileDeleteStrategy.NORMAL.delete(newDir);
            FileDeleteStrategy.NORMAL.delete(newFile);
            
            Thread.sleep(1000);
            
            monitor.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}