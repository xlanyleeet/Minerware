package org.gr_code.minerware.arena;

import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.builders.EnumBuilder;
import org.gr_code.minerware.builders.InventoryBuilder;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.SetupManager;
import org.gr_code.minerware.manager.type.Utils;
import org.gr_code.minerware.manager.type.resources.XMaterial;

import java.util.*;

public class Properties {

    public static final Inventory REMOVE = InventoryBuilder.generateRemoveInventory();

    public static Inventory TYPE = InventoryBuilder.InventoryCreate("", 45).generateTypeInventory().getInventory();

    public Properties(Arena.Type type, float yaw, Location firstLocation, Location lobbyLocationLoser,
            Location lobbyLocationWinner, int minPlayers, List<String> strings, String name) {
        this(type, yaw, firstLocation, lobbyLocationLoser, lobbyLocationWinner, minPlayers, strings, name, null);
    }

    public Properties(Arena.Type type, float yaw, Location firstLocation, Location lobbyLocationLoser,
            Location lobbyLocationWinner, int minPlayers, List<String> strings, String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
        setupInventory();
        this.baseYaw = yaw;
        setType(type.name());
        squares = new Square[type.getCuboids() * type.getCuboids()];
        setFirstLocation(firstLocation);
        setLobbyLocationLoser(lobbyLocationLoser, false);
        setLobbyLocationWinner(lobbyLocationWinner, false);
        maxPlayers = type.getPlayers();
        setMinPlayers(minPlayers);
        setup(false);
        saveCuboid();
        saveSquares();
        strings.forEach(this::addDisabledGame);
    }

    public Properties(String name) {
        this.name = name;
        setupInventory();
    }

    private Location firstLocation, secondLocation, lobbyLocationLoser, lobbyLocationWinner;

    private int maxPlayers, minPlayers;

    private Square[] squares;

    private Arena.Type type;

    protected Inventory inventory, locations, settings;

    private List<Inventory> paginatedGames;

    private Cuboid cuboid;

    private final String name;

    private String displayName; // Custom arena display name

    private String task;

    private float baseYaw;

    protected Set<String> disabledGames = new HashSet<>();

    /* |Void| */

    private void generateCube(boolean generateDefaults) {
        int minX, minY, minZ, maxX, maxY, maxZ;
        int[] paramX = getSortedInts(firstLocation.getBlockX(), secondLocation.getBlockX());
        minX = paramX[0];
        maxX = paramX[1];
        int[] paramY = getSortedInts(firstLocation.getBlockY(), secondLocation.getBlockY());
        minY = paramY[0];
        maxY = paramY[1];
        int[] paramZ = getSortedInts(firstLocation.getBlockZ(), secondLocation.getBlockZ());
        minZ = paramZ[0];
        maxZ = paramZ[1];
        cuboid = new Cuboid(minX, minY, minZ, maxX, maxY, maxZ, firstLocation.getWorld(), generateDefaults);
    }

    public void saveCuboid() {
        cuboid.saveFloor();
    }

    public void saveSquares() {
        for (Square square : squares) {
            square.save();
        }
    }

    public void addDisabledGame(String s) {
        if (s == null)
            return;
        Game game = Game.c(s);
        if (game == null)
            return;
        ItemStack itemStack = ItemBuilder.start(game.createGame(null).getGameItemStack()).setGlowing(false)
                .setLore(Arrays.asList("", Utils.translate("&7Enabled:&c&l false"))).build();
        int i = getSlotFromGame(game.createGame(null).getGameItemStack());
        getInventoryFromGame(itemStack).setItem(i, itemStack);
        disabledGames.add(s);
    }

    public void removeDisabledGame(String s) {
        Game game = Game.c(s);
        if (game == null)
            return;
        ItemStack itemStack = ItemBuilder.start(game.createGame(null).getGameItemStack()).setGlowing(true)
                .setLore(Arrays.asList("", Utils.translate("&7Enabled:&a&l true"))).build();
        int i = getSlotFromGame(itemStack);
        getInventoryFromGame(itemStack).setItem(i, itemStack);
        disabledGames.remove(s);
    }

    public void restoreCuboid() {
        cuboid.restore();
    }

