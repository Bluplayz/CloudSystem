package de.bluplayz.network;

import de.bluplayz.CloudWrapper;
import de.bluplayz.netty.ConnectionListener;
import de.bluplayz.netty.NettyHandler;
import de.bluplayz.netty.PacketHandler;
import de.bluplayz.netty.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.util.function.Consumer;

public class Network {

    @Getter
    private String host = "localhost";

    @Getter
    private int port = 19132;

    @Getter
    private CloudWrapper cloudWrapper;

    @Getter
    private NettyHandler nettyHandler;

    @Getter
    private ConnectionListener connectionListener;

    @Getter
    private PacketHandler packetHandler;

    @Getter
    private Consumer<Boolean> connectingConsumer;

    public Network( CloudWrapper cloudWrapper, String host, int port ) {
        this.cloudWrapper = cloudWrapper;
        this.host = host;
        this.port = port;

        this.nettyHandler = new NettyHandler();
        this.getNettyHandler().connectToServer( this.getHost(), this.getPort(), this.connectingConsumer = new Consumer<Boolean>() {
            @Override
            public void accept( Boolean success ) {
                if ( success ) {
                    Network.this.getCloudWrapper().getLogger().info( "Successfully connected to netty server on " + Network.this.getHost() + ":" + Network.this.getPort() );
                } else {
                    Network.this.getCloudWrapper().getLogger().error( "failed to connect to netty server on " + Network.this.getHost() + ":" + Network.this.getPort() );
                    Network.this.getCloudWrapper().getLogger().error( "reconnecting in 3 Seconds...." );
                    nettyHandler.reconnectToServer( 3, this );
                }
            }
        } );

        this.getNettyHandler().registerConnectionListener( this.connectionListener = new ConnectionListener() {
            @Override
            public void channelConnected( ChannelHandlerContext ctx ) {
                Network.this.getCloudWrapper().getLogger().info( "CloudMaster connected" );
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                Network.this.getCloudWrapper().getLogger().info( "CloudMaster disconnected" );

                Network.this.getCloudWrapper().getLogger().error( "Connection lost to netty server" );
                Network.this.getCloudWrapper().getLogger().error( "reconnecting in 3 Seconds...." );
                nettyHandler.reconnectToServer( 3, Network.this.getConnectingConsumer() );
            }
        } );

        this.getNettyHandler().registerPacketHandler( this.packetHandler = new PacketHandler() {
            @Override
            public void incomingPacket( Packet packet, Channel channel ) {
            }

            @Override
            public void registerPackets() {
            }
        } );
    }
}
