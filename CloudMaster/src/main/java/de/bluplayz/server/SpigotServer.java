package de.bluplayz.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.locale.LocaleAPI;
import de.bluplayz.server.template.Template;
import lombok.Getter;
import lombok.Setter;

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

    @Getter
    private int slots = 0;

    @Getter
    private int onlinePlayers = 0;

    @Getter
    @Setter
    private ActiveMode activeMode = ActiveMode.OFFLINE;

    public SpigotServer( CloudWrapper cloudWrapper, Template template ) {
        this.cloudWrapper = cloudWrapper;
        this.template = template;
        this.id = SpigotServer.STATIC_ID.incrementAndGet();
        this.name = this.getTemplate().getName() + "-" + this.id;
        this.port = this.getAvailablePort();
        SpigotServer.PORTS_IN_USE.add( this.getPort() );
    }

    public void startServer() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        //CloudMaster.getInstance().getNetwork().getNettyHandler().add

        this.getCloudWrapper().getSpigotServers().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        this.setActiveMode( ActiveMode.STOPPING );

        this.getCloudWrapper().getSpigotServers().remove( this );
    }

    private int getAvailablePort() {
        for ( int port = SpigotServer.PORT_START; port < SpigotServer.PORT_END; port++ ) {
            if ( SpigotServer.PORTS_IN_USE.contains( port ) ) {
                continue;
            }

            return port;
        }

        return 0;
    }

    public enum ActiveMode {
        STARTING( 0 ),
        STARTED( 1 ),
        ONLINE( 1 ),
        STOPPING( 2 ),
        OFFLINE( 3 );

        @Getter
        private int id;

        ActiveMode( int id ) {
            this.id = id;
        }
    }
}
