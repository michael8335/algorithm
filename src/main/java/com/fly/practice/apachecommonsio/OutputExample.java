package com.fly.practice.apachecommonsio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;

public final class OutputExample {
    
    private static final String INPUT = "This should go to the output.";

    public static void runExample() {
        System.out.println("Output example...");
        TeeInputStream tee=null;
        TeeInputStream teeIn = null;
        TeeOutputStream teeOut = null;
        
        try {
            //TeeInputStream能将输入流快速拷贝到输出流中
            ByteArrayInputStream in = new ByteArrayInputStream(INPUT.getBytes("US-ASCII"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            tee = new TeeInputStream(in, out, true);
            tee.read(new byte[INPUT.length()]);
            System.out.println("Output stream: " + out.toString());      
            //能将输入流同时拷贝到两个输出流
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            
            teeOut = new TeeOutputStream(out1, out2);
            teeIn = new TeeInputStream(in, teeOut, true);
            teeIn.read(new byte[INPUT.length()]);

            System.out.println("Output stream 1: " + out1.toString());
            System.out.println("Output stream 2: " + out2.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //此处不需要关闭teeOut，当关闭teeIn时，同时会关闭teeOut
            try { teeIn.close();
            tee.close();}
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}