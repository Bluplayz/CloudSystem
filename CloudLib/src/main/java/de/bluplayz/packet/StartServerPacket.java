package de.bluplayz.packet;

import de.bluplayz.netty.packet.Packet;
import de.bluplayz.server.template.Template;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StartServerPacket extends Packet {

    @Getter
    private Template template;

    @Getter
    private boolean success = false;

    public StartServerPacket( Template template ) {
        this.template = template;
    }

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Success Callback
        this.success = byteBuf.readBoolean();

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

        this.template = new Template();
        this.template.setName( name );
        this.template.setType( Template.Type.valueOf( serverType ) );
        this.template.setMinOnlineServers( minOnlineServers );
        this.template.setMaxOnlineServers( maxOnlineServers );
        this.template.setMaxMemory( maxMemory );
        this.template.setTemplateFolder( templateFolder );
        this.template.setProxyFallbackPriorities( fallbackPriorities );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Success Callback
        byteBuf.writeBoolean( this.isSuccess() );

        // ServerType
        byteBuf.writeInt( this.getTemplate().getType().name().length() );
        byteBuf.writeCharSequence( this.getTemplate().getType().name(), Charset.forName( "UTF-8" ) );

        // Name
        byteBuf.writeInt( this.getTemplate().getName().length() );
        byteBuf.writeCharSequence( this.getTemplate().getName(), Charset.forName( "UTF-8" ) );

        // MinOnlineServers
        byteBuf.writeInt( this.getTemplate().getMinOnlineServers() );

        // MaxOnlineServers
        byteBuf.writeInt( this.getTemplate().getMaxOnlineServers() );

        // MaxMemory
        byteBuf.writeInt( this.getTemplate().getMaxMemory() );

        // TemplateFolder
        byteBuf.writeInt( this.getTemplate().getTemplateFolder().length() );
        byteBuf.writeCharSequence( this.getTemplate().getTemplateFolder(), Charset.forName( "UTF-8" ) );

        // ProxyFallbackPriorities
        byteBuf.writeInt( this.getTemplate().getProxyFallbackPriorities().size() );
        for ( String fallbackTemplate : this.getTemplate().getProxyFallbackPriorities() ) {
            byteBuf.writeInt( fallbackTemplate.length() );
            byteBuf.writeCharSequence( fallbackTemplate, Charset.forName( "UTF-8" ) );
        }
    }
}
