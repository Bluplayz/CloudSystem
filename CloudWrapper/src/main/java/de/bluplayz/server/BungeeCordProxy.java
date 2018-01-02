package de.bluplayz.server;

import de.bluplayz.CloudWrapper;
import de.bluplayz.locale.LocaleAPI;
import lombok.Getter;

public class BungeeCordProxy extends Proxy {

    @Getter
    private CloudWrapper cloudWrapper = CloudWrapper.getInstance();

    @Getter
    private Process process;

    public BungeeCordProxy( Proxy proxy ) {
        super( proxy.getTemplate() );

        this.setId( proxy.getId() );
        this.setName( proxy.getName() );
        this.setName( proxy.getName() );
        this.setActiveMode( proxy.getActiveMode() );
        this.setUniqueId( proxy.getUniqueId() );
        this.setPort( proxy.getPort() );
        this.setOnlinePlayers( proxy.getOnlinePlayers() );
    }

    public void startProxy() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );

        this.getCloudWrapper().getBungeeCordProxies().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        this.setActiveMode( ActiveMode.STOPPING );

        /*
        if ( this.process != null ) {
            this.getProcess().destroy();
        }
        */

        this.getCloudWrapper().getBungeeCordProxies().remove( this );
    }
}
