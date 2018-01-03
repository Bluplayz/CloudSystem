package de.bluplayz.cloudwrapper.network;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.logging.Logger;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
import de.bluplayz.cloudlib.packet.CommandSendPacket;
import de.bluplayz.cloudlib.packet.StartProxyPacket;
import de.bluplayz.cloudlib.packet.StartServerPacket;
import de.bluplayz.cloudwrapper.locale.LocaleAPI;
import de.bluplayz.cloudwrapper.server.BungeeCordProxy;
import de.bluplayz.cloudwrapper.server.SpigotServer;
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
                    LocaleAPI.log( "network_master_connected", Network.this.getHost() + ":" + Network.this.getPort() );

                    SetNamePacket setNamePacket = new SetNamePacket( "Unnamed-Wrapper" );
                    Network.this.getPacketHandler().sendPacket( setNamePacket );
                } else {
                    LocaleAPI.log( "network_master_failed_connection", Network.this.getHost() + ":" + Network.this.getPort() );
                    LocaleAPI.log( "network_master_failed_connection_reconnect" );
                    nettyHandler.reconnectToServer( 3, this );
                }
            }
        } );

        this.getNettyHandler().registerConnectionListener( this.connectionListener = new ConnectionListener() {
            @Override
            public void channelConnected( ChannelHandlerContext ctx ) {
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                for ( BungeeCordProxy bungeeCordProxy : Network.this.getCloudWrapper().getBungeeCordProxies() ) {
                    bungeeCordProxy.shutdown();
                }
                for ( SpigotServer spigotServer : Network.this.getCloudWrapper().getSpigotServers() ) {
                    spigotServer.shutdown();
                }

                LocaleAPI.log( "network_master_connection_lost", Network.this.getHost() + ":" + Network.this.getPort() );
                LocaleAPI.log( "network_master_failed_connection_reconnect" );
                nettyHandler.reconnectToServer( 3, Network.this.getConnectingConsumer() );

            }
        } );

        this.getNettyHandler().registerPacketHandler( this.packetHandler = new PacketHandler() {
            @Override
            public void incomingPacket( Packet packet, Channel channel ) {
                if ( packet instanceof StartServerPacket ) {
                    StartServerPacket startServerPacket = (StartServerPacket) packet;

                    SpigotServer spigotServer = new SpigotServer( startServerPacket.getServer() );
                    Network.this.getCloudWrapper().getSpigotServers().add( spigotServer );
                    spigotServer.startServer();
                    return;
                }

                if ( packet instanceof StartProxyPacket ) {
                    StartProxyPacket startProxyPacket = (StartProxyPacket) packet;

                    BungeeCordProxy bungeeCordProxy = new BungeeCordProxy( startProxyPacket.getProxy() );
                    Network.this.getCloudWrapper().getBungeeCordProxies().add( bungeeCordProxy );
                    bungeeCordProxy.startProxy();
                    return;
                }

                if ( packet instanceof CommandSendPacket ) {
                    CommandSendPacket commandSendPacket = (CommandSendPacket) packet;

                    SpigotServer spigotServer = Network.this.getCloudWrapper().getServerByName( commandSendPacket.getServername() );
                    if ( spigotServer != null ) {
                        spigotServer.execute( commandSendPacket.getCommandline() );
                        return;
                    }

                    BungeeCordProxy bungeeCordProxy = Network.this.getCloudWrapper().getProxyByName( commandSendPacket.getServername() );
                    if ( bungeeCordProxy != null ) {
                        bungeeCordProxy.execute( commandSendPacket.getCommandline() );
                        return;
                    }
                    return;
                }
            }

            @Override
            public void registerPackets() {
            }
        } );
    }
}
