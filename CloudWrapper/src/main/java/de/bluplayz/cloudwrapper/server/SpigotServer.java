package de.bluplayz.cloudwrapper.server;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.config.Config;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.Server;
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

        File serverDirectory = this.initServerDirectory();
        this.startProcess( serverDirectory );

        this.getCloudWrapper().getSpigotServers().add( this );
    }

    public void shutdown() {
        LocaleAPI.log( "network_server_stopping", this.getName() );
        this.setActiveMode( ActiveMode.STOPPING );

        if ( this.getProcess() != null ) {
            if ( this.getProcess().isAlive() ) {
                this.getProcess().destroy();
            }
        }

        try {
            File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + this.getTemplate().getName() + "/" + this.getName() );
            FileUtils.deleteDirectory( serverDirectory );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        this.getCloudWrapper().getSpigotServers().remove( this );
    }

    private File initServerDirectory() {
        File templateFolder = new File( this.getTemplate().getTemplateFolder() );
        if ( !templateFolder.exists() ) {
            LocaleAPI.log( "network_server_starting_no_template_folder", this.getName(), this.getTemplate().getName(), this.getTemplate().getTemplateFolder() );
            return null;
        }

        File serverDirectory = new File( CloudWrapper.getRootDirectory(), "temp/" + this.getTemplate().getName() + "/" + this.getName() );
        if ( !serverDirectory.exists() ) {
            serverDirectory.mkdirs();
        } else {
            // Clear Directory because old content
            for ( File file : serverDirectory.listFiles() ) {
                try {
                    FileUtils.forceDelete( file );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }

        // Copy Template Folder
        try {
            FileUtils.copyDirectory( templateFolder, serverDirectory );
        } catch ( IOException e ) {
            e.printStackTrace();
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
            Config dataConfig = new Config( new File( directory, "connection.yml" ), Config.YAML );
            dataConfig.set( "servername", this.getName() );
            dataConfig.set( "uuid", this.getUniqueId().toString() );
            dataConfig.set( "address", this.getCloudWrapper().getNetwork().getHost() );
            dataConfig.set( "port", this.getCloudWrapper().getNetwork().getPort() );
            dataConfig.save();
        } catch ( IOException | NullPointerException e ) {
            e.printStackTrace();
        }

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
            ex.printStackTrace();
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
        processBuilder.command( /*"screen", "-S", this.getName(),*/ "java", "-jar"/*, "Xmx" + this.getTemplate().getMaxMemory() + "M" */, spigotJarName );

        CloudWrapper.getInstance().getPool().execute( () -> {
            try {
                this.process = CloudWrapper.getInstance().startProcess( processBuilder );
                int exitCode = this.process.waitFor();
                this.shutdown();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
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
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( this.getProcess().getOutputStream() ) );
                bufferedWriter.write( commandline + "\n" );
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } );
    }
}
