package de.bluplayz.cloudmaster.command;

import de.bluplayz.CloudMaster;
import de.bluplayz.cloudlib.command.Command;
import de.bluplayz.cloudlib.packet.SaveServerPacket;
import de.bluplayz.cloudlib.server.group.ServerGroup;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudmaster.server.BungeeCordProxy;
import de.bluplayz.cloudmaster.server.CloudWrapper;
import de.bluplayz.cloudmaster.server.SpigotServer;

import java.util.UUID;

public class SaveServerCommand extends Command {

    public SaveServerCommand() {
        super( "SaveServer" );
    }

    @Override
    public void execute( String label, String[] args ) {
        // Usage: /saveserver <name | uuid> <servergroup>
        // SaveServer Command um den aktuellen Server mit allen Configs direkt zu kopieren (/saveserver Lobby-1 Lobby / /saveserver <name> <servergroup>)

        if ( args.length < 2 ) {
            LocaleAPI.log( "command_saveserver_usage" );
            return;
        }

        String server = args[0];
        String serverGroupName = args[1];

        SpigotServer spigotServer = this.getSpigotServer( server );
        BungeeCordProxy bungeeCordProxy = this.getBungeeCordProxy( server );
        ServerGroup serverGroup = ServerGroup.getServerGroupByName( serverGroupName );

        if ( spigotServer == null && bungeeCordProxy == null ) {
            LocaleAPI.log( "network_command_server_not_exist", server );
            return;
        }

        if ( serverGroup == null ) {
            LocaleAPI.log( "network_command_servergroup_not_exist", server );
            return;
        }

        CloudWrapper cloudWrapper;
        if ( spigotServer != null ) {
            cloudWrapper = CloudMaster.getInstance().getServerManager().getCloudWrapperByServer( spigotServer );
        } else {
            cloudWrapper = CloudMaster.getInstance().getServerManager().getCloudWrapperByProxy( bungeeCordProxy );
        }

        // Send Packet to Wrapper
        SaveServerPacket saveServerPacket = new SaveServerPacket( spigotServer != null ? spigotServer.getName() : bungeeCordProxy.getName(), serverGroup );
        CloudMaster.getInstance().getNetwork().getPacketHandler().sendPacket( saveServerPacket, cloudWrapper.getChannel() );

        // Success Message
        LocaleAPI.log( "command_saveserver_success", spigotServer != null ? spigotServer.getName() : bungeeCordProxy.getName(), serverGroup.getName() );
    }

    private SpigotServer getSpigotServer( String server ) {
        if ( server.split( "-" ).length == 2 ) {
            // Check for name
            SpigotServer spigotServer = CloudMaster.getInstance().getServerManager().getServerByName( server );
            if ( spigotServer != null ) {
                return spigotServer;
            }
        } else {
            // Check for UUID
            UUID uuid;
            try {
                uuid = UUID.fromString( server );

                SpigotServer spigotServer = CloudMaster.getInstance().getServerManager().getServerByUUID( uuid );
                if ( spigotServer != null ) {
                    return spigotServer;
                }
            } catch ( IllegalArgumentException ignored ) {
            }
        }

        return null;
    }

    private BungeeCordProxy getBungeeCordProxy( String server ) {
        if ( server.split( "-" ).length == 2 ) {
            // Check for name
            BungeeCordProxy bungeeCordProxy = CloudMaster.getInstance().getServerManager().getProxyByName( server );
            if ( bungeeCordProxy != null ) {
                return bungeeCordProxy;
            }
        } else {
            // Check for UUID
            UUID uuid;
            try {
                uuid = UUID.fromString( server );

                BungeeCordProxy bungeeCordProxy = CloudMaster.getInstance().getServerManager().getProxyByUUID( uuid );
                if ( bungeeCordProxy != null ) {
                    return bungeeCordProxy;
                }
            } catch ( IllegalArgumentException ignored ) {
            }
        }

        return null;
    }
}
