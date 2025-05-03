package org.gr_code.minerware.manager.type.nms.version;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Door;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.manager.type.nms.NMS;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("ALL")
public class v1_16_R1 implements NMS {

    private final Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]{6})");

    private final char COLOR_CHAR = ChatColor.COLOR_CHAR;

    @Override
    public Consumer<Location> getGeneratorDoors() {
        ItemStack planks = requireNonNull(XMaterial.OAK_PLANKS.parseItem());
        return location -> {
            setBlock(planks, location.getBlock());
            Block down = location.clone().add(0,1,0).getBlock(), top = location.clone().add(0,2,0).getBlock();
            BlockState downState = down.getState(), upState = top.getState();
            Door doorDown = (Door) Bukkit.createBlockData(org.bukkit.Material.OAK_DOOR),
                    doorUp = (Door) Bukkit.createBlockData(Material.OAK_DOOR);
            doorDown.setHalf(Bisected.Half.BOTTOM);doorUp.setHalf(Bisected.Half.TOP);
            boolean open = Math.random() <= 0.5;
            doorDown.setOpen(open); doorUp.setOpen(open);
            down.setBlockData(doorDown); top.setBlockData(doorUp);
            downState.update(); upState.update();
        };
    }

    @Override
    public String translate(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    @Override
    public void setUpDirectionButton(Block block) {
        FaceAttachable button = (FaceAttachable) block.getBlockData();
        button.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
        block.setBlockData(button);
    }

    private final ParticleType[] enumParticles = new ParticleType[]
            {Particles.ANGRY_VILLAGER, Particles.BARRIER, Particles.BUBBLE, Particles.CLOUD, Particles.CRIT,
                    Particles.EXPLOSION, Particles.FLAME, Particles.HAPPY_VILLAGER, Particles.HEART, Particles.EXPLOSION_EMITTER,
                    Particles.EXPLOSION_EMITTER, Particles.LARGE_SMOKE, Particles.LAVA, Particles.ENCHANTED_HIT, Particles.NOTE, Particles.PORTAL,
                    Particles.CLOUD, Particles.SMOKE, Particles.ITEM_SNOWBALL};

    @Override
    public void playOutParticle(Location location, float offSet, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, 0, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, float offSet, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, speed, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, float offSetX, float offSetY, float offSetZ, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, 0, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, float offSetX, float offSetY, float offSetZ, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        for(Player player : Bukkit.getOnlinePlayers()){
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, speed, amount);
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
        }
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSet, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, 0, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSet, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSet, offSet, offSet, speed, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSetX, float offSetY, float offSetZ, Particle particle, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, 0, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void playOutParticle(Location location, Player player, float offSetX, float offSetY, float offSetZ, Particle particle, float speed, int amount) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(decodeParticle(particle), false, x, y, z, offSetX, offSetY, offSetZ, speed, amount);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void spawnRedstoneParticle(Location location, float red, float green, float blue, float size) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(new ParticleParamRedstone(red, green, blue, size), false, x, y, z, 0, 0, 0, 0, 1);
        for (Player player : Bukkit.getOnlinePlayers())
            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void spawnRedstoneParticle(Location location, Player player, float red, float green, float blue, float size) {
        float x = (float) location.getX();
        float y = (float) location.getY();
        float z = (float) location.getZ();
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(new ParticleParamRedstone(red, green, blue, size), false, x, y, z, 0, 0, 0, 0, 1);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public int getTypeId(Block block) {
        return 0;
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
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(Particles.ITEM_SNOWBALL, false , x, y+1.5f, z, 2, 0.5f, 0.5f, 0.5f, 3);
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        int random = new Random().nextInt(3);
        if(random == 2)
            playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void updateIceWinEffect(Player player, Location location) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(Particles.CLOUD, false, (float) location.getX()+0.5f, (float) location.getY()+1.0f, (float) location.getZ()+0.5f, 0.5f, 0.5f, 0.5f, 0, 1);
        int random = new Random().nextInt(5);
        if(random == 2)
            playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int showTime, int fadeOut) {
        player.sendTitle(title, subTitle, fadeIn, showTime, fadeIn);
    }

    @Override
    public void sendActionBar(Player player, String text) {
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(new ChatComponentText(text), ChatMessageType.GAME_INFO, player.getUniqueId());
        playerConnection.sendPacket(packetPlayOutChat);
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
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(Particles.LAVA, false , x, y+1.5f, z, 0.5f, 0.3f, 0.5f, 2.5f, 8);
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
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(Particles.CLOUD, false ,(float) x, (float) y,(float) z, 0.1f, 0, 0.1f, 0.15f, Math.max(7, stage / 20));
        PacketPlayOutWorldParticles packetPlayOutWorldParticlesFlame = new PacketPlayOutWorldParticles(Particles.LAVA, false ,(float) x,(float) y, (float) z, 0.1f, 0, 0.1f, 0.1f, Math.max(4, stage / 20));
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
            PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(Particles.EXPLOSION_EMITTER, false , x, y+1.5f, z, 10, 10.5f, 10.5f, 1.5f, 4);
            PacketPlayOutWorldParticles packetPlayOut = new PacketPlayOutWorldParticles(Particles.LAVA, false , x, y+1.5f, z, 10, 7.5f, 7.5f, 1.5f, 25);
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
        PacketPlayOutWorldParticles packetPlayOutWorldParticles = new PacketPlayOutWorldParticles(Particles.LARGE_SMOKE, false , x, y+1.5f, z, 2, 0.5f, 0.5f, 0.05f, 13);
        PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
        playerConnection.sendPacket(packetPlayOutWorldParticles);
    }

    private ParticleType decodeParticle(Particle particle) {
        int id = particle.getId();
        return enumParticles[id-1];
    }

}
