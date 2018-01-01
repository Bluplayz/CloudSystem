package de.bluplayz.server;

import io.netty.channel.Channel;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class ServerManager {

    @Getter
    private List<CloudWrapper> cloudWrappers = new LinkedList<>();

    public CloudWrapper addCloudWrapper( Channel channel ) {
        // Check for existing CloudWrapper with this channel
        if ( this.getCloudWrapperByChannel( channel ) != null ) {
            return null;
        }

        CloudWrapper cloudWrapper = new CloudWrapper();
        cloudWrapper.onConnect( channel );
        this.getCloudWrappers().add( cloudWrapper );
        return cloudWrapper;
    }

    public CloudWrapper removeCloudWrapper( Channel channel ) {
        CloudWrapper cloudWrapper = this.getCloudWrapperByChannel( channel );
        if ( cloudWrapper == null ) {
            return null;
        }

        cloudWrapper.onDisconnect();
        this.getCloudWrappers().remove( cloudWrapper );
        return cloudWrapper;
    }

    public CloudWrapper getCloudWrapperByChannel( Channel channel ) {
        for ( CloudWrapper cloudWrapper : this.getCloudWrappers() ) {
            if ( cloudWrapper.getChannel() == channel ) {
                return cloudWrapper;
            }
        }

        return null;
    }
}
