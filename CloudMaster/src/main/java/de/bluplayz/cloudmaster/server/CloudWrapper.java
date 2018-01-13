package de.bluplayz.cloudmaster.server;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.netty.packet.Packet;
import de.bluplayz.cloudlib.server.group.ServerGroup;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CloudWrapper {

    public static List<Integer> IDS_IN_USE = new ArrayList<>();

    @Getter
    public Channel channel;

    @Getter
    public List<BungeeCordProxy> bungeeCordProxies = new LinkedList<>();

    @Getter
    public List<SpigotServer> spigotServers = new LinkedList<>();

    @Getter
    @Setter
    private int id = 0;

    @Getter
    @Setter
    private String name = "";

    public void onConnect( Channel channel ) {
        this.channel = channel;
        this.setId( this.getAvailableId() );
        this.setName( this.getClass().getSimpleName() + "-" + this.getId() );

        CloudWrapper.IDS_IN_USE.add( this.getId() );
    }

    public void onDisconnect() {
        for ( BungeeCordProxy bungeeCordProxy : this.getBungeeCordProxies() ) {
            bungeeCordProxy.shutdown();
        }
        for ( SpigotServer spigotServer : this.getSpigotServers() ) {
            spigotServer.shutdown();
        }

        if ( CloudWrapper.IDS_IN_USE.contains( this.getId() ) ) {
            CloudWrapper.IDS_IN_USE.remove( (Object) this.getId() );
        }

        this.channel = null;
    }

    public void startServers( ServerGroup... serverGroups ) {
        List<SpigotServer> spigotServers = new ArrayList<>();
        for ( ServerGroup serverGroup : serverGroups ) {
            spigotServers.add( new SpigotServer( this, serverGroup ) );
        }

        for ( SpigotServer spigotServer : spigotServers ) {
            spigotServer.startServer();
        }
    }

    public void startProxies( ServerGroup... serverGroups ) {
        List<BungeeCordProxy> bungeeCordProxies = new ArrayList<>();
        for ( ServerGroup serverGroup : serverGroups ) {
            bungeeCordProxies.add( new BungeeCordProxy( this, serverGroup ) );
        }

        for ( BungeeCordProxy bungeeCordProxy : bungeeCordProxies ) {
            bungeeCordProxy.startProxy();
        }
    }

    public void sendPacket( Packet packet ) {
        if ( packet == null ) {
            return;
        }

        CloudMaster.getInstance().getNetwork().getPacketHandler().sendPacket( packet, this.getChannel() );
    }

    private int getAvailableId() {
        for ( int id = 1; id < 40000; id++ ) {
            if ( CloudWrapper.IDS_IN_USE.contains( id ) ) {
                continue;
            }

            return id;
        }

        return 0;
    }
}
