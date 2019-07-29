package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Author: ghwei
 * @Date: 2019/7/29 8:20
 * @Description:
 */
public class NioSocketTest {
    private Selector selector;

    public void initServer(int port) throws IOException {
        //获取serverSocket通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //将该通道对应的serverSocketChannel绑定到port端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        //获取一个通道选择器
        this.selector = Selector.open();
        //将通道选择器和通道绑定，并注册为accept事件，注册该事件后，当该事件到达的时候，selector.select()hi返回，如果没有的话会一直阻塞，
        //意思就是大门交给Selector管理
        //SelectionKey中定义的4种事件
        //OP_ACCEPT接收收件
        //OP_CONNECT连接就绪事件。
        //OP_READ读就绪事件，表示已经有了可读的数据。
        //OP_WRITE写就绪事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功。。。端口为："+port);
        this.listenSelector();
    }

    public void listenSelector() throws IOException {
        //轮询访问通道选择器
        while(true){
            //当该事件到达的时候，方法返回，负责一直阻塞
            this.selector.select();
            //获取selector选中的项的迭代器，选中的 项为注册的事件
            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                //删除已经选中的key,以防重复处理。
                iterator.remove();
                //处理请求。
                handler(selectionKey);
            }

        }
    }

    private void handler(SelectionKey selectionKey) throws IOException {
        if(selectionKey.isAcceptable()){
            //处理客户端连接请求事件
            System.out.println("新的客户端连接。。");
            ServerSocketChannel server = (ServerSocketChannel)selectionKey.channel();
            //获得和客户端连接通道
            SocketChannel socket = server.accept();
            //设置成非阻塞
            socket.configureBlocking(false);
            //在和客户端连接成功以后,为了可以接收到客户端的消息,需要给通道设置读的权限
            socket.register(this.selector,SelectionKey.OP_READ);
        }else if(selectionKey.isReadable()){
            //处理客户端读的请求
            SocketChannel socket = (SocketChannel) selectionKey.channel();
            //创建读取的缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int readData = socket.read(byteBuffer);
            if(readData > 0){
                //先将缓冲区数据转换为字节数组,然后再转换为string
                String message = new String(byteBuffer.array(), "GBK");
                System.out.println("收到的消息:"+ message);
                //回写数据
                ByteBuffer backByteBuffer = ByteBuffer.wrap("receive data".getBytes("GBK"));
                socket.write(backByteBuffer);
            }else{
                System.out.println("客户端关闭");
                //selectionKey对象会失效,这意味这Selector不会再监控与他相关的事情
                selectionKey.cancel();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioSocketTest nioSocketTest = new NioSocketTest();
        nioSocketTest.initServer(9999);
    }
}
