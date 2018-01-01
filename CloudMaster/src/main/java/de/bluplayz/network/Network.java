package de.bluplayz.network;

import de.bluplayz.CloudMaster;
import de.bluplayz.netty.ConnectionListener;
import de.bluplayz.netty.NettyHandler;
import de.bluplayz.netty.PacketHandler;
import de.bluplayz.netty.packet.Packet;
import de.bluplayz.server.BungeeCordProxy;
import de.bluplayz.server.CloudServer;
import de.bluplayz.server.SpigotServer;
import de.bluplayz.server.Template;
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
                this.getCloudMaster().getLogger().info( "Successfully started netty server on port " + this.getPort() );
            } else {
                this.getCloudMaster().getLogger().error( "failed to start netty server" );
            }
        } );

        this.getNettyHandler().registerConnectionListener( this.connectionListener = new ConnectionListener() {
            @Override
            public void channelConnected( ChannelHandlerContext ctx ) {
                if ( !Network.this.getWhitelist().contains( ctx.channel().remoteAddress().toString().substring( 1 ).split( ":" )[0] ) ) {
                    ctx.close();
                    return;
                }

                CloudServer cloudServer = Network.this.getCloudMaster().getServerManager().addCloudServer( ctx.channel() );
                cloudServer.startProxies( new BungeeCordProxy( cloudServer, Template.getProxyTemplates().get( 0 ) ) );
                cloudServer.startServers( new SpigotServer( cloudServer, Template.getSpigotTemplates().get( 0 ) ), new SpigotServer( cloudServer, Template.getSpigotTemplates().get( 0 ) ) );
                Network.this.getCloudMaster().getLogger().info( "CloudServer connected from " + ctx.channel().remoteAddress().toString().substring( 1 ) );
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                if ( Network.this.getCloudMaster().getServerManager().getCloudServerByChannel( ctx.channel() ) == null ) {
                    ctx.close();
                    return;
                }

                Network.this.getCloudMaster().getServerManager().removeCloudServer( ctx.channel() );
                Network.this.getCloudMaster().getLogger().info( "CloudServer disconnected from " + ctx.channel().remoteAddress().toString().substring( 1 ) );
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
