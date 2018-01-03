package de.bluplayz.cloudlib.netty.packet;

import de.bluplayz.cloudlib.netty.PacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode( ChannelHandlerContext ctx, Packet packet, ByteBuf byteBuf ) throws Exception {
        int id = PacketHandler.PACKETS.indexOf( packet.getClass() );
        if ( id == -1 ) {
            System.out.println( "Couldn't find id of packet " + packet.getClass().getSimpleName() );
        }

        byteBuf.writeInt( id );

        UUID uuid = packet.getUniqueId();

        byte[] bytes = uuid.toString().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        packet.write( byteBuf );
    }
}
