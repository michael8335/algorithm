package com.fly.practice.process;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class RemoteCommandExec {
    public static void exec(String ip, String command) {
        String username = "root";  //用户名
        String password = "admin01519";  //密码
        try {
            Connection conn = new Connection(ip);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username,
                    password);
            if (false == isAuthenticated) {
                throw new IOException("Authentication failed.");
            }
            Session session = conn.openSession();
            session.execCommand(command);

            String charset = Charset.defaultCharset().toString();
            InputStream stdOut = new StreamGobbler(session.getStdout());
            String outStr = processStream(stdOut, charset);

            InputStream stdErr = new StreamGobbler(session.getStderr());
            String errStr = processStream(stdErr, charset);
            
            System.out.println("stdErr:"+errStr);
            System.out.println("stdOut:"+outStr);

            session.close();
            conn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  

    }

    private static String processStream(InputStream in, String charset)
            throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }
        return sb.toString();
    }

    /*
     * sshxcute执行shell脚本，但生成的是单例模式 public static void exec(String ip, String
     * command){ ConnBean conn=new ConnBean(ip,"root","admin01519"); CustomTask
     * commandtask=new ExecCommand(command); SSHExec
     * ssh=SSHExec.getInstance(conn);
     * 
     * ssh.setOption(IOptionName.INTEVAL_TIME_BETWEEN_TASKS, 1000l); try{
     * ssh.connect(); Result result=ssh.exec(commandtask); if(result.isSuccess){
     * System.out.println("Return code:"+result.rc);
     * System.out.println("sysout:"+result.sysout); } else{
     * System.out.println("Return code:"+result.rc);
     * System.out.println("error_msg:"+result.error_msg); } }catch(Exception e){
     * e.printStackTrace(); }finally{ ssh.disconnect(); }
     * 
     * }
     */

    
}

