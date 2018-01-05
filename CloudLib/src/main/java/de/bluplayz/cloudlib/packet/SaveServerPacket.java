package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.template.Template;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class SaveServerPacket extends Packet {

    @Getter
    private String targetServerName;

    @Getter
    private Template targetTemplate;

    /**
     * Will be sent from CloudMaster to CloudWrapper
     * to save the Temp Server to the Template folder
     */
    public SaveServerPacket( String targetServerName, Template targetTemplate ) {
        this.targetServerName = targetServerName;
        this.targetTemplate = targetTemplate;
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

        // Template
        this.targetTemplate = new Template();
        this.targetTemplate.fromByteBuf( byteBuf );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        byte[] bytes;

        // Name
        bytes = this.getTargetServerName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Template
        this.getTargetTemplate().toByteBuf( byteBuf );
    }

    @Override
    public String toString() {
        return "SaveServerPacket{" +
                "targetServerName='" + targetServerName + '\'' +
                ", targetTemplate=" + targetTemplate +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
