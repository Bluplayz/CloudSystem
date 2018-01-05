package de.bluplayz.cloudlib.server;

import de.bluplayz.cloudlib.server.template.Template;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
    private Template template;

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

    public ServerData( Template template ) {
        this.template = template;

        try {
            this.host = InetAddress.getLocalHost().getHostAddress();
        } catch ( UnknownHostException e ) {
            e.printStackTrace();
        }
    }

    public void toByteBuf( ByteBuf byteBuf ) {
        byte[] bytes;

        // Template
        // ServerType
        bytes = this.getTemplate().getType().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Name
        bytes = this.getTemplate().getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // MinOnlineServers
        byteBuf.writeInt( this.getTemplate().getMinOnlineServers() );

        // MaxOnlineServers
        byteBuf.writeInt( this.getTemplate().getMaxOnlineServers() );

        // MaxMemory
        byteBuf.writeInt( this.getTemplate().getMaxMemory() );

        // TemplateFolder
        bytes = this.getTemplate().getTemplateFolder().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // ProxyFallbackPriorities
        byteBuf.writeInt( this.getTemplate().getProxyFallbackPriorities().size() );
        for ( String fallbackTemplate : this.getTemplate().getProxyFallbackPriorities() ) {
            bytes = fallbackTemplate.getBytes( StandardCharsets.UTF_8 );
            byteBuf.writeInt( bytes.length );
            byteBuf.writeBytes( bytes );
        }

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

        // Template
        // ServerType
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        String serverType = new String( bytes );

        // Name
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        String name = new String( bytes );

        // MinOnlineServers
        int minOnlineServers = byteBuf.readInt();

        // MaxOnlineServers
        int maxOnlineServers = byteBuf.readInt();

        // MaxMemory
        int maxMemory = byteBuf.readInt();

        // TemplateFolder
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        String templateFolder = new String( bytes );

        // ProxyFallbackPriorities
        List<String> fallbackPriorities = new ArrayList<>();
        int arraySize = byteBuf.readInt();
        for ( int i = 0; i < arraySize; i++ ) {
            length = byteBuf.readInt();
            bytes = new byte[length];
            for ( int i2 = 0; i2 < length; i2++ ) {
                bytes[i2] = byteBuf.readByte();
            }
            fallbackPriorities.add( new String( bytes ) );
        }

        this.template = new Template();
        this.template.setName( name );
        this.template.setType( Template.Type.valueOf( serverType ) );
        this.template.setMinOnlineServers( minOnlineServers );
        this.template.setMaxOnlineServers( maxOnlineServers );
        this.template.setMaxMemory( maxMemory );
        this.template.setTemplateFolder( templateFolder );
        this.template.setProxyFallbackPriorities( fallbackPriorities );

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
                ", template=" + template +
                ", name='" + name + '\'' +
                ", slots=" + slots +
                ", onlinePlayers=" + onlinePlayers +
                ", activeMode=" + activeMode +
                '}';
    }
}
