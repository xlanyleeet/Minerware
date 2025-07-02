package org.gr_code.minerware.games.resources;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import static java.util.Objects.requireNonNull;

public class Platform {

    private final Location location;
    private final ItemStack materialBlock, materialFirst, materialSecond;
    private final int size;
    private Stage stage;

    public Platform(Location location, XMaterial materialBlock, XMaterial materialFirst, XMaterial materialSecond,
            int size) {
        this.location = location;
        this.materialBlock = materialBlock.parseItem();
        this.materialFirst = materialFirst.parseItem();
        this.materialSecond = materialSecond.parseItem();
        this.size = size;
    }

    public void generate(Arena arena) {
        for (int x = 0; x < size; x++)
            for (int z = 0; z < size; z++) {
                ManageHandler.getModernAPI().setBlock(materialBlock, location.clone().add(x, 0, z).getBlock());
                if (arena.getProperties().getCuboid().notInside(location.clone().add(x, 0, z)))
                    Bukkit.broadcastMessage(location.toString());
            }
        stage = Stage.FULL;
    }

    public void nextStage() {
        switch (getStage()) {
            case FULL:
                firstStage();
                break;
            case FIRST:
                secondStage();
                break;
            case SECOND:
                thirdStage();
                break;
        }
    }

    private void firstStage() {
        for (int x = 0; x < size; x++)
            for (int z = 0; z < size; z++)
                ManageHandler.getModernAPI().setBlock(materialFirst, location.clone().add(x, 0, z).getBlock());
        stage = Stage.FIRST;
    }

    private void secondStage() {
        for (int x = 0; x < size; x++)
            for (int z = 0; z < size; z++)
                ManageHandler.getModernAPI().setBlock(materialSecond, location.clone().add(x, 0, z).getBlock());
        stage = Stage.SECOND;
    }

    @SuppressWarnings("deprecation")
    private void thirdStage() {
        World world = requireNonNull(location.getWorld());
        FallingBlock fallingBlock = ManageHandler.getModernAPI().isLegacy()
                ? ManageHandler.getModernAPI().oldVersion() ? location1 -> {
                    location1.getBlock().setType(Material.AIR);
                    location1.add(0.5, 0, 0.5);
                    // Use Material and byte data for old versions
                    Material material = requireNonNull(materialSecond.getType());
                    world.spawnFallingBlock(location1, material, (byte) 0);
                } : location1 -> {
                    location1.getBlock().setType(Material.AIR);
                    location1.add(0.5, 0, 0.5);
                    // Use BlockData for modern versions
                    Material material = requireNonNull(materialSecond.getType());
                    world.spawnFallingBlock(location1, material.createBlockData());
                }
                : location1 -> {
                    BlockData data = location1.getBlock().getBlockData().clone();
                    location1.getBlock().setType(Material.AIR);
                    location1.add(0.5, 0, 0.5);
                    world.spawnFallingBlock(location1, data);
                };
        for (int x = 0; x < size; x++)
            for (int z = 0; z < size; z++)
                fallingBlock.spawn(location.clone().add(x, 0, z));
        stage = Stage.BROKEN;
    }

    public Stage getStage() {
        return stage;
    }

    public enum Stage {
        FULL, FIRST, SECOND, BROKEN
    }

    interface FallingBlock {
        void spawn(Location location);
    }

}
