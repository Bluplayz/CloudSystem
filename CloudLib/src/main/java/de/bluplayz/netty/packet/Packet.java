package de.bluplayz.netty.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public abstract class Packet {

    @Getter
    @Setter
    public UUID uniqueId = UUID.randomUUID();


    public abstract void read( ByteBuf byteBuf ) throws Exception;

    public abstract void write( ByteBuf byteBuf ) throws Exception;
}