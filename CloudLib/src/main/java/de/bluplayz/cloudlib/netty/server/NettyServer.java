package de.bluplayz.cloudlib.netty.server;

import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.packet.PacketDecoder;
import de.bluplayz.cloudlib.netty.packet.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NettyServer {

    public static final boolean EPOLL = Epoll.isAvailable();
    public static ExecutorService POOL = Executors.newCachedThreadPool();

    @Getter
    private EventLoopGroup eventLoopGroup;
    @Getter
    private ServerBootstrap bootstrap;
    @Getter
    private ChannelFuture future;

    @Getter
    @Setter
    private int port = 8000;

    public void startServer( int port, Consumer<Boolean> consumer ) {
        this.setPort( port );

        if ( getFuture() != null ) {
            this.stopServer();
        }

        NettyServer.POOL.execute( () -> {
            this.eventLoopGroup = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
            try {
                this.bootstrap = new ServerBootstrap()
                        .group( this.getEventLoopGroup() )
                        .channel( EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class )
                        .childHandler( new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel( SocketChannel channel ) throws Exception {
                                channel.pipeline().addLast( new PacketEncoder() );
                                channel.pipeline().addLast( new PacketDecoder() );
                                channel.pipeline().addLast( new ServerHandler( NettyServer.this ) );
                            }
                        } );

                this.future = this.getBootstrap().bind( this.getPort() );
                this.getFuture().sync();
                consumer.accept(true);
                this.getFuture().sync().channel().closeFuture().syncUninterruptibly();
            } catch ( Exception e ) {
                if( NettyHandler.DEBUGMODE ){
                    e.printStackTrace();
                }

                consumer.accept( false );
            } finally {
                if ( this.getEventLoopGroup() != null ) {
                    this.getEventLoopGroup().shutdownGracefully();
                }
            }
        } );
    }

    public void stopServer() {
        if ( this.getFuture() == null ) {
            return;
        }

        for ( Channel channel : NettyHandler.getClients().values() ) {
            channel.close();
        }

        this.future = null;
        this.bootstrap = null;
        this.eventLoopGroup = null;
    }
}
