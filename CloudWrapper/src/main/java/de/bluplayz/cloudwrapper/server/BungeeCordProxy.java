package de.bluplayz.cloudwrapper.server;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.config.Config;
import de.bluplayz.cloudlib.logging.Logger;
import de.bluplayz.cloudlib.server.ActiveMode;
import de.bluplayz.cloudlib.server.Proxy;
import de.bluplayz.cloudwrapper.locale.LocaleAPI;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.*;

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

        File serverDirectory = this.initServerDirectory();
        this.startProcess( serverDirectory );

        this.getCloudWrapper().getBungeeCordProxies().add( this );
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
            Logger.getGlobal().error( e.getMessage(), e );
        }

        this.getCloudWrapper().getBungeeCordProxies().remove( this );
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
                    Logger.getGlobal().error( e.getMessage(), e );
                }
            }
        }

        // Copy Template Folder
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
            Config dataConfig = new Config( new File( directory, "connection.yml" ), Config.YAML );
            dataConfig.set( "servername", this.getName() );
            dataConfig.set( "uuid", this.getUniqueId().toString() );
            dataConfig.set( "address", this.getCloudWrapper().getNetwork().getHost() );
            dataConfig.set( "port", this.getCloudWrapper().getNetwork().getPort() );
            dataConfig.save();
        } catch ( IOException | NullPointerException e ) {
            Logger.getGlobal().error( e.getMessage(), e );
        }

        /*
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
        */

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
        processBuilder.command( /*"screen", "-S", this.getName(),*/ "java", "-jar"/*, "Xmx" + this.getTemplate().getMaxMemory() + "M" */, bungeeCordJar );

        try {
            this.process = processBuilder.start();
        } catch ( IOException e ) {
            Logger.getGlobal().error( e.getMessage(), e );
        }

        CloudWrapper.getInstance().getPool().execute( () -> {
            try {
                this.process = CloudWrapper.getInstance().startProcess( processBuilder );
                int exitCode = this.process.waitFor();
                this.shutdown();
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
                BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( this.getProcess().getOutputStream() ) );
                bufferedWriter.write( commandline + "\n" );
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch ( IOException e ) {
                Logger.getGlobal().error( e.getMessage(), e );
            }
        } );
    }
}
