package de.bluplayz.cloudmaster.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.Server;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudlib.packet.StartServerPacket;
import de.bluplayz.cloudlib.server.template.Template;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SpigotServer extends Server {

    public static List<Integer> PORTS_IN_USE = new ArrayList<>();
    public static List<Integer> IDS_IN_USE = new ArrayList<>();

    @Getter
    private CloudWrapper cloudWrapper;

    public SpigotServer( CloudWrapper cloudWrapper, Template template ) {
        super( template );
        this.cloudWrapper = cloudWrapper;
        this.setId( this.getAvailableId() );
        this.setName( this.getTemplate().getName() + "-" + this.getId() );
        this.setPort( this.getAvailablePort() );

        SpigotServer.PORTS_IN_USE.add( this.getPort() );
        SpigotServer.IDS_IN_USE.add( this.getId() );
    }

    public void startServer() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        StartServerPacket startServerPacket = new StartServerPacket( this );
        CloudMaster.getInstance().getNetwork().getPacketHandler().sendPacket( startServerPacket, this.getCloudWrapper().getChannel() );

        this.getCloudWrapper().getSpigotServers().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        this.setActiveMode( ActiveMode.STOPPING );

        CloudMaster.getInstance().getPool().execute( () -> {
            if ( SpigotServer.PORTS_IN_USE.contains( this.getPort() ) ) {
                SpigotServer.PORTS_IN_USE.remove( (Object) this.getPort() );
            }
            if ( SpigotServer.IDS_IN_USE.contains( this.getId() ) ) {
                SpigotServer.IDS_IN_USE.remove( (Object) this.getId() );
            }
        } );

        CloudMaster.getInstance().getPool().execute( () -> this.getCloudWrapper().getSpigotServers().remove( this ) );
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
