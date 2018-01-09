package de.bluplayz.cloudapi.bukkit.network;

import de.bluplayz.cloudapi.bukkit.BukkitCloudAPI;
import de.bluplayz.cloudapi.bukkit.locale.LocaleAPI;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.netty.packet.defaults.SetNamePacket;
import de.bluplayz.cloudlib.packet.DispatchCommandPacket;
import de.bluplayz.cloudlib.packet.VerifyPlayerPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public class Network {

    @Getter
    private String host = "localhost";

    @Getter
    private int port = 19132;

    @Getter
    private BukkitCloudAPI bukkitCloudAPI;

    @Getter
    private NettyHandler nettyHandler;

    @Getter
    private ConnectionListener connectionListener;

    @Getter
    private PacketHandler packetHandler;

    @Getter
    private Consumer<Boolean> connectingConsumer;

    public Network( BukkitCloudAPI bukkitCloudAPI, String host, int port ) {
        this.bukkitCloudAPI = bukkitCloudAPI;
        this.host = host;
        this.port = port;

        this.nettyHandler = new NettyHandler();
        this.getNettyHandler().connectToServer( this.getHost(), this.getPort(), this.connectingConsumer = new Consumer<Boolean>() {
            @Override
            public void accept( Boolean success ) {
                if ( success ) {
                    LocaleAPI.log( "network_master_connected", Network.this.getHost() + ":" + Network.this.getPort() );

                    SetNamePacket setNamePacket = new SetNamePacket( Network.this.getBukkitCloudAPI().getServerName() );
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
                if ( packet instanceof DispatchCommandPacket ) {
                    DispatchCommandPacket dispatchCommandPacket = (DispatchCommandPacket) packet;
                    Bukkit.dispatchCommand( Bukkit.getConsoleSender(), dispatchCommandPacket.getCommandline() );
                    return;
                }

                if ( packet instanceof VerifyPlayerPacket ) {
                    VerifyPlayerPacket verifyPlayerPacket = (VerifyPlayerPacket) packet;
                    if ( Network.this.getBukkitCloudAPI().getAllowedPlayers().contains( verifyPlayerPacket.getPlayer() ) ) {
                        return;
                    }

                    Network.this.getBukkitCloudAPI().getAllowedPlayers().add( verifyPlayerPacket.getPlayer() );
                    System.out.println( "Verify " + verifyPlayerPacket.getPlayer().toString() + " at " + System.currentTimeMillis() );

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if ( !Network.this.getBukkitCloudAPI().getAllowedPlayers().contains( verifyPlayerPacket.getPlayer() ) ) {
                                return;
                            }

                            Network.this.getBukkitCloudAPI().getAllowedPlayers().add( verifyPlayerPacket.getPlayer() );
                        }
                    }.runTaskLater( Network.this.getBukkitCloudAPI(), 5 * 20 );
                    return;
                }
            }

            @Override
            public void registerPackets() {
            }
        } );
    }
}
