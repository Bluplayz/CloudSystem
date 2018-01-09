package de.bluplayz.cloudmaster.command;

import de.bluplayz.cloudlib.command.Command;

public class ScreenCommand extends Command {

    public ScreenCommand() {
        super( "Screen" );
    }

    @Override
    public void execute( String label, String[] args ) {
        /*
        // dispatch SERVERNAME COMMANDLINE
        if ( args.length < 2 ) {
            LocaleAPI.log( "command_dispatch_usage" );
            return;
        }

        String servername = args[0];
        String commandline = Arrays.toString( args ).substring( servername.length() + 3, Arrays.toString( args ).length() - 1 ).replaceAll( ", ", " " );

        if ( servername.split( "-" ).length >= 2 ) {
            SpigotServer spigotServer = CloudMaster.getInstance().getServerManager().getServerByName( servername );
            if ( spigotServer != null ) {
                DispatchCommandPacket commandSendPacket = new DispatchCommandPacket( servername, commandline );
                spigotServer.getCloudWrapper().sendPacket( commandSendPacket );

                LocaleAPI.log( "command_dispatch_success", servername, commandline );
                return;
            }

            BungeeCordProxy bungeeCordProxy = CloudMaster.getInstance().getServerManager().getProxyByName( servername );
            if ( bungeeCordProxy != null ) {
                DispatchCommandPacket commandSendPacket = new DispatchCommandPacket( servername, commandline );
                bungeeCordProxy.getCloudWrapper().sendPacket( commandSendPacket );

                LocaleAPI.log( "command_dispatch_success", servername, commandline );
                return;
            }
        } else {
            // Check for UUID?
        }

        LocaleAPI.log( "command_dispatch_server_exist", servername );
        */
    }
}
