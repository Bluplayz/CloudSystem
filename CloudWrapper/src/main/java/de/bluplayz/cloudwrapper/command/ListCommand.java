package de.bluplayz.cloudwrapper.command;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.command.Command;
import de.bluplayz.cloudwrapper.server.BungeeCordProxy;
import de.bluplayz.cloudwrapper.server.SpigotServer;

public class ListCommand extends Command {

    public ListCommand() {
        super( "List" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudWrapper.getInstance().getLogger().info( this.getTabString() + "BungeeCordProxies: " + CloudWrapper.getInstance().getBungeeCordProxies().size() );
        for ( BungeeCordProxy bungeeCordProxy : CloudWrapper.getInstance().getBungeeCordProxies() ) {
            CloudWrapper.getInstance().getLogger().info( this.getTabString() + bungeeCordProxy.getName() + " {" );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "ServerGroup: " + bungeeCordProxy.getServerGroup().getName() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "UniqueId: " + bungeeCordProxy.getUniqueId().toString() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "Port: " + bungeeCordProxy.getPort() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "Players: " + bungeeCordProxy.getOnlinePlayers() + " / " + bungeeCordProxy.getSlots() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "State: " + bungeeCordProxy.getActiveMode().name() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString() + "}" );
        }

        CloudWrapper.getInstance().getLogger().info( this.getTabString() + "SpigotServer: " + CloudWrapper.getInstance().getSpigotServers().size() );
        for ( SpigotServer spigotServer : CloudWrapper.getInstance().getSpigotServers() ) {
            CloudWrapper.getInstance().getLogger().info( this.getTabString() + spigotServer.getName() + " {" );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "ServerGroup: " + spigotServer.getServerGroup().getName() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "UniqueId: " + spigotServer.getUniqueId().toString() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "Port: " + spigotServer.getPort() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "Players: " + spigotServer.getOnlinePlayers() + " / " + spigotServer.getSlots() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString( 2 ) + "State: " + spigotServer.getActiveMode().name() );
            CloudWrapper.getInstance().getLogger().info( this.getTabString() + "}" );
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
