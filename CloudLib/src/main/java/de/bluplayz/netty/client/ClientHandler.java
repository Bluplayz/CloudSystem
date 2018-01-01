package de.bluplayz.netty.client;

import de.bluplayz.netty.ConnectionListener;
import de.bluplayz.netty.NettyHandler;
import de.bluplayz.netty.PacketHandler;
import de.bluplayz.netty.packet.Packet;
import de.bluplayz.netty.packet.defaultpackets.DisconnectPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;

public class ClientHandler extends SimpleChannelInboundHandler<Packet> {

    @Getter
    private Channel channel = null;

    @Getter
    private NettyClient nettyClient;

    public ClientHandler( NettyClient client ) {
        nettyClient = client;
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        //super.exceptionCaught( ctx, cause );
    }

    protected void channelRead0( ChannelHandlerContext ctx, Packet packet ) throws Exception {
        if ( packet instanceof DisconnectPacket ) {
            getChannel().close();
        }

        for ( PacketHandler handler : NettyHandler.getPacketHandlers() ) {
            handler.incomingPacket( packet, getChannel() );
        }
    }

    @Override
    public void channelActive( ChannelHandlerContext ctx ) throws Exception {
        this.channel = ctx.channel();

        for ( ConnectionListener handler : NettyHandler.getConnectionListeners() ) {
            handler.channelConnected( ctx );
        }

        this.getNettyClient().setChannel( ctx.channel() );

        if ( NettyHandler.getPacketHandlers().size() > 0 ) {
            if ( PacketHandler.packetsToSend.size() > 0 ) {
                for ( Packet packet : PacketHandler.packetsToSend ) {
                    NettyHandler.getPacketHandlers().get( 0 ).sendPacket( packet );
                }
                PacketHandler.packetsToSend.clear();
            }
        }
    }

    @Override
    public void channelInactive( ChannelHandlerContext ctx ) throws Exception {
        this.channel = null;

        for ( ConnectionListener handler : NettyHandler.getConnectionListeners() ) {
            handler.channelDisconnected( ctx );
        }

        this.getNettyClient().setChannel( null );
    }
}