package de.bluplayz.cloudmaster.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.packet.StartServerPacket;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.ServerData;
import de.bluplayz.cloudlib.server.group.ServerGroup;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BungeeCordProxy extends ServerData {

    public static List<Integer> PORTS_IN_USE = new ArrayList<>();
    public static List<Integer> IDS_IN_USE = new ArrayList<>();

    @Getter
    private CloudWrapper cloudWrapper;

    public BungeeCordProxy( CloudWrapper cloudWrapper, ServerGroup serverGroup ) {
        super( serverGroup );
        this.setId( this.getAvailableId() );
        this.setName( this.getServerGroup().getName() + "-" + this.getId() );
        this.cloudWrapper = cloudWrapper;
        this.setPort( this.getAvailablePort() );

        BungeeCordProxy.PORTS_IN_USE.add( this.getPort() );
        BungeeCordProxy.IDS_IN_USE.add( this.getId() );
    }

    public void startProxy() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getUniqueId().toString(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        StartServerPacket startProxyPacket = new StartServerPacket( this );
        CloudMaster.getInstance().getNetwork().getPacketHandler().sendPacket( startProxyPacket, this.getCloudWrapper().getChannel() );

        this.getCloudWrapper().getBungeeCordProxies().add( this );
    }

    public void shutdown() {
        if ( this.getActiveMode() != ActiveMode.OFFLINE && this.getActiveMode() != ActiveMode.STOPPING ) {
            LocaleAPI.log( "network_server_stopping", this.getName(), this.getUniqueId().toString() );
            this.setActiveMode( ActiveMode.STOPPING );
        }

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
        for ( int port = BungeeCordProxy.PROXY_PORT_START; port < BungeeCordProxy.PROXY_PORT_END; port++ ) {
            if ( BungeeCordProxy.PORTS_IN_USE.contains( port ) ) {
                continue;
            }

            return port;
        }

        return 0;
    }

    private int getAvailableId() {
        for ( int id = 1; id < 40000; id++ ) {
            if ( BungeeCordProxy.IDS_IN_USE.contains( id ) ) {
                continue;
            }

            return id;
        }

        return 0;
    }
}
