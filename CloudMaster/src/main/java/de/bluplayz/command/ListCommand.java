package de.bluplayz.command;

import de.bluplayz.CloudMaster;
import de.bluplayz.server.BungeeCordProxy;
import de.bluplayz.server.CloudWrapper;
import de.bluplayz.server.SpigotServer;

public class ListCommand extends Command {

    public ListCommand() {
        super( "List" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudMaster.getInstance().getLogger().info( "CloudWrapper online: " + CloudMaster.getInstance().getServerManager().getCloudWrappers().size() );
        for ( CloudWrapper cloudWrapper : CloudMaster.getInstance().getServerManager().getCloudWrappers() ) {
            CloudMaster.getInstance().getLogger().info( cloudWrapper.getName() + " {" );

            CloudMaster.getInstance().getLogger().info( this.getTabString() + "BungeeCordProxies: " + cloudWrapper.getBungeeCordProxies().size() );
            for ( BungeeCordProxy bungeeCordProxy : cloudWrapper.getBungeeCordProxies() ) {
                CloudMaster.getInstance().getLogger().info( this.getTabString() + bungeeCordProxy.getName() + " {" );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Template: " + bungeeCordProxy.getTemplate().getName() );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Port: " + bungeeCordProxy.getPort() );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "State: " + bungeeCordProxy.getActiveMode().name() );
                CloudMaster.getInstance().getLogger().info( this.getTabString() + "}" );
            }

            CloudMaster.getInstance().getLogger().info( this.getTabString() + "SpigotServer: " + cloudWrapper.getSpigotServers().size() );
            for ( SpigotServer spigotServer : cloudWrapper.getSpigotServers() ) {
                CloudMaster.getInstance().getLogger().info( this.getTabString() + spigotServer.getName() + " {" );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Template: " + spigotServer.getTemplate().getName() );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Port: " + spigotServer.getPort() );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "State: " + spigotServer.getActiveMode().name() );
                CloudMaster.getInstance().getLogger().info( this.getTabString() + "}" );
            }
            CloudMaster.getInstance().getLogger().info( "}" );
        }
    }

    private String getTabString() {
        return "    ";
    }

    private String getTabString( int tabCounts ) {
        StringBuilder stringBuilder = new StringBuilder();

        for ( int i = 0; i < tabCounts; i++ ) {
            stringBuilder.append( "    " );
        }

        return stringBuilder.toString();
    }
}
