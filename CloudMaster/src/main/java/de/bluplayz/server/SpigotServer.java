package de.bluplayz.server;

import de.bluplayz.locale.LocaleAPI;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SpigotServer {

    public static final AtomicInteger STATIC_ID = new AtomicInteger( 0 );

    public static final int PORT_START = 30000;
    public static final int PORT_END = 70000;

    public static List<Integer> PORTS_IN_USE = new ArrayList<>();

    @Getter
    private CloudWrapper cloudWrapper;

    @Getter
    private Template template;

    @Getter
    private int port = 0;

    @Getter
    private int id = 0;

    @Getter
    private UUID uuid = UUID.randomUUID();

    @Getter
    private String name = "";

    public SpigotServer( CloudWrapper cloudWrapper, Template template ) {
        this.cloudWrapper = cloudWrapper;
        this.template = template;
        this.id = STATIC_ID.incrementAndGet();
        this.name = this.getTemplate().getName() + "-" + this.id;
        this.port = this.getAvailablePort();
        PORTS_IN_USE.add( this.getPort() );
    }

    public void startServer() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );
        // TODO

        this.getCloudWrapper().getSpigotServers().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        // TODO

        this.getCloudWrapper().getSpigotServers().remove( this );
    }

    private int getAvailablePort() {
        for ( int port = PORT_START; port < PORT_END; port++ ) {
            if ( PORTS_IN_USE.contains( port ) ) {
                continue;
            }

            return port;
        }

        return 0;
    }
}
