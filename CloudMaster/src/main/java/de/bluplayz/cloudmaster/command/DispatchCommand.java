package de.bluplayz.cloudmaster.command;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.command.Command;
import de.bluplayz.cloudlib.packet.CommandSendPacket;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudmaster.server.BungeeCordProxy;
import de.bluplayz.cloudmaster.server.SpigotServer;

import java.util.Arrays;

public class DispatchCommand extends Command {

    public DispatchCommand() {
        super( "Dispatch" );
    }

    @Override
    public void execute( String label, String[] args ) {
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
                CommandSendPacket commandSendPacket = new CommandSendPacket( servername, commandline );
                spigotServer.getCloudWrapper().sendPacket( commandSendPacket );

                LocaleAPI.log( "command_dispatch_success", servername, commandline );
                return;
            }

            BungeeCordProxy bungeeCordProxy = CloudMaster.getInstance().getServerManager().getProxyByName( servername );
            if ( bungeeCordProxy != null ) {
                CommandSendPacket commandSendPacket = new CommandSendPacket( servername, commandline );
                bungeeCordProxy.getCloudWrapper().sendPacket( commandSendPacket );

                LocaleAPI.log( "command_dispatch_success", servername, commandline );
                return;
            }
        } else {
            // Check for UUID?
        }

        LocaleAPI.log( "command_dispatch_server_exist", servername );
    }
}
