package org.gr_code.minerware.manager.type.nms.version;

import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Button;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.manager.type.nms.NMS;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ALL")
public class v1_11_R1 implements NMS {

    @Override
    public Consumer<Location> getGeneratorDoors() {
        byte dataTop = (0x8), dataDown = (0x4);
        ItemStack planks = requireNonNull(XMaterial.OAK_PLANKS.parseItem());
        return location -> {
            setBlock(planks, location.getBlock());
            Block down = location.clone().add(0,1,0).getBlock(), top = location.clone().add(0,2,0).getBlock();
            setBlock(64, dataDown, down);
            setBlock(64, dataTop, top);
            BlockState downState = down.getState();
            org.bukkit.material.Door doorDown = (org.bukkit.material.Door) downState.getData();
            doorDown.setOpen(Math.random() <= 0.5);
            downState.setData(doorDown);
            downState.update();
        };
    }

    @Override
    public void setUpDirectionButton(Block block) {
        BlockState bs = block.getState();
        Button but = (Button) bs.getData();
        but.setFacingDirection(BlockFace.UP);
        bs.setData(but);
        bs.update(true);
    }

    private final EnumParticle[] enumParticles = new EnumParticle[]
            {EnumParticle.VILLAGER_ANGRY, EnumParticle.BARRIER, EnumParticle.WATER_BUBBLE, EnumParticle.CLOUD, EnumParticle.CRIT,
                    EnumParticle.EXPLOSION_NORMAL, EnumParticle.FLAME, EnumParticle.VILLAGER_HAPPY, EnumParticle.HEART, EnumParticle.EXPLOSION_HUGE,
                    EnumParticle.EXPLOSION_LARGE, EnumParticle.SMOKE_LARGE, EnumParticle.LAVA, EnumParticle.CRIT_MAGIC, EnumParticle.NOTE, EnumParticle.PORTAL,
                    EnumParticle.REDSTONE, EnumParticle.SMOKE_NORMAL, EnumParticle.SNOWBALL};

    @Override
    public void playOutParticle(Location location, float offSet, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, 0, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, float offSet, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, speed, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, float offSetX, float offSetY, float offSetZ, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, 0, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void sendActionBar(Player player, String text) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(new ChatComponentText(text), (byte) 2);
        playerConnection.sendPacket(packetPlayOutChat);
    }


    @Override
    public void playOutParticle(Location location, float offSetX, float offSetY, float offSetZ, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, speed, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSet, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, 0, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSet, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, speed, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSetX, float offSetY, float offSetZ, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, 0, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSetX, float offSetY, float offSetZ, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles((EnumParticle) decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, speed, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void spawnRedstoneParticle(Location location, float red, float green, float blue, float size) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, false, x, y, z, red, green, blue, 1, 0);
        for (Player player : Bukkit.getOnlinePlayers())
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void spawnRedstoneParticle(Location location, Player player, float red, float green, float blue, float size) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, false, x, y, z, red, green, blue, 1, 0);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public boolean isLegacy() {
        return true;
    }

    @Override
    public int getTypeId(Block block) {
        return ((CraftBlock)block).getTypeId();
    }

    @Override
    public void setBlock(ItemStack baseItem, Block block) {
        ((CraftBlock)block).setTypeIdAndData(baseItem.getType().getId(), Objects.requireNonNull(baseItem.getData()).getData(), false);
    }

    @Override
    public void setBlock(int id, byte data, Block block) {
        ((CraftBlock)block).setTypeIdAndData(id, data, false);
    }

