package de.bluplayz.server;

import de.bluplayz.server.template.Template;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Server {

    public static final int PORT_START = 30000;
    public static final int PORT_END = 70000;

    @Getter
    @Setter
    private Template template;

    @Getter
    @Setter
    private int port = 0;

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

    public Server( Template template ) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "Server{" +
                "template=" + template +
                ", port=" + port +
                ", id=" + id +
                ", uuid=" + uniqueId +
                ", name='" + name + '\'' +
                ", slots=" + slots +
                ", onlinePlayers=" + onlinePlayers +
                ", activeMode=" + activeMode +
                '}';
    }
}
