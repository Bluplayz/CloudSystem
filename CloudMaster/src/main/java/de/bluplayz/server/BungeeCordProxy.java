package de.bluplayz.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.locale.LocaleAPI;
import de.bluplayz.packet.StartServerPacket;
import de.bluplayz.server.template.Template;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BungeeCordProxy extends Proxy {

    public static final AtomicInteger STATIC_ID = new AtomicInteger( 0 );
    public static List<Integer> PORTS_IN_USE = new ArrayList<>();

    @Getter
    private CloudWrapper cloudWrapper;

    public BungeeCordProxy( CloudWrapper cloudWrapper, Template template ) {
        super( template );
        this.id = BungeeCordProxy.STATIC_ID.incrementAndGet();
        this.name = this.getTemplate().getName() + "-" + this.id;
        this.cloudWrapper = cloudWrapper;
        this.port = this.getAvailablePort();
        BungeeCordProxy.PORTS_IN_USE.add( this.getPort() );
    }

    public void startProxy() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        /*
        StartServerPacket startServerPacket = new StartServerPacket( this );
        CloudMaster.getInstance().getNetwork().getNettyHandler().addPacketCallback( startServerPacket, packet -> {
            if ( startServerPacket.isSuccess() ) {
                this.setActiveMode( ActiveMode.STARTED );
                LocaleAPI.log( "network_server_started_successfully", this.getName(), this.getPort() );
            } else {
                this.setActiveMode( ActiveMode.OFFLINE );
            }
        } );
        CloudMaster.getInstance().getNetwork().getPacketHandler().sendPacket( startServerPacket, this.getCloudWrapper().getChannel() );
        */

        this.getCloudWrapper().getBungeeCordProxies().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        this.setActiveMode( ActiveMode.STOPPING );

        this.getCloudWrapper().getBungeeCordProxies().remove( this );
    }

    private int getAvailablePort() {
        for ( int port = BungeeCordProxy.PORT_START; port < BungeeCordProxy.PORT_END; port++ ) {
            if ( BungeeCordProxy.PORTS_IN_USE.contains( port ) ) {
                continue;
            }

            return port;
        }

        return 0;
    }
}
