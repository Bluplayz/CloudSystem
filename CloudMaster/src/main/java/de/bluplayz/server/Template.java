package de.bluplayz.server;

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
    private String templateFolder = "/home/templates/" + this.name;

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

    public enum Type {
        PROXY,
        SPIGOT
    }
}
