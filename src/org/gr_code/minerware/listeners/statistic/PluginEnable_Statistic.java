package org.gr_code.minerware.listeners.statistic;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.gr_code.minerware.manager.type.PlaceholderManager;

public class PluginEnable_Statistic implements Listener {

    public static boolean ENABLED = false;

    @EventHandler
    public void onEnable(PluginEnableEvent pluginEnableEvent){
        if(ENABLED)
            return;
        if(pluginEnableEvent.getPlugin().getName().equalsIgnoreCase("PlaceholderAPI")){
            new PlaceholderManager().register();
            ENABLED = true;
        }
    }


}


