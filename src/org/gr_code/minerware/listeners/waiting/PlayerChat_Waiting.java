package org.gr_code.minerware.listeners.waiting;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.Utils;

import java.util.Objects;
import java.util.UUID;

public class PlayerChat_Waiting implements Listener {

    private final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();

    @EventHandler
    public void onAsync(AsyncPlayerChatEvent asyncPlayerChatEvent){
        Player player = asyncPlayerChatEvent.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!Utils.isInGame(uuid)) {
            if(!fileConfiguration.getBoolean("chat-handler.global"))
                return;
            asyncPlayerChatEvent.setCancelled(true);
            String format = fileConfiguration.getString("chat-handler.global-format");
            assert format != null;
            format = format.replace("<name>", player.getName())
                    .replace("<message>", asyncPlayerChatEvent.getMessage());
            String result = Utils.request(format, player);
            Bukkit.getOnlinePlayers().stream().filter(p -> !Utils.isInGame(p.getUniqueId()))
                    .forEach(p -> p.sendMessage(Utils.translate(result)));
            return;
        }
        if(!fileConfiguration.getBoolean("chat-handler.game"))
            return;
        asyncPlayerChatEvent.setCancelled(true);
        String format = fileConfiguration.getString("chat-handler.game-format");
        assert format != null;
        format = format.replace("<name>", player.getName())
                .replace("<message>", asyncPlayerChatEvent.getMessage());
        String result = Utils.request(format, player);
        Objects.requireNonNull(ServerManager.getArena(uuid)).getPlayers()
                .forEach(p -> p.getPlayer().sendMessage(Utils.translate(result)));
    }
}


