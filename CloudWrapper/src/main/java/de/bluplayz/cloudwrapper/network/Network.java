package de.bluplayz.cloudwrapper.network;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
import de.bluplayz.cloudlib.packet.DispatchCommandPacket;
import de.bluplayz.cloudlib.packet.SaveServerPacket;
import de.bluplayz.cloudlib.packet.ServerStartedPacket;
import de.bluplayz.cloudlib.packet.StartServerPacket;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.ServerData;
import de.bluplayz.cloudlib.server.template.Template;
import de.bluplayz.cloudwrapper.locale.LocaleAPI;
import de.bluplayz.cloudwrapper.server.BungeeCordProxy;
import de.bluplayz.cloudwrapper.server.SpigotServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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

                    if ( startServerPacket.getServerData().getTemplate().getType() == Template.Type.PROXY ) {
                        BungeeCordProxy bungeeCordProxy = new BungeeCordProxy( startServerPacket.getServerData() );
                        Network.this.getCloudWrapper().getBungeeCordProxies().add( bungeeCordProxy );
                        bungeeCordProxy.startProxy();
                    } else {
                        SpigotServer spigotServer = new SpigotServer( startServerPacket.getServerData() );
                        Network.this.getCloudWrapper().getSpigotServers().add( spigotServer );
                        spigotServer.startServer();
                    }
                    return;
                }

                if ( packet instanceof ServerStartedPacket ) {
                    ServerStartedPacket serverStartedPacket = (ServerStartedPacket) packet;

                    SpigotServer spigotServer = Network.this.getCloudWrapper().getServerByName( serverStartedPacket.getName() );
                    if ( spigotServer != null ) {
                        spigotServer.setActiveMode( ActiveMode.ONLINE );
                        LocaleAPI.log( "network_server_started_successfully", spigotServer.getName(), spigotServer.getUniqueId().toString(), spigotServer.getPort() );
                        return;
                    }

                    BungeeCordProxy bungeeCordProxy = Network.this.getCloudWrapper().getProxyByName( serverStartedPacket.getName() );
                    if ( bungeeCordProxy != null ) {
                        bungeeCordProxy.setActiveMode( ActiveMode.ONLINE );
                        LocaleAPI.log( "network_server_started_successfully", bungeeCordProxy.getName(), bungeeCordProxy.getUniqueId().toString(), bungeeCordProxy.getPort() );
                        return;
                    }
                    return;
                }

                if ( packet instanceof SaveServerPacket ) {
                    SaveServerPacket saveServerPacket = (SaveServerPacket) packet;

                    SpigotServer spigotServer = Network.this.getCloudWrapper().getServerByName( saveServerPacket.getTargetServerName() );
                    BungeeCordProxy bungeeCordProxy = Network.this.getCloudWrapper().getProxyByName( saveServerPacket.getTargetServerName() );

                    if ( spigotServer == null && bungeeCordProxy == null ) {
                        return;
                    }

                    ServerData serverData = spigotServer != null ? spigotServer : bungeeCordProxy;

                    File templateDirectory = new File( saveServerPacket.getTargetTemplate().getTemplateFolder() );
                    File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + serverData.getTemplate().getName() + "/" + serverData.getName() );

                    Network.this.copyDirectoryRecursive( serverDirectory, templateDirectory );
                    return;
                }
            }

            @Override
            public void registerPackets() {
            }
        } );
    }

    private void copyDirectoryRecursive( File src, File dest ) {
        try {
            for ( File file : src.listFiles() ) {
                if ( file.isDirectory() ) {
                    this.copyDirectoryRecursive(file, new File( dest, file.getName() ));
                } else {
                    FileUtils.copyFileToDirectory( file, dest );
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
