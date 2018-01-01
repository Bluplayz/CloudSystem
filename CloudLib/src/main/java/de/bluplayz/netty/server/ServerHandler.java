package de.bluplayz.netty.server;

import de.bluplayz.netty.ConnectionListener;
import de.bluplayz.netty.NettyHandler;
import de.bluplayz.netty.PacketHandler;
import de.bluplayz.netty.packet.Packet;
import de.bluplayz.netty.packet.defaults.DisconnectPacket;
import de.bluplayz.netty.packet.defaults.SetNamePacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;

import java.util.Map;

public class ServerHandler extends SimpleChannelInboundHandler<Packet> {

    @Getter
    private Channel channel = null;

    @Getter
    private NettyServer nettyServer;

    public ServerHandler( NettyServer nettyServer ) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        if ( NettyHandler.DEBUGMODE ) {
            super.exceptionCaught( ctx, cause );
        }
    }

    protected void channelRead0( ChannelHandlerContext ctx, Packet packet ) throws Exception {
        for ( PacketHandler handler : NettyHandler.getPacketHandlers() ) {
            handler.incomingPacket( packet, this.getChannel() );
        }

        if ( packet instanceof DisconnectPacket ) {
            this.getChannel().close();
        }

        NettyHandler.getInstance().runPacketCallbacks( packet );

        if ( packet instanceof SetNamePacket ) {
            SetNamePacket setNamePacket = (SetNamePacket) packet;

            // Remove old entry
            String oldname = "";
            for ( Map.Entry entry : NettyHandler.getClients().entrySet() ) {
                String name = (String) entry.getKey();
                Channel channel = (Channel) entry.getValue();

                if ( channel == ctx.channel() ) {
                    oldname = name;
                    break;
                }
            }

            // Remove and add with new name
            if ( !oldname.equalsIgnoreCase( "" ) ) {
                NettyHandler.getClients().remove( oldname );
                NettyHandler.getClients().put( setNamePacket.getName(), ctx.channel() );
            }
        }
    }

    @Override
    public void channelActive( ChannelHandlerContext ctx ) throws Exception {
        this.channel = ctx.channel();

        for ( ConnectionListener handler : NettyHandler.getConnectionListeners() ) {
            handler.channelConnected( ctx );
        }

        NettyHandler.getClients().put( "Channel" + NettyHandler.getClients().size() + 1, ctx.channel() );
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx ) throws Exception {
        //Logger.log( "Client disconnected" );
        this.channel = null;

        for ( ConnectionListener handler : NettyHandler.getConnectionListeners() ) {
            handler.channelDisconnected( ctx );
        }

        if ( NettyHandler.getClients().containsValue( ctx.channel() ) ) {
            String name = "";
            for ( Map.Entry entry : NettyHandler.getClients().entrySet() ) {
                if ( entry.getValue() == ctx.channel() ) {
                    name = (String) entry.getKey();
                    break;
                }
            }
            if ( !name.equalsIgnoreCase( "" ) ) {
                NettyHandler.getClients().remove( name );
            }
        }
    }
}