    public void restoreSquares() {
        for (Square square : squares) {
            square.restore();
        }
    }

    public void doRestore() {
        restoreCuboid();
        restoreSquares();
    }

    private void setBlock(Location location, boolean block, Location original, XMaterial xMaterial, int slot) {
        if (original != null && block)
            ManageHandler.getModernAPI().setBlock(Objects.requireNonNull(XMaterial.AIR.parseItem()),
                    original.clone().add(0, -1, 0).getBlock());
        if (location != null) {
            if (block)
                ManageHandler.getModernAPI().setBlock(Objects.requireNonNull(xMaterial.parseItem()),
                        location.clone().add(0, -1, 0).getBlock());
            locations.setItem(slot, ItemBuilder.start(Objects.requireNonNull(getLocationsGUI().getItem(slot)))
                    .setGlowing(true).build());
            return;
        }
        locations.setItem(slot,
                ItemBuilder.start(Objects.requireNonNull(getLocationsGUI().getItem(slot))).setGlowing(false).build());
    }

    public void setLobbyLocationLoser(Location lobbyLocationLoser, boolean block) {
        setBlock(lobbyLocationLoser, block, this.lobbyLocationLoser, XMaterial.RED_WOOL, 14);
        this.lobbyLocationLoser = lobbyLocationLoser;
    }

    public void setLobbyLocationWinner(Location lobbyLocationWinner, boolean block) {
        setBlock(lobbyLocationWinner, block, this.lobbyLocationWinner, XMaterial.LIME_WOOL, 12);
        this.lobbyLocationWinner = lobbyLocationWinner;
    }

    public void setFirstLocation(Location firstLocation) {
        secondLocation = null;
        setLobbyLocationLoser(null, true);
        setLobbyLocationWinner(null, true);
        this.firstLocation = firstLocation;
        locations.setItem(10,
                ItemBuilder.start(Objects.requireNonNull(locations.getItem(10))).setGlowing(true).build());
    }

    private void setupInventory() {
        this.inventory = InventoryBuilder.InventoryCreate("&c&lArena &e&l" + this.getName(), 45)
                .generateArenaInventory().getInventory();
        this.locations = InventoryBuilder.InventoryCreate("", 45).generateLocationsInventory().getInventory();
        this.settings = InventoryBuilder.InventoryCreate("", 45).generateOptionsInventory().getInventory();
        this.paginatedGames = InventoryBuilder.generateGamesInventory();
    }

    public void setup(boolean generateDefaults) {
        secondLocation = getFirstLocation().add(Utils.getVector(baseYaw, type.getSizeArena() - 1));
        generateCube(generateDefaults);
        Location startLocation = firstLocation.clone()
                .add(Utils.getToSquare(baseYaw).multiply(type.getDistanceCorners() - 1).add(new Vector(0, 1, 0)))
                .add(Utils.getToSquare(baseYaw - 90).multiply(type.getDistanceCorners()));
        int a = 0;
        for (int i = 0; i < type.getCuboids(); i++) {
            for (int j = 0; j < type.getCuboids(); j++) {
                Location current = startLocation.clone().add(Utils.getToSquare(baseYaw)
                        .multiply(type.getSizeSquares() + type.getDistanceSquares()).multiply(j));
                Square square = new Square(current, baseYaw, type.getSizeSquares(), generateDefaults);
                squares[a] = square;
                a++;
            }
            startLocation
                    .add(Utils.getToSquare(baseYaw - 90).multiply(type.getSizeSquares() + type.getDistanceSquares()));
        }
        setEmpty(new int[] { 14, 12 }, true, getEditGUI());
        getLocationsGUI().setItem(16,
                ItemBuilder.start(Objects.requireNonNull(getLocationsGUI().getItem(16))).setGlowing(true).build());
    }

    public void setType(String type) {
        Arena.Type aType = Arena.parseType(type);
        getSettingsGUI().setItem(15,
                ItemBuilder.start(Objects.requireNonNull(getItemStackType(aType))).setGlowing(true).build());
        if (this.type != null && type.equals(this.type.toString()))
            return;
        this.type = aType;
        this.maxPlayers = aType.getPlayers();
        squares = new Square[this.type.getCuboids() * this.type.getCuboids()];
    }

