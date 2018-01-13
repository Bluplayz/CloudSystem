package de.bluplayz.cloudmaster.command;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.command.Command;
import de.bluplayz.cloudlib.server.group.ServerGroup;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudmaster.server.CloudWrapper;

public class StartServerCommand extends Command {

    public StartServerCommand() {
        super( "StartServer" );
    }

    @Override
    public void execute( String label, String[] args ) {
        // Startserver <ServerGroup> <optional : amount>
        if ( args.length < 1 ) {
            LocaleAPI.log( "command_startserver_usage" );
            return;
        }

        int amount = 1;
        String serverGroupName = args[0];
        if ( args.length >= 2 ) {
            try {
                amount = Integer.parseInt( args[1] );
                if ( amount < 1 || amount > 50 ) {
                    amount = amount < 1 ? 1 : 50;
                }
            } catch ( NumberFormatException ignored ) {
            }
        }

        ServerGroup serverGroup = ServerGroup.getServerGroupByName( serverGroupName );
        if ( serverGroup == null ) {
            LocaleAPI.log( "network_command_servergroup_not_exist", serverGroupName );
            return;
        }

        // Success message
        if ( amount > 1 ) {
            LocaleAPI.log( "command_startserver_success_multi", amount, serverGroup.getName() );
        } else {
            LocaleAPI.log( "command_startserver_success_single", serverGroup.getName() );
        }

        for ( int i = 0; i < amount; i++ ) {
            CloudWrapper bestCloudWrapper = CloudMaster.getInstance().getServerManager().getCloudWrappers().get( 0 );
            for ( CloudWrapper cloudWrapper : CloudMaster.getInstance().getServerManager().getCloudWrappers() ) {
                if ( ( cloudWrapper.getBungeeCordProxies().size() + cloudWrapper.getSpigotServers().size() ) <= ( bestCloudWrapper.getBungeeCordProxies().size() + bestCloudWrapper.getSpigotServers().size() ) ) {
                    bestCloudWrapper = cloudWrapper;
                }
            }

            if ( serverGroup.getType() == ServerGroup.Type.PROXY ) {
                bestCloudWrapper.startProxies( serverGroup );
            } else {
                bestCloudWrapper.startServers( serverGroup );
            }
        }
    }
}
