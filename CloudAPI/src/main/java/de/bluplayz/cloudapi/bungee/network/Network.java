package de.bluplayz.cloudapi.bungee.network;

import de.bluplayz.cloudapi.bungee.BungeeCloudAPI;
import de.bluplayz.cloudapi.bungee.locale.LocaleAPI;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
import de.bluplayz.cloudlib.packet.DispatchCommandPacket;
import de.bluplayz.cloudlib.packet.RegisterServerPacket;
import de.bluplayz.cloudlib.packet.UnregisterServerPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
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
                //System.out.println( "Incoming Packet: " + packet.toString() );
                if ( packet instanceof RegisterServerPacket ) {
                    RegisterServerPacket registerServerPacket = (RegisterServerPacket) packet;

                    if ( ProxyServer.getInstance().getServers().containsKey( registerServerPacket.getServerData().getName() ) ) {
                        return;
                    }

                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                            registerServerPacket.getServerData().getName(),
                            InetSocketAddress.createUnresolved( registerServerPacket.getServerData().getHost(), registerServerPacket.getServerData().getPort() ),
                            registerServerPacket.getServerData().getMotd(),
                            false
                    );
                    ProxyServer.getInstance().getServers().put( registerServerPacket.getServerData().getName(), serverInfo );

                    if ( Network.this.getBungeeCloudAPI().getProxyFallbackPriorities().contains( registerServerPacket.getServerData().getServerGroup().getName() ) ) {
                        // Add to fallback Server
                        ProxyServer.getInstance().getConfig().getListeners().iterator().next().getServerPriority().add( registerServerPacket.getServerData().getName() );
                    }
                    return;
                }

                if ( packet instanceof UnregisterServerPacket ) {
                    UnregisterServerPacket unregisterServerPacket = (UnregisterServerPacket) packet;

                    if ( !ProxyServer.getInstance().getServers().containsKey( unregisterServerPacket.getServerData().getName() ) ) {
                        return;
                    }

                    ProxyServer.getInstance().getServers().remove( unregisterServerPacket.getServerData().getName() );

                    if ( Network.this.getBungeeCloudAPI().getProxyFallbackPriorities().contains( unregisterServerPacket.getServerData().getServerGroup().getName() ) ) {
                        // Remove from fallback Server
                        ProxyServer.getInstance().getConfig().getListeners().iterator().next().getServerPriority().remove( unregisterServerPacket.getServerData().getName() );
                    }
                    return;
                }

                if ( packet instanceof DispatchCommandPacket ) {
                    DispatchCommandPacket dispatchCommandPacket = (DispatchCommandPacket) packet;
                    ProxyServer.getInstance().getPluginManager().dispatchCommand( ProxyServer.getInstance().getConsole(), dispatchCommandPacket.getCommandline() );
                    return;
                }
            }

            @Override
            public void registerPackets() {
            }
        } );
    }
}
