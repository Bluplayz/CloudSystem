package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.Proxy;
import de.bluplayz.cloudlib.server.template.Template;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class StartProxyPacket extends Packet {

    @Getter
    private Proxy proxy;

    /**
     * Will be sent from CloudMaster to CloudWrapper to start
     * a Proxy on the CloudWrapper
     */
    public StartProxyPacket( Proxy proxy ) {
        this.proxy = proxy;
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
        this.proxy = new Proxy( template );

        // Port
        this.getProxy().setPort( byteBuf.readInt() );

        // ID
        this.getProxy().setId( byteBuf.readInt() );

        // UUID
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.getProxy().setUniqueId( UUID.fromString( new String( bytes ) ) );

        // Name
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.getProxy().setName( new String( bytes ) );

        // Slots
        this.getProxy().setSlots( byteBuf.readInt() );

        // OnlinePlayers
        this.getProxy().setOnlinePlayers( byteBuf.readInt() );

        // ActiveMode
        length = byteBuf.readInt();
        bytes = new byte[length];
        for ( int i = 0; i < length; i++ ) {
            bytes[i] = byteBuf.readByte();
        }
        this.getProxy().setActiveMode( ActiveMode.valueOf( new String( bytes ) ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        byte[] bytes;

        // Template
        // ServerType
        bytes = this.getProxy().getTemplate().getType().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Name
        bytes = this.getProxy().getTemplate().getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // MinOnlineServers
        byteBuf.writeInt( this.getProxy().getTemplate().getMinOnlineServers() );

        // MaxOnlineServers
        byteBuf.writeInt( this.getProxy().getTemplate().getMaxOnlineServers() );

        // MaxMemory
        byteBuf.writeInt( this.getProxy().getTemplate().getMaxMemory() );

        // TemplateFolder
        bytes = this.getProxy().getTemplate().getTemplateFolder().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // ProxyFallbackPriorities
        byteBuf.writeInt( this.getProxy().getTemplate().getProxyFallbackPriorities().size() );
        for ( String fallbackTemplate : this.getProxy().getTemplate().getProxyFallbackPriorities() ) {
            bytes = fallbackTemplate.getBytes( StandardCharsets.UTF_8 );
            byteBuf.writeInt( bytes.length );
            byteBuf.writeBytes( bytes );
        }

        // Port
        byteBuf.writeInt( this.getProxy().getPort() );

        // ID
        byteBuf.writeInt( this.getProxy().getId() );

        // UUID
        bytes = this.getProxy().getUniqueId().toString().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Name
        bytes = this.getProxy().getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Slots
        byteBuf.writeInt( this.getProxy().getSlots() );

        // OnlinePlayers
        byteBuf.writeInt( this.getProxy().getOnlinePlayers() );

        // ActiveMode
        bytes = this.getProxy().getActiveMode().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );
    }

    @Override
    public String toString() {
        return "StartProxyPacket{" +
                "proxy=" + proxy +
                ", uniqueId=" + uniqueId +
                '}';
    }
}
