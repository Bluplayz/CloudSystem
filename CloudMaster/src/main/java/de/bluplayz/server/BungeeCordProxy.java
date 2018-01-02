package de.bluplayz.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.locale.LocaleAPI;
import de.bluplayz.server.template.Template;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BungeeCordProxy extends Proxy {

    public static List<Integer> PORTS_IN_USE = new ArrayList<>();
    public static List<Integer> IDS_IN_USE = new ArrayList<>();

    @Getter
    private CloudWrapper cloudWrapper;

    public BungeeCordProxy( CloudWrapper cloudWrapper, Template template ) {
        super( template );
        this.setId( this.getAvailableId() );
        this.setName( this.getTemplate().getName() + "-" + this.getId() );
        this.cloudWrapper = cloudWrapper;
        this.setPort( this.getAvailablePort() );

        BungeeCordProxy.PORTS_IN_USE.add( this.getPort() );
        BungeeCordProxy.IDS_IN_USE.add( this.getId() );
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

        CloudMaster.getInstance().getPool().execute( () -> {
            if ( BungeeCordProxy.PORTS_IN_USE.contains( this.getPort() ) ) {
                BungeeCordProxy.PORTS_IN_USE.remove( (Object) this.getPort() );
            }
            if ( BungeeCordProxy.IDS_IN_USE.contains( this.getId() ) ) {
                BungeeCordProxy.IDS_IN_USE.remove( (Object) this.getId() );
            }
        } );

        CloudMaster.getInstance().getPool().execute( () -> this.getCloudWrapper().getBungeeCordProxies().remove( this ) );
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

    private int getAvailableId() {
        for ( int id = 1; id < 40000; id++ ) {
            if ( SpigotServer.IDS_IN_USE.contains( id ) ) {
                continue;
            }

            return id;
        }

        return 0;
    }
}
