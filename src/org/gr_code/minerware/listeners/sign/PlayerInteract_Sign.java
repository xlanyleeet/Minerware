package org.gr_code.minerware.listeners.sign;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.ServerManager;
import org.gr_code.minerware.manager.type.SetupManager;
import org.gr_code.minerware.manager.type.SignManager;
import org.gr_code.minerware.manager.type.Utils;

public class PlayerInteract_Sign implements Listener {

    private static final String string = MinerPlugin.getInstance().getOptions().getString("sign.random-join.line-1");

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if(event.getClickedBlock() == null)
            return;
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        Block block = event.getClickedBlock();
        if(!(block.getState() instanceof Sign))
            return;
        Sign sign = (Sign) block.getState();
        Player player = event.getPlayer();
        if(SignManager.getSignLocations().contains(SetupManager.toString(block.getLocation()))){
            String arena = ChatColor.stripColor(sign.getLine(1));
            if(ServerManager.getArena(arena) != null){
                player.chat("/minerware join "+arena);
            }
            SignManager.update();
            return;
        }
        if(!(ChatColor.stripColor(sign.getLine(0))
                .equalsIgnoreCase(ChatColor.stripColor(Utils.translate(SignManager.title)))
                && sign.getLine(1).equals(
                Utils.translate(string))))
            return;
       player.chat("/minerware randomJoin");
    }


}
