package de.bluplayz.cloudlib.server;

import de.bluplayz.cloudlib.server.group.ServerGroup;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ServerData {

    // BungeeCord Port Range
    public static final int PROXY_PORT_START = 25565;
    public static final int PROXY_PORT_END = 30000;

    // Spigot Port Range
    public static final int SERVER_PORT_START = 30000;
    public static final int SERVER_PORT_END = 70000;

    @Getter
    @Setter
    private ServerGroup serverGroup;

    @Getter
    @Setter
    private int id = 0;

    @Getter
    @Setter
    private UUID uniqueId = UUID.randomUUID();

    @Getter
    @Setter
    private String host = "localhost";

    @Getter
    @Setter
    private int port = 0;

    @Getter
    @Setter
    private String name = "";

    @Getter
    @Setter
    private String motd = "";

    @Getter
    @Setter
    private int slots = 0;

    @Getter
    @Setter
    private int onlinePlayers = 0;

    @Getter
    @Setter
    private ActiveMode activeMode = ActiveMode.OFFLINE;

    public ServerData( ServerGroup serverGroup ) {
        this.serverGroup = serverGroup;

        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch ( UnknownHostException e ) {
            e.printStackTrace();
        }
    }

    public void toByteBuf( ByteBuf byteBuf ) {
        byte[] bytes;

        // ServerGroup
        this.getServerGroup().toByteBuf( byteBuf );

        // ID
        byteBuf.writeInt( this.getId() );

        // UUID
        bytes = this.getUniqueId().toString().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Host
        bytes = this.getHost().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Port
        byteBuf.writeInt( this.getPort() );

        // Name
        bytes = this.getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // MOTD
        bytes = this.getMotd().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Slots
        byteBuf.writeInt( this.getSlots() );

        // OnlinePlayers
        byteBuf.writeInt( this.getOnlinePlayers() );

        // ActiveMode
        bytes = this.getActiveMode().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );
    }

    public void fromByteBuf( ByteBuf byteBuf ) {
        int length;
        byte[] bytes;

        // ServerGroup
        this.serverGroup = new ServerGroup();
        this.serverGroup.fromByteBuf( byteBuf );

        // ID
        this.setId( byteBuf.readInt() );

        // UUID
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.setUniqueId( UUID.fromString( new String( bytes ) ) );

        // Host
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.setHost( new String( bytes ) );

        // Port
        this.setPort( byteBuf.readInt() );

        // Name
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.setName( new String( bytes ) );

        // MOTD
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.setMotd( new String( bytes ) );

        // Slots
        this.setSlots( byteBuf.readInt() );

        // OnlinePlayers
        this.setOnlinePlayers( byteBuf.readInt() );

        // ActiveMode
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.setActiveMode( ActiveMode.valueOf( new String( bytes ) ) );
    }

    @Override
    public String toString() {
        return "ServerData{" +
                "id=" + id +
                ", uniqueId=" + uniqueId +
                ", port=" + port +
                ", serverGroup=" + serverGroup +
                ", name='" + name + '\'' +
                ", slots=" + slots +
                ", onlinePlayers=" + onlinePlayers +
                ", activeMode=" + activeMode +
                '}';
    }
}
