package de.bluplayz.server;

import de.bluplayz.CloudWrapper;
import de.bluplayz.locale.LocaleAPI;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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

        this.initServerDirectory();

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
                        //lines = lines.replace( line, "server-ip=127.0.0.1" );
                        lines = lines.replace( line, "server-ip=" + InetAddress.getLocalHost().getHostAddress() );
                        break;
                }
                i++;
            }

            Files.write( properties.toPath(), lines.getBytes() );
        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

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
        try {
            this.process = processBuilder.start();
        } catch ( IOException ex ) {
            ex.printStackTrace();
            return;
        }
    }
}
