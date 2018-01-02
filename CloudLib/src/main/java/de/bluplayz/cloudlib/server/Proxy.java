package de.bluplayz.cloudlib.server;

import de.bluplayz.cloudlib.server.template.Template;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Proxy {

    public static final int PORT_START = 25565;
    public static final int PORT_END = 30000;

    @Getter
    @Setter
    private int port = 0;

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
    private String name = "";

    @Getter
    @Setter
    private int slots = 0;

    @Getter
    @Setter
    private int onlinePlayers = 0;

    @Getter
    @Setter
    private ActiveMode activeMode = ActiveMode.OFFLINE;

    public Proxy( Template template ) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "port=" + port +
                ", template=" + template +
                ", id=" + id +
                ", uniqueId=" + uniqueId +
                ", name='" + name + '\'' +
                ", slots=" + slots +
                ", onlinePlayers=" + onlinePlayers +
                ", activeMode=" + activeMode +
                '}';
    }
}
