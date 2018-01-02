package de.bluplayz.cloudlib.server.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

        for ( Template template : Template.allTemplates ) {
            if ( template.getType() == Type.PROXY ) {
                templates.add( template );
            }
        }

        return templates;
    }

    public static List<Template> getSpigotTemplates() {
        List<Template> templates = new ArrayList<>();

        for ( Template template : Template.allTemplates ) {
            if ( template.getType() == Type.SPIGOT ) {
                templates.add( template );
            }
        }

        return templates;
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
