package de.bluplayz.cloudlib.packet;

import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.Server;
import de.bluplayz.cloudlib.server.template.Template;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class StartServerPacket extends Packet {

    @Getter
    private Server server;

    @Getter
    @Setter
    private boolean success = false;

    public StartServerPacket( Server server ) {
        this.server = server;
    }

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Success Callback
        this.success = byteBuf.readBoolean();

        // Template
        // ServerType
        length = byteBuf.readInt();
        String serverType = (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) );

        // Name
        length = byteBuf.readInt();
        String name = (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) );

        // MinOnlineServers
        int minOnlineServers = byteBuf.readInt();

        // MaxOnlineServers
        int maxOnlineServers = byteBuf.readInt();

        // MaxMemory
        int maxMemory = byteBuf.readInt();

        // TemplateFolder
        length = byteBuf.readInt();
        String templateFolder = (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) );

        // ProxyFallbackPriorities
        List<String> fallbackPriorities = new ArrayList<>();
        int arraySize = byteBuf.readInt();
        for ( int i = 0; i < arraySize; i++ ) {
            length = byteBuf.readInt();
            fallbackPriorities.add( (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) ) );
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
        this.getServer().setUniqueId( UUID.fromString( (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) ) ) );

        // Name
        length = byteBuf.readInt();
        this.getServer().setName( (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) ) );

        // Slots
        this.getServer().setSlots( byteBuf.readInt() );

        // OnlinePlayers
        this.getServer().setOnlinePlayers( byteBuf.readInt() );

        // ActiveMode
        length = byteBuf.readInt();
        this.getServer().setActiveMode( ActiveMode.valueOf( (String) byteBuf.readCharSequence( length, Charset.forName( "UTF-8" ) ) ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Success Callback
        byteBuf.writeBoolean( this.isSuccess() );

        // Template
        // ServerType
        byteBuf.writeInt( this.getServer().getTemplate().getType().name().length() );
        byteBuf.writeCharSequence( this.getServer().getTemplate().getType().name(), Charset.forName( "UTF-8" ) );

        // Name
        byteBuf.writeInt( this.getServer().getTemplate().getName().length() );
        byteBuf.writeCharSequence( this.getServer().getTemplate().getName(), Charset.forName( "UTF-8" ) );

        // MinOnlineServers
        byteBuf.writeInt( this.getServer().getTemplate().getMinOnlineServers() );

        // MaxOnlineServers
        byteBuf.writeInt( this.getServer().getTemplate().getMaxOnlineServers() );

        // MaxMemory
        byteBuf.writeInt( this.getServer().getTemplate().getMaxMemory() );

        // TemplateFolder
        byteBuf.writeInt( this.getServer().getTemplate().getTemplateFolder().length() );
        byteBuf.writeCharSequence( this.getServer().getTemplate().getTemplateFolder(), Charset.forName( "UTF-8" ) );

        // ProxyFallbackPriorities
        byteBuf.writeInt( this.getServer().getTemplate().getProxyFallbackPriorities().size() );
        for ( String fallbackTemplate : this.getServer().getTemplate().getProxyFallbackPriorities() ) {
            byteBuf.writeInt( fallbackTemplate.length() );
            byteBuf.writeCharSequence( fallbackTemplate, Charset.forName( "UTF-8" ) );
        }

        // Port
        byteBuf.writeInt( this.getServer().getPort() );

        // ID
        byteBuf.writeInt( this.getServer().getId() );

        // UUID
        byteBuf.writeInt( this.getServer().getUniqueId().toString().length() );
        byteBuf.writeCharSequence( this.getServer().getUniqueId().toString(), Charset.forName( "UTF-8" ) );

        // Name
        byteBuf.writeInt( this.getServer().getName().length() );
        byteBuf.writeCharSequence( this.getServer().getName(), Charset.forName( "UTF-8" ) );

        // Slots
        byteBuf.writeInt( this.getServer().getSlots() );

        // OnlinePlayers
        byteBuf.writeInt( this.getServer().getOnlinePlayers() );

        // ActiveMode
        byteBuf.writeInt( this.getServer().getActiveMode().name().length() );
        byteBuf.writeCharSequence( this.getServer().getActiveMode().name(), Charset.forName( "UTF-8" ) );
    }

    @Override
    public String toString() {
        return "StartServerPacket{" +
                "server=" + server +
                ", success=" + success +
                '}';
    }
}
