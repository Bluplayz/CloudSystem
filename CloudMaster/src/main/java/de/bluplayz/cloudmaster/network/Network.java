package de.bluplayz.cloudmaster.network;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudmaster.server.BungeeCordProxy;
import de.bluplayz.cloudmaster.server.CloudWrapper;
import de.bluplayz.cloudmaster.server.SpigotServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Network {

    @Getter
    public int port = 19132;

    @Getter
    private CloudMaster cloudMaster;

    @Getter
    private NettyHandler nettyHandler;

    @Getter
    private ConnectionListener connectionListener;

    @Getter
    private PacketHandler packetHandler;

    @Getter
    private List<String> whitelist = new ArrayList<>();

    public Network( CloudMaster cloudMaster, int port, List<String> whitelist ) {
        this.cloudMaster = cloudMaster;
        this.port = port;
        this.whitelist = whitelist;

        this.nettyHandler = new NettyHandler();
        this.getNettyHandler().startServer( this.getPort(), success -> {
            if ( success ) {
                LocaleAPI.log( "network_netty_started_successfully", this.getPort() );
            } else {
                LocaleAPI.log( "network_netty_starting_failed" );
            }
        } );

        this.getNettyHandler().registerConnectionListener( this.connectionListener = new ConnectionListener() {
            @Override
            public void channelConnected( ChannelHandlerContext ctx ) {
                if ( !Network.this.getWhitelist().contains( ctx.channel().remoteAddress().toString().substring( 1 ).split( ":" )[0] ) ) {
                    //Logger.getGlobal().warning( "Not Whitelisted IP(" + ctx.channel().remoteAddress().toString().substring( 1 ).split( ":" )[0] + ") want to connect!" );
                    ctx.close();
                    return;
                }

                //Network.this.getCloudMaster().getServerManager().checkForServers();
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                if ( !Network.this.getWhitelist().contains( ctx.channel().remoteAddress().toString().substring( 1 ).split( ":" )[0] ) ) {
                    //Logger.getGlobal().warning( "Not Whitelisted IP(" + ctx.channel().remoteAddress().toString().substring( 1 ).split( ":" )[0] + ") want to disconnect!" );
                    ctx.close();
                    return;
                }

                if ( Network.this.getCloudMaster().getServerManager().getCloudWrapperByChannel( ctx.channel() ) != null ) {
                    // CloudWrapper disconnected
                    CloudWrapper cloudWrapper = Network.this.getCloudMaster().getServerManager().removeCloudWrapper( ctx.channel() );
                    LocaleAPI.log( "network_wrapper_disconnected", cloudWrapper.getName(), ctx.channel().remoteAddress().toString().substring( 1 ) );
                    //Network.this.getCloudMaster().getServerManager().checkForServers();
                } else {
                    // Bukkit- or Bungee Server disconnected
                    //Network.this.getCloudMaster().getServerManager().checkForServers();
                }
            }
        } );

        this.getNettyHandler().registerPacketHandler( this.packetHandler = new PacketHandler() {
            @Override
            public void incomingPacket( Packet packet, Channel channel ) {
                if ( packet instanceof SetNamePacket ) {
                    SetNamePacket setNamePacket = (SetNamePacket) packet;
                    if ( setNamePacket.getName().equalsIgnoreCase( "Unnamed-Wrapper" ) ) {
                        // CloudWrapper
                        CloudWrapper cloudWrapper = Network.this.getCloudMaster().getServerManager().addCloudWrapper( channel );
                        LocaleAPI.log( "network_wrapper_connected", cloudWrapper.getName(), channel.remoteAddress().toString().substring( 1 ) );
                        setNamePacket.setName( cloudWrapper.getName() );

                        Network.this.getCloudMaster().getServerManager().checkForServers();
                    } else {
                        if ( Network.this.getCloudMaster().getServerManager().getServerByName( setNamePacket.getName() ) != null ) {
                            // Spigot
                            SpigotServer spigotServer = Network.this.getCloudMaster().getServerManager().getServerByName( setNamePacket.getName() );

                            if ( spigotServer.getActiveMode() == ActiveMode.ONLINE ) {
                                return;
                            }

                            spigotServer.setActiveMode( ActiveMode.ONLINE );
                            LocaleAPI.log( "network_server_started_successfully", spigotServer.getName(), spigotServer.getPort() );
                            //Network.this.getCloudMaster().getServerManager().checkForServers();
                        } else {
                            // Bungee
                            BungeeCordProxy bungeeCordProxy = Network.this.getCloudMaster().getServerManager().getProxyByName( setNamePacket.getName() );

                            if ( bungeeCordProxy.getActiveMode() == ActiveMode.ONLINE ) {
                                return;
                            }

                            bungeeCordProxy.setActiveMode( ActiveMode.ONLINE );
                            LocaleAPI.log( "network_server_started_successfully", bungeeCordProxy.getName(), bungeeCordProxy.getPort() );
                            //Network.this.getCloudMaster().getServerManager().checkForServers();
                        }
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
