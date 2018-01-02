package de.bluplayz.cloudmaster.network;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudmaster.server.CloudWrapper;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudlib.netty.ConnectionListener;
import de.bluplayz.cloudlib.netty.NettyHandler;
import de.bluplayz.cloudlib.netty.PacketHandler;
import de.bluplayz.cloudlib.netty.packet.Packet;
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
                    ctx.close();
                    return;
                }

                CloudWrapper cloudWrapper = Network.this.getCloudMaster().getServerManager().addCloudWrapper( ctx.channel() );
                LocaleAPI.log( "network_wrapper_connected", cloudWrapper.getName(), ctx.channel().remoteAddress().toString().substring( 1 ) );
                Network.this.getCloudMaster().getServerManager().checkForServers();
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                if ( Network.this.getCloudMaster().getServerManager().getCloudWrapperByChannel( ctx.channel() ) == null ) {
                    ctx.close();
                    return;
                }

                CloudWrapper cloudWrapper = Network.this.getCloudMaster().getServerManager().removeCloudWrapper( ctx.channel() );
                LocaleAPI.log( "network_wrapper_disconnected", cloudWrapper.getName(), ctx.channel().remoteAddress().toString().substring( 1 ) );
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
