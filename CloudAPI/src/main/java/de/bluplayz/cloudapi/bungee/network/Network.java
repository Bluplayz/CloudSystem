package de.bluplayz.cloudapi.bungee.network;

import de.bluplayz.cloudapi.bungee.BungeeCloudAPI;
import de.bluplayz.cloudapi.bungee.locale.LocaleAPI;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
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
    private BungeeCloudAPI bungeeCloudAPI;

    @Getter
    private NettyHandler nettyHandler;

    @Getter
    private ConnectionListener connectionListener;

    @Getter
    private PacketHandler packetHandler;

    @Getter
    private Consumer<Boolean> connectingConsumer;

    public Network( BungeeCloudAPI bungeeCloudAPI, String host, int port ) {
        this.bungeeCloudAPI = bungeeCloudAPI;
        this.host = host;
        this.port = port;

        this.nettyHandler = new NettyHandler();
        this.getNettyHandler().connectToServer( this.getHost(), this.getPort(), this.connectingConsumer = new Consumer<Boolean>() {
            @Override
            public void accept( Boolean success ) {
                if ( success ) {
                    LocaleAPI.log( "network_master_connected", Network.this.getHost() + ":" + Network.this.getPort() );

                    SetNamePacket setNamePacket = new SetNamePacket( Network.this.getBungeeCloudAPI().getServerName() );
                    Network.this.getPacketHandler().sendPacket( setNamePacket );
                } else {
                    LocaleAPI.log( "network_master_failed_connection", Network.this.getHost() + ":" + Network.this.getPort() );
                    LocaleAPI.log( "network_master_failed_connection_reconnect" );
                    Network.this.nettyHandler.reconnectToServer( 3, this );
                }
            }
        } );

        this.getNettyHandler().registerConnectionListener( this.connectionListener = new ConnectionListener() {
            @Override
            public void channelConnected( ChannelHandlerContext ctx ) {
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                LocaleAPI.log( "network_master_connection_lost", Network.this.getHost() + ":" + Network.this.getPort() );
                LocaleAPI.log( "network_master_failed_connection_reconnect" );
                Network.this.nettyHandler.reconnectToServer( 3, Network.this.getConnectingConsumer() );

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
