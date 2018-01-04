package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class ProxyStartedPacket extends Packet {

    @Getter
    private String name;

    /**
     * Will be sent from CloudMaster to CloudWrapper
     * when the Proxy is online
     */
    public ProxyStartedPacket( String name ) {
        this.name = name;
    }

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;
        byte[] bytes;

        // Name
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.name = new String( bytes );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        byte[] bytes;

        // Name
        bytes = this.getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );
    }

    @Override
    public String toString() {
        return "ProxyStartedPacket{" +
                "name='" + name + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
