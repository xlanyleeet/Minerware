package org.gr_code.minerware.games.microgames;

import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.sendTitle;
import static org.gr_code.minerware.manager.type.Utils.translate;

public class KnockEveryone extends MicroGame {

    private static final ItemStack[] greenArmor = new ItemStack[4];
    private static final ItemStack[] yellowArmor = new ItemStack[4];
    private static final ItemStack[] orangeArmor = new ItemStack[4];
    private static final ItemStack[] redArmor = new ItemStack[4];
    static {
        ItemStack[] armor = {XMaterial.LEATHER_HELMET.parseItem(), XMaterial.LEATHER_CHESTPLATE.parseItem(),
                XMaterial.LEATHER_LEGGINGS.parseItem(), XMaterial.LEATHER_BOOTS.parseItem()};
        for (int i = 0; i < 4; i ++) {
            ItemStack itemStack = armor[i];
            LeatherArmorMeta meta = requireNonNull((LeatherArmorMeta) requireNonNull(itemStack).getItemMeta());
            greenArmor[i] = new ItemStack(itemStack);
            yellowArmor[i] = new ItemStack(itemStack);
            orangeArmor[i] = new ItemStack(itemStack);
            redArmor[i] = new ItemStack(itemStack);
            meta.setColor(Color.LIME);
            greenArmor[i].setItemMeta(meta);
            meta.setColor(Color.YELLOW);
            yellowArmor[i].setItemMeta(meta);
            meta.setColor(Color.ORANGE);
            orangeArmor[i].setItemMeta(meta);
            meta.setColor(Color.RED);
            redArmor[i].setItemMeta(meta);
        }
    }

    public KnockEveryone(Arena arena) {
        super(480, arena, "knockback");
    }

    @Override
    public void secondStartGame() {
        if (getArena().isHardMode()) getArena().getProperties().destroySquares();
        int count = getArena().isHardMode() ? 3 : 2;
        ItemStack slime = ItemBuilder.start(requireNonNull(XMaterial.SLIME_BALL.parseItem())).addEnchantment(Enchantment.KNOCKBACK, count, true).build();
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        Cuboid cuboid = getArena().getProperties().getCuboid();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            setArmor(player, "GREEN");
            gamePlayer.setTask("GREEN");
            player.getInventory().setItem(0, slime);
            player.getInventory().setHeldItemSlot(0);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
        });
    }

    private void setArmor(Player player, String color) {
        ItemStack[] armor;
        switch (color) {
            case "GREEN":
                armor = greenArmor; break;
            case "YELLOW":
                armor = yellowArmor; break;
            case "ORANGE":
                armor = orangeArmor; break;
            case "RED":
                armor = redArmor; break;
            default: return;
        }
        player.getInventory().setHelmet(armor[0]);
        player.getInventory().setChestplate(armor[1]);
        player.getInventory().setLeggings(armor[2]);
        player.getInventory().setBoots(armor[3]);
        player.getInventory().setItem(8, armor[1]);
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        if (playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).count() == 1) setTime(1);
        playerList.stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @Override
    public void end() {
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME)
                .forEach(gamePlayer -> onWin(gamePlayer.getPlayer(), false));
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.KNOCK_EVERYONE;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.SLIME_BALL.parseItem())).setDisplayName("&e&lKNOCK EVERYONE").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();
        UUID uuid = player.getUniqueId();
        GamePlayer damagerPlayer = getArena().getPlayer(damager.getUniqueId());
        GamePlayer entityPlayer = requireNonNull(getArena().getPlayer(uuid));
        boolean damagerIsPlaying = requireNonNull(damagerPlayer).getState() == State.PLAYING_GAME;
        boolean playerIsPlaying = requireNonNull(entityPlayer).getState() == State.PLAYING_GAME;
        if (!(damagerIsPlaying && playerIsPlaying && entityPlayer.getTask() != null)) return;
        e.setCancelled(false);
        e.setDamage(0);
        Vector knock = damager.getLocation().getDirection().normalize();
        damager.setHealth(20); player.setHealth(20);
        switch (entityPlayer.getTask()) {
            case "GREEN":
                setArmor(player, "YELLOW");
                player.setVelocity(new Vector(knock.getX() * 0.5, 0.15, knock.getZ() * 0.5));
                entityPlayer.setTask("YELLOW");
                break;
            case "YELLOW":
                setArmor(player, "ORANGE");
                player.setVelocity(new Vector(knock.getX() * 2, 0.15, knock.getZ() * 2));
                entityPlayer.setTask("ORANGE");
                break;
            case "ORANGE":
                setArmor(player, "RED");
                player.setVelocity(new Vector(knock.getX() * 4, 0.15, knock.getZ() * 4));
                entityPlayer.setTask("RED");
                break;
            case "RED":
                player.setVelocity(new Vector(knock.getX() * 8, 0.15, knock.getZ() * 8));
                break;
        }
    }

}