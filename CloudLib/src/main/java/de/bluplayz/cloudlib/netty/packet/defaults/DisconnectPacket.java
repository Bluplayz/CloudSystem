package de.bluplayz.cloudlib.netty.packet.defaults;

import de.bluplayz.cloudlib.netty.packet.Packet;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class DisconnectPacket extends Packet {

    @Override
    public void read( ByteBuf byteBuf ) throws IOException {
    }

    @Override
    public void write( ByteBuf byteBuf ) throws IOException {
    }
}
