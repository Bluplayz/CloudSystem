package de.bluplayz.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.locale.LocaleAPI;
import de.bluplayz.logging.Logger;
import de.bluplayz.packet.StartServerPacket;
import de.bluplayz.server.template.Template;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SpigotServer extends Server {

    public static final AtomicInteger STATIC_ID = new AtomicInteger( 0 );
    public static List<Integer> PORTS_IN_USE = new ArrayList<>();

    @Getter
    private CloudWrapper cloudWrapper;

    public SpigotServer( CloudWrapper cloudWrapper, Template template ) {
        super( template );
        this.cloudWrapper = cloudWrapper;
        this.id = SpigotServer.STATIC_ID.incrementAndGet();
        this.name = this.getTemplate().getName() + "-" + this.id;
        this.port = this.getAvailablePort();
        SpigotServer.PORTS_IN_USE.add( this.getPort() );
    }

    public void startServer() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        StartServerPacket startServerPacket = new StartServerPacket( this );
        CloudMaster.getInstance().getNetwork().getNettyHandler().addPacketCallback( startServerPacket, packet -> {
            if ( ( (StartServerPacket) packet ).isSuccess() ) {
                this.setActiveMode( ActiveMode.STARTED );
                LocaleAPI.log( "network_server_started_successfully", this.getName(), this.getPort() );
            } else {
                this.setActiveMode( ActiveMode.OFFLINE );
            }
        } );
        CloudMaster.getInstance().getNetwork().getPacketHandler().sendPacket( startServerPacket, this.getCloudWrapper().getChannel() );

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
}
