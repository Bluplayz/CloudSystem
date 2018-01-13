package de.bluplayz.cloudwrapper.server;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.config.Config;
import de.bluplayz.cloudlib.logging.Logger;
import de.bluplayz.cloudlib.packet.ServerStoppedPacket;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.ServerData;
import de.bluplayz.cloudwrapper.locale.LocaleAPI;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class BungeeCordProxy extends ServerData {

    @Getter
    private CloudWrapper cloudWrapper = CloudWrapper.getInstance();

    @Getter
    private Process process;

    @Getter
    private BufferedWriter bufferedWriter;

    public BungeeCordProxy( ServerData serverData ) {
        super( serverData.getServerGroup() );

        this.setId( serverData.getId() );
        this.setName( serverData.getName() );
        this.setName( serverData.getName() );
        this.setActiveMode( serverData.getActiveMode() );
        this.setUniqueId( serverData.getUniqueId() );
        this.setPort( serverData.getPort() );
        this.setOnlinePlayers( serverData.getOnlinePlayers() );
    }

    public void startProxy() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getUniqueId().toString(), this.getPort() );

        File serverDirectory = this.initServerDirectory();
        this.startProcess( serverDirectory );

        this.getCloudWrapper().getBungeeCordProxies().add( this );
    }

    public void forceShutdown() {
        if ( this.getActiveMode() != ActiveMode.OFFLINE && this.getActiveMode() != ActiveMode.STOPPING ) {
            LocaleAPI.log( "network_server_stopping", this.getName(), this.getUniqueId().toString() );
            this.setActiveMode( ActiveMode.STOPPING );
        }

        if ( this.getProcess() != null ) {
            if ( this.getProcess().isAlive() ) {
                this.getProcess().destroy();
                try {
                    this.getBufferedWriter().close();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        try {
            File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + this.getServerGroup().getName() + "/" + this.getName() );
            FileUtils.deleteDirectory( serverDirectory );
        } catch ( IOException e ) {
            //Logger.getGlobal().error( e.getMessage(), e );
        }

        if ( this.getActiveMode() != ActiveMode.OFFLINE ) {
            this.setActiveMode( ActiveMode.OFFLINE );
            LocaleAPI.log( "network_server_stopped_successfully", this.getName(), this.getPort() );
        }

        ServerStoppedPacket serverStoppedPacket = new ServerStoppedPacket( this.getName() );
        this.getCloudWrapper().getNetwork().getPacketHandler().sendPacket( serverStoppedPacket );

        this.getCloudWrapper().getBungeeCordProxies().remove( this );
    }

    private File initServerDirectory() {
        File templateFolder = new File( this.getServerGroup().getTemplateFolder() );
        if ( !templateFolder.exists() ) {
            LocaleAPI.log( "network_server_starting_no_template_folder", this.getName(), this.getServerGroup().getName(), this.getServerGroup().getTemplateFolder() );
            return null;
        }

        File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + this.getServerGroup().getName() + "/" + this.getName() );
        if ( !serverDirectory.exists() ) {
            serverDirectory.mkdirs();
        } else {
            // Clear Directory because old content
            for ( File file : serverDirectory.listFiles() ) {
                try {
                    FileUtils.forceDelete( file );
                } catch ( IOException e ) {
                    Logger.getGlobal().error( e.getMessage(), e );
                }
            }
        }

        // Copy ServerGroup Folder
        try {
            FileUtils.copyDirectory( templateFolder, serverDirectory );
        } catch ( IOException e ) {
            Logger.getGlobal().error( e.getMessage(), e );
        }

        // Copy global bukkit plugins (and also CloudAPI)
        try {
            for ( File file : new File( CloudWrapper.getRootDirectory(), "local/plugins" ).listFiles() ) {
                FileUtils.copyFileToDirectory( file, new File( serverDirectory, "plugins" ) );
            }
            // Connection Data Cofig for CloudAPI
            File directory = new File( serverDirectory, "plugins/CloudAPI" );
            if ( !directory.exists() ) {
                directory.mkdirs();
            }
            Config dataConfig = new Config( new File( directory, "data.yml" ), Config.YAML );
            dataConfig.set( "servername", this.getName() );
            dataConfig.set( "uuid", this.getUniqueId().toString() );
            dataConfig.set( "address", this.getCloudWrapper().getNetwork().getHost() );
            dataConfig.set( "port", this.getCloudWrapper().getNetwork().getPort() );
            dataConfig.set( "fallbackPriorities", this.getServerGroup().getProxyFallbackPriorities() );
            dataConfig.save();
        } catch ( IOException | NullPointerException e ) {
            Logger.getGlobal().error( e.getMessage(), e );
        }

        return serverDirectory;
    }

    private void startProcess( File serverDirectory ) {
        String bungeeCordJar = "BungeeCord.jar";
        for ( File file : serverDirectory.listFiles() ) {
            if ( file.getName().toLowerCase().endsWith( ".jar" ) ) {
                if ( file.getName().toLowerCase().contains( "bungee" ) || file.getName().toLowerCase().contains( "proxy" ) ) {
                    bungeeCordJar = file.getName();
                    break;
                }
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory( serverDirectory );
        processBuilder.command( /*"screen", "-S", this.getName(),*/ "java", "-jar"/*, "Xmx" + this.getServerGroup().getMaxMemory() + "M" */, bungeeCordJar );

        try {
            this.process = processBuilder.start();
        } catch ( IOException e ) {
            Logger.getGlobal().error( e.getMessage(), e );
        }

        CloudWrapper.getInstance().getPool().execute( () -> {
            try {
                this.process = CloudWrapper.getInstance().startProcess( processBuilder );
                this.bufferedWriter = new BufferedWriter( new OutputStreamWriter( this.getProcess().getOutputStream() ) );

                int exitCode = this.process.waitFor();
                this.setActiveMode( ActiveMode.STOPPING );
                this.forceShutdown();
            } catch ( InterruptedException e ) {
                Logger.getGlobal().error( e.getMessage(), e );
            }
        } );
    }

    public void execute( String commandline ) {
        if ( this.getProcess() == null ) {
            return;
        }

        if ( !this.getProcess().isAlive() ) {
            return;
        }

        CloudWrapper.getInstance().getPool().execute( () -> {
            try {
                this.getBufferedWriter().write( commandline + "\n" );
                this.getBufferedWriter().flush();
            } catch ( IOException e ) {
                Logger.getGlobal().error( e.getMessage(), e );
            }
        } );
    }
}
