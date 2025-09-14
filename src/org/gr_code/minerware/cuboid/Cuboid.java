package org.gr_code.minerware.cuboid;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.*;
import java.util.stream.Collectors;

public class Cuboid {

    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    World world;
    private final List<Location> locations = new ArrayList<>();
    private final Location centerLocation;
    private final HashSet<String> floor;

    public Cuboid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, World world, boolean generateDefaults) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.floor = new HashSet<>();
        this.world = world;
        centerLocation = new Location(world, (float) (maxX + minX) / 2, minY + 2, (float) (maxZ + minZ) / 2);
        for (int mX = minX; mX <= maxX; mX++) {
            for (int mY = minY; mY <= maxY; mY++)
                for (int mZ = minZ; mZ <= maxZ; mZ++) {
                    if (mY == minY && generateDefaults && world.getBlockAt(mX, mY, mZ).getType() == Material.AIR)
                        ManageHandler.getModernAPI().setBlock(
                                Objects.requireNonNull(XMaterial.YELLOW_TERRACOTTA.parseItem()),
                                world.getBlockAt(mX, mY, mZ));
                    if ((mY == maxY || mY == minY) && (mX == maxX || mX == minX) && (mZ == minZ || mZ == maxZ)
                            && generateDefaults)
                        ManageHandler.getModernAPI().setBlock(
                                Objects.requireNonNull(XMaterial.GRAY_STAINED_GLASS.parseItem()),
                                world.getBlockAt(mX, mY, mZ));
                    locations.add(world.getBlockAt(mX, mY, mZ).getLocation());
                }
        }
    }

    public static Location getRandomLocation(Arena arena) {
        List<Location> squares = new ArrayList<>();
        for (Properties.Square sq : arena.getProperties().getSquares())
            squares.addAll(sq.getLocations());
        Location first = arena.getProperties().getFirstLocation();
        List<Location> list = arena.getProperties().getCuboid().getLocations().stream()
                .filter(location -> location.getBlockY() == first.getBlockY() + 1)
                .filter(location -> arena.getProperties().getCuboid().getCenter()
                        .distance(location) <= ((float) getSize(arena) / 2) - 2)
                .filter(location -> !squares.contains(location.getBlock().getLocation()))
                .collect(Collectors.toList());
        int size = list.size();
        int randomA = new Random().nextInt(size);
        Location random = list.get(randomA);
        random.setYaw(new Random().nextInt(180));
        random.setPitch(0);
        return random.clone();
    }

    public static int getSize(Arena arena) {
        Properties properties = arena.getProperties();
        Location a = properties.getFirstLocation();
        Location b = properties.getSecondLocation();
        return Math.abs(a.getBlockX() - b.getBlockX());
    }

    public boolean notInside(Location location) {
        return (location.getBlockY() > maxY || location.getBlockY() < minY)
                || (location.getBlockZ() > maxZ || location.getBlockZ() < minZ)
                || (location.getBlockX() > maxX || location.getBlockX() < minX);
    }

    public List<Location> getLocations() {
        return new ArrayList<>(locations);
    }

    public Location getCenter() {
        Location location = centerLocation.clone();
        location.setYaw(new Random().nextInt(180));
        return location;
    }

    public void saveFloor() {
        floor.clear();
        locations.stream().filter(location -> location.getBlockY() == centerLocation.getBlockY() - 2)
                .forEach(location -> {
                    int mX = location.getBlockX();
                    int mY = location.getBlockY();
                    int mZ = location.getBlockZ();
                    assert world != null;
                    Block block = world.getBlockAt(mX, mY, mZ);
                    ItemStack itemStack;
                    if (ManageHandler.getModernAPI().isLegacy()) {
                        // For legacy versions, try to get ItemStack but avoid expensive getData() calls
                        try {
                            itemStack = block.getState().getData().toItemStack();
                        } catch (Exception e) {
                            // Fallback to simple ItemStack if getData() fails
                            itemStack = new ItemStack(block.getType());
                        }
                    } else {
                        itemStack = new ItemStack(block.getType());
                    }
                    String string = XMaterial.toString(itemStack);
                    floor.add(mX + ":" + mY + ":" + mZ + ":" + string);
                });
    }

    public void restore() {
        floor.forEach(s -> {
            String[] strings = s.split(":");
            int x = Integer.parseInt(strings[0]);
            int y = Integer.parseInt(strings[1]);
            int z = Integer.parseInt(strings[2]);
            ItemStack itemStack = XMaterial.valueOf(strings[3]).parseItem();
            if (!Utils.getItem(getCenter().getBlock().getWorld().getBlockAt(x, y, z)).isSimilar(itemStack))
                ManageHandler.getModernAPI().setBlock(Objects.requireNonNull(itemStack),
                        getCenter().getBlock().getWorld().getBlockAt(x, y, z));
        });
    }

}
