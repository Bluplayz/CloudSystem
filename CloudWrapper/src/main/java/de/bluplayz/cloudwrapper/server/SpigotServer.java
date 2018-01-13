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
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SpigotServer extends ServerData {

    @Getter
    private CloudWrapper cloudWrapper = CloudWrapper.getInstance();

    @Getter
    private Process process;

    @Getter
    private BufferedWriter bufferedWriter;

    public SpigotServer( ServerData serverData ) {
        super( serverData.getServerGroup() );

        this.setId( serverData.getId() );
        this.setName( serverData.getName() );
        this.setName( serverData.getName() );
        this.setActiveMode( serverData.getActiveMode() );
        this.setUniqueId( serverData.getUniqueId() );
        this.setPort( serverData.getPort() );
        this.setOnlinePlayers( serverData.getOnlinePlayers() );
    }

    public void startServer() {
        LocaleAPI.log( "network_server_starting", this.getName(), this.getUniqueId().toString(), this.getPort() );
        this.setActiveMode( ActiveMode.STARTING );

        File serverDirectory = this.initServerDirectory();
        this.startProcess( serverDirectory );

        this.getCloudWrapper().getSpigotServers().add( this );
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

        this.getCloudWrapper().getSpigotServers().remove( this );
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
            dataConfig.save();
        } catch ( IOException | NullPointerException e ) {
            Logger.getGlobal().error( e.getMessage(), e );
        }

        // Init server.properties
        File properties = new File( serverDirectory, "server.properties" );
        try {
            String lines = new String( Files.readAllBytes( properties.toPath() ), StandardCharsets.UTF_8 );
            String[] splitLines = lines.split( "\n" );

            int i = 0;
            for ( String line : splitLines ) {
                switch ( line.split( "=" )[0] ) {
                    case "server-port":
                        lines = lines.replace( line, "server-port=" + this.getPort() );
                        break;
                    case "online-mode":
                        lines = lines.replace( line, "online-mode=false" );
                        break;
                    case "server-ip":
                        lines = lines.replace( line, "server-ip=" + InetAddress.getLocalHost().getHostAddress() );
                        break;
                }
                i++;
            }

            Files.write( properties.toPath(), lines.getBytes() );
        } catch ( IOException ex ) {
            Logger.getGlobal().error( ex.getMessage(), ex );
        }

        // Init spigot.yml
        properties = new File( serverDirectory, "spigot.yml" );
        try {
            String lines = new String( Files.readAllBytes( properties.toPath() ), StandardCharsets.UTF_8 );
            String[] splitLines = lines.split( "\n" );

            int i = 0;
            for ( String line : splitLines ) {
                switch ( line.split( ":" )[0] ) {
                    case "bungeecord":
                        lines = lines.replace( line, line.replace( "false", "true" ) );
                        break;
                    case "restart-on-crash":
                        lines = lines.replace( line, line.replace( "true", "false" ) );
                        break;
                }
                i++;
            }

            Files.write( properties.toPath(), lines.getBytes() );
        } catch ( IOException ex ) {
            Logger.getGlobal().error( ex.getMessage(), ex );
        }

        return serverDirectory;
    }

    private void startProcess( File serverDirectory ) {
        String spigotJarName = "spigot.jar";
        for ( File file : serverDirectory.listFiles() ) {
            if ( file.getName().toLowerCase().endsWith( ".jar" ) ) {
                if ( file.getName().toLowerCase().contains( "spigot" ) || file.getName().toLowerCase().contains( "server" ) ) {
                    spigotJarName = file.getName();
                    break;
                }
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory( serverDirectory );
        processBuilder.command( /*"screen", "-S", this.getName(),*/ "java", "-jar"/*, "Xmx" + this.getServerGroup().getMaxMemory() + "M" */, spigotJarName );

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
                if ( this.getBufferedWriter() == null ) {
                    return;
                }

                this.getBufferedWriter().write( commandline + "\n" );
                this.getBufferedWriter().flush();
            } catch ( IOException e ) {
                Logger.getGlobal().error( e.getMessage(), e );
            }
        } );
    }
}
