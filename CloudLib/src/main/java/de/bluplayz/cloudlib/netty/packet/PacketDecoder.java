package de.bluplayz.cloudlib.netty.packet;

import de.bluplayz.cloudlib.netty.PacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.UUID;

public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode( ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> output ) throws Exception {
        int id = byteBuf.readInt();
        Class<? extends Packet> packetClass = PacketHandler.PACKETS.get( id );

        if ( packetClass == null ) {
            //throw new NullPointerException( "Couldn't find packet by id " + id );
            System.out.println( "Couldn't find packet by id " + id );
        }

        Packet packet = packetClass.newInstance();
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        packet.uniqueId = UUID.fromString( new String( bytes ) );

        packet.read( byteBuf );
        output.add( packet );
    }
}
