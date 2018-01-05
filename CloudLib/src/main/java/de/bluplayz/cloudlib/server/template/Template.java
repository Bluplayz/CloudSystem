package de.bluplayz.cloudlib.server.template;

import de.bluplayz.cloudlib.server.ActiveMode;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Template {

    @Getter
    private static List<Template> allTemplates = new ArrayList<>();

    @Getter
    @Setter
    private String name = "";

    @Getter
    @Setter
    private Type type = Type.SPIGOT;

    @Getter
    @Setter
    private int minOnlineServers = 0;

    @Getter
    @Setter
    private int maxOnlineServers = 0;

    @Getter
    @Setter
    private int maxMemory = 256;

    @Getter
    @Setter
    private String templateFolder = "/home/server/" + this.name;

    @Getter
    @Setter
    private List<String> proxyFallbackPriorities = new ArrayList<>();

    public static List<Template> getProxyTemplates() {
        List<Template> templates = new ArrayList<>();

        for ( Template template : Template.getAllTemplates() ) {
            if ( template.getType() == Type.PROXY ) {
                templates.add( template );
            }
        }

        return templates;
    }

    public static List<Template> getSpigotTemplates() {
        List<Template> templates = new ArrayList<>();

        for ( Template template : Template.getAllTemplates() ) {
            if ( template.getType() == Type.SPIGOT ) {
                templates.add( template );
            }
        }

        return templates;
    }

    public static Template getTemplateByName( String templatename ) {
        for ( Template template : Template.getAllTemplates() ) {
            if ( template.getName().equalsIgnoreCase( templatename ) ) {
                return template;
            }
        }

        return null;
    }

    public void toByteBuf( ByteBuf byteBuf ) {
        byte[] bytes;

        // ServerType
        bytes = this.getType().name().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // Name
        bytes = this.getName().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // MinOnlineServers
        byteBuf.writeInt( this.getMinOnlineServers() );

        // MaxOnlineServers
        byteBuf.writeInt( this.getMaxOnlineServers() );

        // MaxMemory
        byteBuf.writeInt( this.getMaxMemory() );

        // TemplateFolder
        bytes = this.getTemplateFolder().getBytes( StandardCharsets.UTF_8 );
        byteBuf.writeInt( bytes.length );
        byteBuf.writeBytes( bytes );

        // ProxyFallbackPriorities
        byteBuf.writeInt( this.getProxyFallbackPriorities().size() );
        for ( String fallbackTemplate : this.getProxyFallbackPriorities() ) {
            bytes = fallbackTemplate.getBytes( StandardCharsets.UTF_8 );
            byteBuf.writeInt( bytes.length );
            byteBuf.writeBytes( bytes );
        }
    }

    public void fromByteBuf( ByteBuf byteBuf ) {
        int length;
        byte[] bytes;

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

        this.setName( name );
        this.setType( Template.Type.valueOf( serverType ) );
        this.setMinOnlineServers( minOnlineServers );
        this.setMaxOnlineServers( maxOnlineServers );
        this.setMaxMemory( maxMemory );
        this.setTemplateFolder( templateFolder );
        this.setProxyFallbackPriorities( fallbackPriorities );
    }

    @Override
    public String toString() {
        return "Template{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", minOnlineServers=" + minOnlineServers +
                ", maxOnlineServers=" + maxOnlineServers +
                ", maxMemory=" + maxMemory +
                ", templateFolder='" + templateFolder + '\'' +
                ", proxyFallbackPriorities=" + proxyFallbackPriorities +
                '}';
    }

    public enum Type {
        PROXY,
        SPIGOT
    }
}
