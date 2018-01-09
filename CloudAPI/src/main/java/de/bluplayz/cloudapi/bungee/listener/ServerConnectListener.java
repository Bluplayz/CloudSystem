package de.bluplayz.cloudapi.bungee.listener;

import de.bluplayz.cloudapi.bungee.BungeeCloudAPI;
import de.bluplayz.cloudlib.packet.VerifyPlayerPacket;
import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerConnectListener implements Listener {

    @Getter
    private BungeeCloudAPI bungeeCloudAPI;

    public ServerConnectListener( BungeeCloudAPI bungeeCloudAPI ) {
        this.bungeeCloudAPI = bungeeCloudAPI;
    }

    @EventHandler
    public void onServerConnect( ServerConnectEvent e ) {
        ProxiedPlayer player = e.getPlayer();
        ServerInfo serverInfo = e.getTarget();

        VerifyPlayerPacket verifyPlayerPacket = new VerifyPlayerPacket( player.getUniqueId(), serverInfo.getName() );
        this.getBungeeCloudAPI().getNetwork().getPacketHandler().sendPacket( verifyPlayerPacket );
    }
}
