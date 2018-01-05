package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.ServerData;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnregisterServerPacket extends Packet {

    @Getter
    private ServerData serverData;

    /**
     * Will be sent from CloudMaster to BungeeCord
     * to unregister a Server
     */
    public UnregisterServerPacket( ServerData serverData ) {
        this.serverData = serverData;
    }

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        this.serverData = new ServerData( null );
        this.getServerData().fromByteBuf( byteBuf );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        this.getServerData().toByteBuf( byteBuf );
    }

    @Override
    public String toString() {
        return "UnregisterServerPacket{" +
                "serverData=" + serverData +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
