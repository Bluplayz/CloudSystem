package de.bluplayz.netty.client;

import de.bluplayz.netty.NettyHandler;
import de.bluplayz.netty.packet.PacketDecoder;
import de.bluplayz.netty.packet.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NettyClient {

    public static final boolean EPOLL = Epoll.isAvailable();
    public static ExecutorService POOL = Executors.newCachedThreadPool();

    @Getter
    private EventLoopGroup eventLoopGroup;

    @Getter
    private Bootstrap bootstrap;

    @Getter
    private ChannelFuture future;

    @Getter
    @Setter
    private String host = "localhost";

    @Getter
    @Setter
    private int port = 8000;

    @Getter
    @Setter
    private Channel channel;

    @Getter
    @Setter
    private Consumer<Boolean> consumer;

    public void connect( String host, int port, Consumer<Boolean> consumer ) {
        this.setHost( host );
        this.setPort( port );
        this.setConsumer( consumer );

        NettyClient.POOL.execute( () -> {
            this.eventLoopGroup = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
            try {
                this.bootstrap = new Bootstrap()
                        .group( this.getEventLoopGroup() )
                        .channel( EPOLL ? EpollSocketChannel.class : NioSocketChannel.class )
                        .handler( new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel( SocketChannel channel ) throws Exception {
                                channel.pipeline().addLast( new PacketEncoder() );
                                channel.pipeline().addLast( new PacketDecoder() );
                                channel.pipeline().addLast( new ClientHandler( NettyClient.this ) );
                            }
                        } );
                this.future = this.getBootstrap().connect( this.getHost(), this.getPort() );

                this.getFuture().sync();
                consumer.accept( true );

                this.getFuture().sync().channel().closeFuture().syncUninterruptibly();
            } catch ( Exception e ) {
                if ( NettyHandler.DEBUGMODE ) {
                    e.printStackTrace();
                }

                this.setChannel( null );
                consumer.accept( false );
            } finally {
                this.getEventLoopGroup().shutdownGracefully();
                this.setChannel( null );
            }
        } );
    }

    public void scheduleConnect( int time ) {
        if ( this.isConnected() ) {
            return;
        }

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        NettyClient.this.connect( getHost(), getPort(), NettyClient.this.getConsumer() );
                    }
                },
                time
        );
    }

    public void disconnect() {
        try {
            if ( this.getFuture() == null || this.getChannel() == null ) {
                return;
            }

            if ( !this.getChannel().isActive() ) {
                return;
            }

            this.getChannel().close();

            this.setChannel( null );
            this.future = null;
            this.bootstrap = null;
            this.eventLoopGroup = null;
        } catch ( Exception ignored ) {
        }
    }

    public void reconnect() {
        this.scheduleConnect( 0 );
    }

    public boolean isConnected() {
        return this.getChannel() != null && this.getChannel().isActive();
    }
}
