package com.fly.practice.nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import javax.swing.text.html.HTMLDocument.Iterator;
/**
* Simple echo-back server which listens for incoming stream connections and
* echoes back whatever it reads. A single Selector object is used to listen to
* the server socket (to accept new connections) and all the active socket
* channels.
* @author zale (zalezone.cn)
*/
public class SelectSockets {
    public static int PORT_NUMBER = 1234;
    public static void main(String[] argv) throws Exception 
    {
        new SelectSockets().go(argv);
    }
    public void go(String[] argv) throws Exception 
    {
        int port = PORT_NUMBER;
        if (argv.length > 0) 
        { // 覆盖默认的监听端口
            port = Integer.parseInt(argv[0]);
        }
        System.out.println("Listening on port " + port);
        ServerSocketChannel serverChannel = ServerSocketChannel.open();// 打开一个未绑定的serversocketchannel
        ServerSocket serverSocket = serverChannel.socket();// 得到一个ServerSocket去和它绑定 
        Selector selector = Selector.open();// 创建一个Selector供下面使用
        serverSocket.bind(new InetSocketAddress(port));//设置server channel将会监听的端口
        serverChannel.configureBlocking(false);//设置非阻塞模式
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);//将ServerSocketChannel注册到Selector
        while (true) 
        {
            // This may block for a long time. Upon returning, the
            // selected set contains keys of the ready channels.
            int n = selector.select();
            if (n == 0) 
            {
                continue; // nothing to do
            }           
            java.util.Iterator<SelectionKey> it = selector.selectedKeys().iterator();// Get an iterator over the set of selected keys
            //在被选择的set中遍历全部的key
            while (it.hasNext()) 
            {
                SelectionKey key = (SelectionKey) it.next();
                // 判断是否是一个连接到来
                if (key.isAcceptable()) 
                {
                    ServerSocketChannel server =(ServerSocketChannel) key.channel();
                    SocketChannel channel = server.accept();
                    registerChannel(selector, channel,SelectionKey.OP_READ);//注册读事件
                    sayHello(channel);//对连接进行处理
                }
                //判断这个channel上是否有数据要读
                if (key.isReadable()) 
                {
                    readDataFromSocket(key);
                }
                //从selected set中移除这个key，因为它已经被处理过了
                it.remove();
            }
        }
    }
    // ----------------------------------------------------------
    /**
    * Register the given channel with the given selector for the given
    * operations of interest
    */
    protected void registerChannel(Selector selector,SelectableChannel channel, int ops) throws Exception
    {
        if (channel == null) 
        {
            return; // 可能会发生
        }
        // 设置通道为非阻塞
        channel.configureBlocking(false);
        // 将通道注册到选择器上
        channel.register(selector, ops);
    }
    // ----------------------------------------------------------
    // Use the same byte buffer for all channels. A single thread is
    // servicing all the channels, so no danger of concurrent acccess.
    //对所有的通道使用相同的缓冲区。单线程为所有的通道进行服务，所以并发访问没有风险
    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    /**
    * Sample data handler method for a channel with data ready to read.
    * 对于一个准备读入数据的通道的简单的数据处理方法
    * @param key
    *
    A SelectionKey object associated with a channel determined by
    the selector to be ready for reading. If the channel returns
    an EOF condition, it is closed here, which automatically
    invalidates the associated key. The selector will then
    de-register the channel on the next select call.
    
    一个选择器决定了和通道关联的SelectionKey object是准备读状态。如果通道返回EOF，通道将被关闭。
    并且会自动使相关的key失效，选择器然后会在下一次的select call时取消掉通道的注册
    */
    protected void readDataFromSocket(SelectionKey key) throws Exception 
    {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int count;
        buffer.clear(); // 清空Buffer
        // Loop while data is available; channel is nonblocking
        //当可以读到数据时一直循环，通道为非阻塞
        while ((count = socketChannel.read(buffer)) > 0) 
        {
            buffer.flip(); // 将缓冲区置为可读
            // Send the data; don't assume it goes all at once
            //发送数据，不要期望能一次将数据发送完
            while (buffer.hasRemaining()) 
            {
                socketChannel.write(buffer);
            }
            // WARNING: the above loop is evil. Because
            // it's writing back to the same nonblocking
            // channel it read the data from, this code can
            // potentially spin in a busy loop. In real life
            // you'd do something more useful than this.
            //这里的循环是无意义的，具体按实际情况而定
            buffer.clear(); // Empty buffer
        }
        if (count < 0) 
        {
            // Close channel on EOF, invalidates the key
            //读取结束后关闭通道，使key失效
            socketChannel.close();
        }
    }
    // ----------------------------------------------------------
    /**
    * Spew a greeting to the incoming client connection.
    *
    * @param channel
    *
    The newly connected SocketChannel to say hello to.
    */
    private void sayHello(SocketChannel channel) throws Exception 
    {
        buffer.clear();
        buffer.put("Hi there!\r\n".getBytes());
        buffer.flip();
        channel.write(buffer);
    }
}