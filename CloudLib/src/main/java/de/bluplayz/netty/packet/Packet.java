package de.bluplayz.netty.packet;

import io.netty.buffer.ByteBuf;

public abstract class Packet {
    public abstract void read( ByteBuf byteBuf ) throws Exception;
    public abstract void write( ByteBuf byteBuf ) throws Exception;
}