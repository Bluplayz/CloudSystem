package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.Server;
import de.bluplayz.cloudlib.server.template.Template;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class StartServerPacket extends Packet {

    @Getter
    private Server server;

    /**
     * Will be sent from CloudMaster to CloudWrapper to start
     * a SpigotServer on the CloudWrapper
     */
    public StartServerPacket( Server server ) {
        this.server = server;
    }

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
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

        Template template = new Template();
        template.setName( name );
        template.setType( Template.Type.valueOf( serverType ) );
        template.setMinOnlineServers( minOnlineServers );
        template.setMaxOnlineServers( maxOnlineServers );
        template.setMaxMemory( maxMemory );
        template.setTemplateFolder( templateFolder );
        template.setProxyFallbackPriorities( fallbackPriorities );
        this.server = new Server( template );

        // Port
        this.getServer().setPort( byteBuf.readInt() );

        // ID
        this.getServer().setId( byteBuf.readInt() );

        // UUID
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.getServer().setUniqueId( UUID.fromString( new String( bytes ) ) );

        // Name
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.getServer().setName( new String( bytes ) );

        // Slots
        this.getServer().setSlots( byteBuf.readInt() );

        // OnlinePlayers
        this.getServer().setOnlinePlayers( byteBuf.readInt() );

        // ActiveMode
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.getServer().setActiveMode( ActiveMode.valueOf( new String( bytes ) ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        byte[] bytes;

        // Template
        // ServerType
        bytes = this.getServer().getTemplate().getType().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Name
        bytes = this.getServer().getTemplate().getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // MinOnlineServers
        byteBuf.writeInt( this.getServer().getTemplate().getMinOnlineServers() );

        // MaxOnlineServers
        byteBuf.writeInt( this.getServer().getTemplate().getMaxOnlineServers() );

        // MaxMemory
        byteBuf.writeInt( this.getServer().getTemplate().getMaxMemory() );

        // TemplateFolder
        bytes = this.getServer().getTemplate().getTemplateFolder().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // ProxyFallbackPriorities
        byteBuf.writeInt( this.getServer().getTemplate().getProxyFallbackPriorities().size() );
        for ( String fallbackTemplate : this.getServer().getTemplate().getProxyFallbackPriorities() ) {
            bytes = fallbackTemplate.getBytes( StandardCharsets.UTF_8 );
            byteBuf.writeInt( bytes.length );
            byteBuf.writeBytes( bytes );
        }

        // Port
        byteBuf.writeInt( this.getServer().getPort() );

        // ID
        byteBuf.writeInt( this.getServer().getId() );

        // UUID
        bytes = this.getServer().getUniqueId().toString().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Name
        bytes = this.getServer().getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Slots
        byteBuf.writeInt( this.getServer().getSlots() );

        // OnlinePlayers
        byteBuf.writeInt( this.getServer().getOnlinePlayers() );

        // ActiveMode
        bytes = this.getServer().getActiveMode().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );
    }

    @Override
    public String toString() {
        return "StartServerPacket{" +
                "server=" + server +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
