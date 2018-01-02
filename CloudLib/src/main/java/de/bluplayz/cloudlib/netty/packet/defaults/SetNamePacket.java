package de.bluplayz.cloudlib.netty.packet.defaults;

import de.bluplayz.cloudlib.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@NoArgsConstructor
public class SetNamePacket extends Packet {

    @Getter
    @Setter
    private String name = "";

    @Override
    public void read( ByteBuf byteBuf ) throws IOException {
        int length = byteBuf.readInt();
        this.setName( (String) byteBuf.readCharSequence( length, StandardCharsets.UTF_8 ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws IOException {
        byteBuf.writeInt( this.getName().length() );
        byteBuf.writeCharSequence( this.getName(), StandardCharsets.UTF_8 );
    }
}
