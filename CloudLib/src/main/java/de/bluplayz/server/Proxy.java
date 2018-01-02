package de.bluplayz.server;

import de.bluplayz.server.template.Template;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Proxy {

    public static final int PORT_START = 25565;
    public static final int PORT_END = 30000;

    @Getter
    public int port = 0;

    @Getter
    private Template template;

    @Getter
    public int id = 0;

    @Getter
    private UUID uuid = UUID.randomUUID();

    @Getter
    public String name = "";

    @Getter
    private int slots = 0;

    @Getter
    private int onlinePlayers = 0;

    @Getter
    @Setter
    private ActiveMode activeMode = ActiveMode.OFFLINE;

    public Proxy( Template template ) {
        this.template = template;
    }
}
