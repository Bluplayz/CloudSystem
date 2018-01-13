package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.group.ServerGroup;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class SaveServerPacket extends Packet {

    @Getter
    private String targetServerName;

    @Getter
    private ServerGroup targetServerGroup;

    /**
     * Will be sent from CloudMaster to CloudWrapper
     * to save the Temp Server to the ServerGroup folder
     */
    public SaveServerPacket( String targetServerName, ServerGroup targetServerGroup ) {
        this.targetServerName = targetServerName;
        this.targetServerGroup = targetServerGroup;
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
        this.targetServerName = new String( bytes );

        // ServerGroup
        this.targetServerGroup = new ServerGroup();
        this.targetServerGroup.fromByteBuf( byteBuf );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        byte[] bytes;

        // Name
        bytes = this.getTargetServerName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // ServerGroup
        this.getTargetServerGroup().toByteBuf( byteBuf );
    }

    @Override
    public String toString() {
        return "SaveServerPacket{" +
                "targetServerName='" + targetServerName + '\'' +
                ", targetServerGroup=" + targetServerGroup +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