    @Override
    public ItemStack setUnbreakable(ItemStack itemStack, boolean bool) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        try {
            assert itemMeta != null;
            Method spigotMethod = itemMeta.getClass().getDeclaredMethod("spigot");
            spigotMethod.setAccessible(true);
            Object spigotInstance = spigotMethod.invoke(itemMeta);
            Class<?> spigotClass = spigotInstance.getClass();
            Method setUnbreakable = spigotClass.getDeclaredMethod("setUnbreakable", boolean.class);
            setUnbreakable.setAccessible(true);
            setUnbreakable.invoke(spigotInstance, bool);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void spawnIceWinEffect(Player player, Location location) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), blockPosition);
        packetPlayOutBlockChange.block = Blocks.ICE.getBlockData();
        playerConnection.sendPacket(packetPlayOutBlockChange);
    }

    @Override
    public void sendRestorePackets(Player player, Arena arena) {
        Cuboid cuboid = arena.getProperties().getCuboid();
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        cuboid.getLocations().stream().filter(location -> location.getBlockY() == arena.getProperties().getFirstLocation().getBlockY()
                || location.getBlockY() == arena.getProperties().getFirstLocation().getBlockY()+1)
                .forEachOrdered(location ->
                {
                    BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
                    IBlockData iBlockData = ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle().getType(blockPosition);
                    PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), blockPosition);
                    packetPlayOutBlockChange.block = iBlockData;
                    PacketPlayOutBlockBreakAnimation packetPlayOutBlockBreakAnimation = new PacketPlayOutBlockBreakAnimation(0, blockPosition, 10);
                    playerConnection.sendPacket(packetPlayOutBlockChange);
                    playerConnection.sendPacket(packetPlayOutBlockBreakAnimation);
                });
    }

    @Override
    public void updateBlocksWinEffect(Player player) {
        Location location = player.getLocation().add(0, -1, 0);
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        BlockPosition[] blockPositions = new BlockPosition[]{blockPosition, blockPosition.east(), blockPosition.south(), blockPosition.north(), blockPosition.west()};
        for(BlockPosition block : blockPositions) {
            PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), block);
            packetPlayOutBlockChange.block = Blocks.PACKED_ICE.getBlockData();
            PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
            if(!(player.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).getType() == org.bukkit.Material.AIR))
                playerConnection.sendPacket(packetPlayOutBlockChange);
        }
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.SNOWBALL, false , x, y+1.5f, z, 2, 0.5f, 0.5f, 0.5f, 3);
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        int random = new Random().nextInt(3);
        if(random == 2)
            playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void updateIceWinEffect(Player player, Location location) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false, (float) location.getX()+0.5f, (float) location.getY()+1.0f, (float) location.getZ()+0.5f, 0.5f, 0.5f, 0.5f, 0, 1);
        int random = new Random().nextInt(5);
        if(random == 2)
            playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int showTime, int fadeOut) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        IChatBaseComponent titleComponent = new ChatMessage(title);
        IChatBaseComponent subTitleComponent = new ChatMessage(subTitle);
        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleComponent, fadeIn, showTime, fadeOut);
        PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subTitleComponent, fadeIn, showTime, fadeOut);
        PacketPlayOutTitle packetPlayOutTimings = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, titleComponent, fadeIn, showTime, fadeOut);
        playerConnection.sendPacket(packetPlayOutTitle);
        if(subTitle != null)
            playerConnection.sendPacket(packetPlayOutSubTitle);
        playerConnection.sendPacket(packetPlayOutTimings);
    }

    @Override
    public void updateFireWinEffect(Player player) {
        Location location = player.getLocation().clone();
        location = location.add(location.getDirection().setY(0).normalize().multiply(-1.5)).clone();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        BlockPosition[] blockPositions = new BlockPosition[]{blockPosition.east(), blockPosition.west()};
        for(BlockPosition block : blockPositions) {
            PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), block);
            packetPlayOutBlockChange.block = Blocks.FIRE.getBlockData();
            PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
            if(player.getWorld().getBlockAt(block.getX(), block.getY()-1, block.getZ()).getType() != org.bukkit.Material.AIR)
                playerConnection.sendPacket(packetPlayOutBlockChange);
        }
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.LAVA, false , x, y+1.5f, z, 0.5f, 0.3f, 0.5f, 2.5f, 8);
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        int random = new Random().nextInt(4);
        if(random == 2)
            playerConnection.sendPacket(packetPlayOutWorldParticles);
    }


    @Override
    public void updateRocketWinEffect(Player player, int stage) {
        Location location = player.getLocation().clone();
        double x = location.getX();
        double y = location.getY() - 0.75;
        double z = location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false ,(float) x, (float) y,(float) z, 0.1f, 0, 0.1f, 0.15f, Math.max(7, stage / 20));
        PacketPlayOutWorldParticles packetPlayOutWorldParticlesFlame = new PacketPlayOutWorldParticles(EnumParticle.LAVA, false ,(float) x,(float) y, (float) z, 0.1f, 0, 0.1f, 0.1f, Math.max(4, stage / 20));
        for(Player pl : Bukkit.getOnlinePlayers()){
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticlesFlame);
        }
        if(stage == 100){
            player.setAllowFlight(true);
            BlockPosition blockPosition = new BlockPosition(x, y-0.25, z);
            BlockPosition[] blockPositions = new BlockPosition[]{blockPosition ,blockPosition.east(), blockPosition.west(), blockPosition.north(), blockPosition.south()};
            for(BlockPosition block : blockPositions) {
                PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), block);
                packetPlayOutBlockChange.block = Blocks.STONE.getBlockData();
                PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
                if(player.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).getType() != org.bukkit.Material.AIR)
                    playerConnection.sendPacket(packetPlayOutBlockChange);
                return;
            }
        }
        if(stage > 100)
            player.setVelocity(new Vector(0, 0.13, 0));
    }

    @Override
    public void updateExplosionWinEffect(Player player, int stage) {
        Location location = player.getLocation().add(0, -1, 0);
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if(stage < 70){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_LARGE, false , x, y+1.5f, z, 10, 10.5f, 10.5f, 1.5f, 4);
            PacketPlayOutWorldParticles packetPlayOut = new PacketPlayOutWorldParticles(EnumParticle.LAVA, false , x, y+1.5f, z, 10, 7.5f, 7.5f, 1.5f, 25);
            PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
            playerConnection.sendPacket(packetPlayOutWorldParticles);
            playerConnection.sendPacket(packetPlayOut);
            int random = new Random().nextInt(5);
            if(random == 1)
                player.playSound(player.getLocation(), Objects.requireNonNull(XSound.ENTITY_GENERIC_EXPLODE.parseSound()), 0.5f, 0f);
            return;
        }
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        BlockPosition[] blockPositions = new BlockPosition[]{blockPosition,
                blockPosition.east(), blockPosition.south(), blockPosition.north(), blockPosition.west(),
                blockPosition.west().south(), blockPosition.south().south(), blockPosition.north().south(), blockPosition.north().east(), blockPosition.west().north()};
        for(BlockPosition block : blockPositions) {
            PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), block);
            packetPlayOutBlockChange.block = Blocks.TNT.getBlockData();
            PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
            if(!(player.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()).getType() == org.bukkit.Material.AIR))
                playerConnection.sendPacket(packetPlayOutBlockChange);
        }
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(EnumParticle.SMOKE_LARGE, false , x, y+1.5f, z, 2, 0.5f, 0.5f, 0.05f, 13);
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    public Object decodeParticle(Particle particle) {
        int id = particle.getId();
        return enumParticles[id-1];
    }

}
