package org.gr_code.minerware.games.resources;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;

import java.io.InputStreamReader;
import java.io.Reader;

import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

public class Building {

    private final String[] blocks;
    private ItemStack itemSquare, itemFloor;
    private final Arena arena;
    private Location first;

    public Building(String nameFileBuilding, Arena arena) {
        Reader reader = new InputStreamReader(requireNonNull(MinerPlugin.getInstance().getResource(nameFileBuilding + ".yml")));
        FileConfiguration config = YamlConfiguration.loadConfiguration(reader);
        blocks = requireNonNull(config.getString(arena.getProperties().getType().toLowerCase())).replace(" ", "").split(",");
        this.arena = arena;
        chooseLocation();
    }

    private void chooseLocation() {
        Location first = arena.getProperties().getFirstLocation();
        Location second = arena.getProperties().getSecondLocation();
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        this.first = new Location(first.getWorld(), minX, first.getBlockY() + 1, minZ);
    }

    public ItemStack getItemFloor() {
        return itemFloor;
    }

    public ItemStack getItemSquare() {
        return itemSquare;
    }

    public void setItemSquare(ItemStack item) {
        itemSquare = item;
    }

    public void setItemFloor(ItemStack item) {
        itemFloor = item;
    }

    public void generateBuilding() {
        for (String strBlock : blocks) {
            String[] settings = strBlock.split(":");
            ItemStack item = settings[3].equalsIgnoreCase("itemFl") ? itemFloor : itemSquare;
            Location block = first.clone().add(parseInt(settings[0]), parseInt(settings[1]), parseInt(settings[2]));
            ManageHandler.getNMS().setBlock(item, block.getBlock());
        }
    }

}
