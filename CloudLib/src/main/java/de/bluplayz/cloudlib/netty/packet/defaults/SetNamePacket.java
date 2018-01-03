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
        byte[] bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.setName( new String( bytes ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws IOException {
        byte[] bytes = this.getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );
    }

    @Override
    public String toString() {
        return "SetNamePacket{" +
                "name='" + name + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
