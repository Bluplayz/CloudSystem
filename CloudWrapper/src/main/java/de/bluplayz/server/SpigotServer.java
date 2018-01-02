package de.bluplayz.server;

import de.bluplayz.CloudWrapper;
import de.bluplayz.locale.LocaleAPI;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SpigotServer extends Server {

    @Getter
    private CloudWrapper cloudWrapper = CloudWrapper.getInstance();

    @Getter
    private Process process;

    public SpigotServer( Server server ) {
        super( server.getTemplate() );

        this.setId( server.getId() );
        this.setName( server.getName() );
        this.setName( server.getName() );
        this.setActiveMode( server.getActiveMode() );
        this.setUniqueId( server.getUniqueId() );
        this.setPort( server.getPort() );
        this.setOnlinePlayers( server.getOnlinePlayers() );
    }

    public void startServer() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        this.initServerDirectory();

        this.getCloudWrapper().getSpigotServers().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        this.setActiveMode( ActiveMode.STOPPING );

        try {
            File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + this.getTemplate().getName() + "/" + this.getName() );
            FileUtils.deleteDirectory( serverDirectory );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        this.getCloudWrapper().getSpigotServers().remove( this );
    }

    private void initServerDirectory() {
        File templateFolder = new File( this.getTemplate().getTemplateFolder() );
        if ( !templateFolder.exists() ) {
            LocaleAPI.log( "network_server_starting_no_template_folder", this.getName(), this.getTemplate().getName(), this.getTemplate().getTemplateFolder() );
            return;
        }

        File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + this.getTemplate().getName() + "/" + this.getName() );
        if ( !serverDirectory.exists() ) {
            serverDirectory.mkdirs();
        }

        try {
            FileUtils.copyDirectory( templateFolder, serverDirectory );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
