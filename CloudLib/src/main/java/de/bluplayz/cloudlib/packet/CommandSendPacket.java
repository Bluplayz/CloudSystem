package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class CommandSendPacket extends Packet {

    @Getter
    private String commandline = "";

    @Getter
    private String servername = "";

    /**
     * Will be sent from CloudMaster to CloudWrapper
     * and the CloudWrapper execute them on the target Server or Proxy
     */
    public CommandSendPacket( String servername, String commandline ) {
        this.servername = servername;
        this.commandline = commandline;
    }

    @Override
    public void read( ByteBuf byteBuf ) throws IOException {
        int length;
        byte[] bytes;

        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.servername = new String( bytes );

        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.commandline = new String( bytes );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws IOException {
        byte[] bytes;

        bytes = this.getServername().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        bytes = this.getCommandline().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );
    }

    @Override
    public String toString() {
        return "CommandSendPacket{" +
                "commandline='" + commandline + '\'' +
                ", servername='" + servername + '\'' +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
