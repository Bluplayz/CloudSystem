package de.bluplayz.cloudapi.bukkit.listener;

import de.bluplayz.cloudapi.bukkit.BukkitCloudAPI;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    @Getter
    private BukkitCloudAPI bukkitCloudAPI;

    public PlayerLoginListener( BukkitCloudAPI bukkitCloudAPI ) {
        this.bukkitCloudAPI = bukkitCloudAPI;
    }

    @EventHandler
    public void onLogin( PlayerLoginEvent e ) {
        Player player = e.getPlayer();

        if ( this.getBukkitCloudAPI().getAllowedPlayers().contains( player.getUniqueId() ) ) {
            this.getBukkitCloudAPI().getAllowedPlayers().remove( player.getUniqueId() );
        } else {
            e.disallow( PlayerLoginEvent.Result.KICK_OTHER, "§cConnection failed." );
        }
    }
}