    public void setBaseYaw(float baseYaw) {
        this.baseYaw = baseYaw;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setMinPlayers(int players) {
        this.minPlayers = players;
        List<String> strings = Objects.requireNonNull(EnumBuilder.PLAYERS.getItemStack().getItemMeta()).getLore();
        assert strings != null;
        strings.add(Utils.translate("&7Current value: &c&l" + getMinPlayers()));
        if (players != 0) {
            getEditGUI().setItem(16, ItemBuilder.start(EnumBuilder.PLAYERS.getItemStack())
                    .setLore(strings).build());
            return;
        }
        getEditGUI().setItem(16, EnumBuilder.PLAYERS.getItemStack());
    }

    public void destroyCuboid() {
        if (cuboid == null)
            return;
        cuboid.getLocations().forEach(location -> ManageHandler.getModernAPI()
                .setBlock(Objects.requireNonNull(XMaterial.AIR.parseItem()), location.getBlock()));
        cuboid = null;
        secondLocation = null;
        setLobbyLocationWinner(null, true);
        setLobbyLocationLoser(null, true);
        setEmpty(new int[] { 16, 14, 12 }, false, getLocationsGUI());
        getEditGUI().setItem(14, ItemBuilder.start(EnumBuilder.SQUARES.getItemStack()).setGlowing(false).build());
        getEditGUI().setItem(12, ItemBuilder.start(EnumBuilder.FLOOR.getItemStack()).setGlowing(false).build());
    }

    public void destroySquares() {
        for (Square square : squares) {
            square.getLocations().forEach(location -> ManageHandler.getModernAPI()
                    .setBlock(Objects.requireNonNull(XMaterial.AIR.parseItem()), location.getBlock()));
        }
    }

    public void unloadChunks() {
        if (!ManageHandler.getModernAPI().isLegacy())
            Arrays.stream(Objects.requireNonNull(Bukkit.getWorld(getName())).getLoadedChunks())
                    .forEach(chink -> chink.unload(true));
    }

    private void setEmpty(int[] ints, boolean bool, Inventory... inventories) {
        for (Inventory inventory : inventories) {
            for (int i : ints)
                inventory.setItem(i, ItemBuilder
                        .start(Objects.requireNonNull(inventory.getItem(i)))
                        .setGlowing(bool)
                        .build());
        }
    }

    /* |Getters| */

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getLobbyLocation() {
        return cuboid.getCenter().clone();
    }

    public Inventory getEditGUI() {
        return inventory;
    }

    public List<Inventory> getPaginatedGames() {
        return paginatedGames;
    }

    public Location getLobbyLocationLoser() {
        return lobbyLocationLoser;
    }

    public Location getLobbyLocationWinner() {
        return lobbyLocationWinner;
    }

    public Square[] getSquares() {
        return squares;
    }

    public Set<String> getDisabledGames() {
        return disabledGames;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getSize() {
        return type.getSizeArena();
    }

    public float getBaseYaw() {
        return baseYaw;
    }

    public Location getSecondLocation() {
        return secondLocation == null ? null : secondLocation.clone();
    }

    public Location getFirstLocation() {
        return firstLocation == null ? null : firstLocation.clone();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    private int[] getSortedInts(int paramInt, int paramInt_2) {
        int[] ints = new int[2];
        if (paramInt <= paramInt_2) {
            ints[0] = paramInt;
            ints[1] = paramInt_2;
            return ints;
        }
        ints[1] = paramInt;
        ints[0] = paramInt_2;
        return ints;
    }

    public boolean closed = false;

    public Inventory getSettingsGUI() {
        return settings;
    }

    public Inventory getLocationsGUI() {
        return locations;
    }

    public Arena.Type getArenaType() {
        return this.type;
    }

    public String getTask() {
        return task;
    }

    public String getType() {
        return type == null ? null : type.toString();
    }

    public boolean canFinish() {
        return type != null && cuboid != null && minPlayers != 0 && firstLocation != null
                && lobbyLocationLoser != null && lobbyLocationWinner != null && squares != null;
    }

    public int getSlotFromGame(ItemStack itemStack) {
        for (Inventory inventory : paginatedGames) {
            for (int j = 0; j < 45; j++) {
                ItemStack itemStack1 = inventory.getItem(j);
                if (itemStack1 != null && itemStack1.hasItemMeta() &&
                        Objects.requireNonNull(itemStack1.getItemMeta()).hasDisplayName()
                        && itemStack1.getItemMeta().getDisplayName()
                                .equals(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName()))
                    return j;
            }
        }
        return -1;
    }

    public Inventory getInventoryFromGame(ItemStack itemStack) {
        for (Inventory inventory : paginatedGames) {
            for (int j = 0; j < 45; j++) {
                ItemStack itemStack1 = inventory.getItem(j);
                if (itemStack1 != null && itemStack1.hasItemMeta() &&
                        Objects.requireNonNull(itemStack1.getItemMeta()).hasDisplayName()
                        && itemStack1.getItemMeta().getDisplayName()
                                .equals(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName()))
                    return inventory;
            }
        }
        return null;
    }

    public static class Square {

        private final World world;

        private final List<Location> locations = new ArrayList<>();

        private final HashSet<String> stringHashSet = new HashSet<>();

        public Square(Location start, float yaw, int size, boolean generateDefaults) {
            world = start.getWorld();
            Location location = start.clone();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Vector vector = Utils.getToSquare(yaw).multiply(j + 1);
                    Location current = location.clone().add(vector);
                    if (generateDefaults && current.getBlock().getType() == Material.AIR)
                        ManageHandler.getModernAPI()
                                .setBlock(Objects.requireNonNull(XMaterial.LIME_TERRACOTTA.parseItem()),
                                        location.clone().add(vector).getBlock());
                    locations.add(current);
                }
                location.add(Utils.getToSquare(yaw - 90));
            }
            if (!generateDefaults)
                save();
        }

        public List<Location> getLocations() {
            return locations;
        }

        public void restore() {
            stringHashSet.forEach(s -> {
                String[] strings = s.split(":");
                int x = Integer.parseInt(strings[0]);
                int y = Integer.parseInt(strings[1]);
                int z = Integer.parseInt(strings[2]);
                ItemStack itemStack = XMaterial.valueOf(strings[3]).parseItem();
                assert itemStack != null;
                ManageHandler.getModernAPI().setBlock(itemStack, world.getBlockAt(x, y, z));
            });
        }

        public void save() {
            stringHashSet.clear();
            locations.forEach(location -> {
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();

                // Get the block type and convert to appropriate item
                Material blockType = location.getBlock().getType();
                String item;

                // Handle wall signs by converting them to their corresponding sign item
                if (blockType.name().contains("WALL_SIGN")) {
                    String signType = blockType.name().replace("_WALL_SIGN", "_SIGN");
                    try {
                        Material signMaterial = Material.valueOf(signType);
                        item = XMaterial.matchXMaterial(signMaterial).name();
                    } catch (Exception e) {
                        // Fallback to OAK_SIGN if conversion fails
                        item = XMaterial.OAK_SIGN.name();
                    }
                } else {
                    // For other blocks, try to get the corresponding item
                    ItemStack itemStack;
                    if (ManageHandler.getModernAPI().isLegacy()) {
                        // noinspection deprecation
                        itemStack = location.getBlock().getState().getData().toItemStack();
                    } else {
                        itemStack = new ItemStack(blockType);
                    }

                    String materialName = XMaterial.toString(itemStack);
                    if (materialName != null) {
                        item = materialName;
                    } else {
                        // Fallback if XMaterial.toString fails
                        item = XMaterial.matchXMaterial(blockType).name();
                    }
                }

                stringHashSet.add(x + ":" + y + ":" + z + ":" + item);
            });
        }
    }

    public static ItemStack getItemStackType(Arena.Type type) {
        switch (type) {
            case DEFAULT:
                return TYPE.getItem(14);
            case MEGA:
                return TYPE.getItem(16);
            case MINI:
                return TYPE.getItem(12);
            case MICRO:
                return TYPE.getItem(10);
        }
        return null;
    }

    public static void finish(Properties properties) {
        Arena arena = new Arena(properties);
        MinerPlugin.getARENA_REGISTRY().add(arena);
        SetupManager.saveArena(arena);
    }

}
