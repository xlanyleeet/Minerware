package org.gr_code.minerware.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.listeners.bungee.PlayerJoin_Bungee;
import org.gr_code.minerware.listeners.bungee.PlayerLogin_Bungee;
import org.gr_code.minerware.listeners.bungee.PlayerQuit_Bungee;
import org.gr_code.minerware.listeners.bungee.ServerListPing_Bungee;
import org.gr_code.minerware.listeners.game.*;
import org.gr_code.minerware.listeners.setup.InventoryClick_Setup;
import org.gr_code.minerware.listeners.setup.InventoryClose_Setup;
import org.gr_code.minerware.listeners.setup.PlayerInteract_Setup;
import org.gr_code.minerware.listeners.sign.PlayerInteract_Sign;
import org.gr_code.minerware.listeners.sign.SignChange_Sign;
import org.gr_code.minerware.listeners.statistic.*;
import org.gr_code.minerware.listeners.waiting.*;
import org.gr_code.minerware.manager.ManageHandler;

public class ManagerListener {

    MinerPlugin minerPlugin = MinerPlugin.getInstance();
    PluginManager pluginManager = Bukkit.getPluginManager();

    public ManagerListener() {
        //[SIGN]
        pluginManager.registerEvents(new SignChange_Sign(), minerPlugin);
        pluginManager.registerEvents(new PlayerInteract_Sign(), minerPlugin);
        //[STATISTIC]
        pluginManager.registerEvents(new PlayerWorldChange_Statistic(), minerPlugin);
        pluginManager.registerEvents(new PlayerWin_Statistic(), minerPlugin);
        pluginManager.registerEvents(new PlayerPrepareLeaveArena_Statistic(), minerPlugin);
        pluginManager.registerEvents(new PlayerRespawn_Statistic(), minerPlugin);
        pluginManager.registerEvents(new PluginEnable_Statistic(), minerPlugin);
        //[SETUP]
        pluginManager.registerEvents(new InventoryClick_Setup(), minerPlugin);
        pluginManager.registerEvents(new InventoryClose_Setup(), minerPlugin);
        pluginManager.registerEvents(new PlayerInteract_Setup(), minerPlugin);
        //[BUNGEE]
        pluginManager.registerEvents(new PlayerQuit_Bungee(), minerPlugin);
        pluginManager.registerEvents(new PlayerLogin_Bungee(), minerPlugin);
        pluginManager.registerEvents(new PlayerJoin_Bungee(), minerPlugin);
        pluginManager.registerEvents(new ServerListPing_Bungee(), minerPlugin);
        //[WAITING]
        pluginManager.registerEvents(new PlayerChat_Waiting(), minerPlugin);
        pluginManager.registerEvents(new EntityDamage_Waiting(), minerPlugin);
        pluginManager.registerEvents(new PlayerInteract_Waiting(), minerPlugin);
        pluginManager.registerEvents(new PlayerInventoryClick_Waiting(), minerPlugin);
        pluginManager.registerEvents(new PlayerDropItem_Waiting(), minerPlugin);
        //[GAMES]
        pluginManager.registerEvents(new PlayerFish_Games(), minerPlugin);
        pluginManager.registerEvents(new InventoryClose_Game(), minerPlugin);
        pluginManager.registerEvents(new PlayerShearEntity_Games(), minerPlugin);
        pluginManager.registerEvents(new EntityDismount_Games(), minerPlugin);
        pluginManager.registerEvents(new AsyncPlayerChat_Games(), minerPlugin);
        pluginManager.registerEvents(new EntityChangeBlock_Games(), minerPlugin);
        pluginManager.registerEvents(new EntityShootBow_Games(), minerPlugin);
        pluginManager.registerEvents(new EntityExplode_Games(), minerPlugin);
        pluginManager.registerEvents(new BlockExplode_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerInteractEntity_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerInteractAtEntity_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerLeave_Games(), minerPlugin);
        pluginManager.registerEvents(new EntityDamage_Games(), minerPlugin);
        pluginManager.registerEvents(new FoodLevelChange_Games(), minerPlugin);
        pluginManager.registerEvents(new BlockFade_Games(), minerPlugin);
        pluginManager.registerEvents(new BlockFromTo_Games(), minerPlugin);
        pluginManager.registerEvents(new BlockBreak_Games(), minerPlugin);
        pluginManager.registerEvents(new BlockPlace_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerDropItem_Games(), minerPlugin);
        pluginManager.registerEvents(new InventoryClick_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerInteract_Games(), minerPlugin);
        pluginManager.registerEvents(new ProjectileHit_Games(), minerPlugin);
        pluginManager.registerEvents(new EntityCombust_Games(), minerPlugin);
        pluginManager.registerEvents(new CreatureSpawn_Games(), minerPlugin);
        pluginManager.registerEvents(new ItemSpawn_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerExpChange_Games(), minerPlugin);
        pluginManager.registerEvents(new PlayerCommandPreprocess_Games(), minerPlugin);
        if (ManageHandler.getModernAPI().oldVersion()) {
            pluginManager.registerEvents(new PlayerPickUpItem_Games(), minerPlugin);
            return;
        }
        pluginManager.registerEvents(new EntityPickUpItem_Games(), minerPlugin);
    }

    public static void register(){
        new ManagerListener();
    }
}



