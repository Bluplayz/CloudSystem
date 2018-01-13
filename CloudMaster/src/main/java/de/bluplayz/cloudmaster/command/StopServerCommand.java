package de.bluplayz.cloudmaster.command;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.command.Command;
import de.bluplayz.cloudlib.packet.StopServerPacket;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudmaster.server.SpigotServer;

public class StopServerCommand extends Command {

    public StopServerCommand() {
        super( "StopServer" );
    }

    @Override
    public void execute( String label, String[] args ) {
        // Stopserver <Servername>
        if ( args.length < 1 ) {
            LocaleAPI.log( "command_stopserver_usage" );
            return;
        }

        String servername = args[0];
        SpigotServer spigotServer = CloudMaster.getInstance().getServerManager().getServerByName( servername );
        if ( spigotServer == null ) {
            LocaleAPI.log( "network_command_server_not_exist", servername );
            return;
        }

        StopServerPacket stopServerPacket = new StopServerPacket( spigotServer );
        CloudMaster.getInstance().getServerManager().getCloudWrapperByServer( spigotServer ).sendPacket( stopServerPacket );
        LocaleAPI.log( "network_server_stopping", spigotServer.getName(), spigotServer.getUniqueId() );
    }
}
