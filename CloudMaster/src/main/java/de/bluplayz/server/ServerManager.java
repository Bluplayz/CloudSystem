package de.bluplayz.server;

import io.netty.channel.Channel;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class ServerManager {

    @Getter
    private List<CloudServer> cloudServers = new LinkedList<>();

    public CloudServer addCloudServer( Channel channel ) {
        // Check for existing CloudServer with this channel
        if ( this.getCloudServerByChannel( channel ) != null ) {
            return null;
        }

        CloudServer cloudServer = new CloudServer();
        cloudServer.onConnect( channel );
        this.getCloudServers().add( cloudServer );
        return cloudServer;
    }

    public CloudServer removeCloudServer( Channel channel ) {
        CloudServer cloudServer = this.getCloudServerByChannel( channel );
        if ( cloudServer == null ) {
            return null;
        }

        cloudServer.onDisconnect();
        this.getCloudServers().remove( cloudServer );
        return cloudServer;
    }

    public CloudServer getCloudServerByChannel( Channel channel ) {
        for ( CloudServer cloudServer : this.getCloudServers() ) {
            if ( cloudServer.getChannel() == channel ) {
                return cloudServer;
            }
        }

        return null;
    }
}
