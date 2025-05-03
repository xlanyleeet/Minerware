package org.gr_code.minerware.manager.type.nms.hologram.version;

import net.minecraft.server.v1_13_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.SetupManager;
import org.gr_code.minerware.manager.type.StatisticManager;
import org.gr_code.minerware.manager.type.nms.hologram.IHologram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class v1_13_R1 implements IHologram {
    @Override
    public void spawnAll() {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> strings = fileConfiguration.getStringList("holograms.locations");
        List<String> holoStrings = fileConfiguration.getStringList("holograms.format");
        Location location = null;
        int size  = holoStrings.size();
        int count = 0;
        for(String s : strings){
            if(count == 0)
                location = null;
            for(String string : holoStrings){
                location = location == null ? SetupManager.fromString(s) : location.add(0,-0.26,0).clone();
                EntityArmorStand entityArmorStand = spawn(location, string);
                PlayerConnection playerConnection = ((CraftPlayer) Objects.requireNonNull(Bukkit.getPlayer(owner))).getHandle().playerConnection;
                playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(entityArmorStand));
                playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true));
                stands.add(entityArmorStand);
                count = count == size-1 ? 0 : count+1;
            }
        }
    }

    protected EntityArmorStand spawn(Location location, String name){
        EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle(), location.getX(), location.getY(), location.getZ());
        entityArmorStand.getBukkitEntity();
        entityArmorStand.setSmall(true);
        entityArmorStand.setInvisible(true);
        entityArmorStand.setArms(false);
        entityArmorStand.setBasePlate(false);
        entityArmorStand.setCustomNameVisible(true);
        entityArmorStand.setCustomName(new ChatComponentText(StatisticManager.replace(name, owner)));
        return entityArmorStand;
    }

    @Override
    public void destroyAll() {
        for (EntityArmorStand entityArmorStand : stands){
            PlayerConnection playerConnection = ((CraftPlayer) Objects.requireNonNull(Bukkit.getPlayer(owner))).getHandle().playerConnection;
            playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityArmorStand.getId()));
        }
        stands.clear();
    }

    @Override
    public void update() {
        FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();
        List<String> holoStrings = fileConfiguration.getStringList("holograms.format");
        int size = holoStrings.size();
        int count = 0;
        for(EntityArmorStand entityArmorStand : stands){
            PlayerConnection playerConnection = ((CraftPlayer) Objects.requireNonNull(Bukkit.getPlayer(owner))).getHandle().playerConnection;
            entityArmorStand.setCustomName(new ChatComponentText(StatisticManager.replace(holoStrings.get(count), owner)));
            playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true));
            count = count == size-1 ? 0 : count+1;
        }
    }

    @Override
    public UUID owner() {
        return owner;
    }

    @Override
    public List<EntityArmorStand> getStands() {
        return stands;
    }

    private UUID owner;

    private final List<EntityArmorStand> stands = new ArrayList<>();

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
