package de.bluplayz.command;

import de.bluplayz.CloudMaster;
import de.bluplayz.server.BungeeCordProxy;
import de.bluplayz.server.CloudServer;
import de.bluplayz.server.SpigotServer;

public class ListCommand extends Command {

    public ListCommand() {
        super( "List" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudMaster.getInstance().getLogger().info( "CloudServer online: " + CloudMaster.getInstance().getServerManager().getCloudServers().size() );
        for ( CloudServer cloudServer : CloudMaster.getInstance().getServerManager().getCloudServers() ) {
            CloudMaster.getInstance().getLogger().info( cloudServer.getName() + " {" );

            CloudMaster.getInstance().getLogger().info( this.getTabString() + "BungeeCordProxies: " + cloudServer.getBungeeCordProxies().size() );
            for ( BungeeCordProxy bungeeCordProxy : cloudServer.getBungeeCordProxies() ) {
                CloudMaster.getInstance().getLogger().info( this.getTabString() + bungeeCordProxy.getName() + " {" );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Template: " + bungeeCordProxy.getTemplate().getName() );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Port: " + bungeeCordProxy.getPort() );
                CloudMaster.getInstance().getLogger().info( this.getTabString() + "}" );
            }

            CloudMaster.getInstance().getLogger().info( this.getTabString() + "SpigotServer: " + cloudServer.getSpigotServers().size() );
            for ( SpigotServer spigotServer : cloudServer.getSpigotServers() ) {
                CloudMaster.getInstance().getLogger().info( this.getTabString() + spigotServer.getName() + " {" );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Template: " + spigotServer.getTemplate().getName() );
                CloudMaster.getInstance().getLogger().info( this.getTabString( 2 ) + "Port: " + spigotServer.getPort() );
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
