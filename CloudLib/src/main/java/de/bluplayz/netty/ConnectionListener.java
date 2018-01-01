package de.bluplayz.netty;

import io.netty.channel.ChannelHandlerContext;

public abstract class ConnectionListener {

    public abstract void channelConnected( ChannelHandlerContext ctx );
    public abstract void channelDisconnected( ChannelHandlerContext ctx );
}
