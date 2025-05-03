package org.gr_code.minerware.games.microgames;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.arena.Properties;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class SnowballFight extends MicroGame {
    
    private String colorRed;
    private String colorBlue;
    private String colorYellow;
    private String colorGreen;

    public SnowballFight(Arena arena) {
        super(280, arena, "snowball-fight");
    }

    @Override
    public void startGame() {
        colorRed = translate(getString("messages.color.red"));
        colorBlue = translate(getString("messages.color.blue"));
        colorYellow = translate(getString("messages.color.yellow"));
        colorGreen = translate(getString("messages.color.green"));
        super.startGame();
    }

    @Override
	public String getAchievementForMsg() {
        String achievementMsg = getString("messages.achievement");
        List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> x.getAchievement() != null).collect(Collectors.toList());
        if (achievement.isEmpty()) return "";
        int maximum = 0;
        GamePlayer gamePlayer = null;
        for (GamePlayer key : achievement) {
            int doubleKey = Integer.parseInt(key.getAchievement());
            if (doubleKey <= maximum) continue;
            maximum = doubleKey;
            gamePlayer = key;
        }
        String name = gamePlayer.getPlayer().getName();
        return requireNonNull(achievementMsg).replace("<name>", name).replace("<count>", Integer.toString(maximum));
    }

    @Override
    public String getWhoWon() {
        if (getArena().getPlayers().stream().allMatch(x -> x.getState() == State.WINNER_IN_GAME))
            return translate(getString("messages.who-won.all-won"));
        String taskColor = requireNonNull(getArena().getPlayers().stream().filter(x -> x.getState() == State.WINNER_IN_GAME).findFirst().orElse(null)).getTask();
        String whoWon = translate(getString("messages.who-won.team-won"));
        String color;
        switch (taskColor) {
            case "RED":
                color = colorRed;
                break;
            case "BLUE":
                color = colorBlue;
                break;
            case "YELLOW":
                color = colorYellow;
                break;
            default:
                color = colorGreen;
                break;
        }
        return whoWon.replace("<color>", color);
    }

    private void setColorArmor(GamePlayer gamePlayer, Color color) {
        ItemStack[] armor = {XMaterial.LEATHER_HELMET.parseItem(), XMaterial.LEATHER_CHESTPLATE.parseItem(),
                XMaterial.LEATHER_LEGGINGS.parseItem(), XMaterial.LEATHER_BOOTS.parseItem()};
        for (ItemStack itemStack : armor) {
            LeatherArmorMeta meta = (LeatherArmorMeta) requireNonNull(itemStack).getItemMeta();
            requireNonNull(meta).setColor(color);
            itemStack.setItemMeta(meta);
        }
        Player player = gamePlayer.getPlayer();
        requireNonNull(player).getInventory().setHelmet(armor[0]);
        player.getInventory().setChestplate(armor[1]);
        player.getInventory().setLeggings(armor[2]);
        player.getInventory().setBoots(armor[3]);
    }
    
    private void generate() {
        for (Properties.Square square : getArena().getProperties().getSquares()) square.getLocations().forEach(loc -> {
            for (int i = 0; i < 5; i ++) ManageHandler.getNMS()
                    .setBlock(requireNonNull(XMaterial.SNOW_BLOCK.parseItem()),
                            loc.clone().add(0, i,0).getBlock());
        });
        Location first = getArena().getProperties().getFirstLocation();
        getArena().getProperties().getCuboid().getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
                .forEach(loc -> ManageHandler.getNMS().setBlock(requireNonNull(XMaterial.SNOW_BLOCK.parseItem()), loc.getBlock()));
    }

    private void generateTeams() {
        String team = translate(getString("messages.team"));
        List<GamePlayer>[] result = getArena().isHardMode() ?
                new List[] {new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()}
                : new List[] {new ArrayList<>(), new ArrayList<>()};
        String[] teams = {"BLUE", "RED", "GREEN", "YELLOW"};
        String[] colorTeams = {colorBlue, colorRed, colorGreen, colorYellow};
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};
        int i = 0;
        int count = getArena().isHardMode() ? 4 : 2;
        for (GamePlayer gamePlayer : getArena().getPlayers()) {
            if (i == count) i = 0;
            result[i].add(gamePlayer);
            i++;
        }
        for (int j = 0; j < count; j ++) for (GamePlayer gamePlayer : result[j]) {
            gamePlayer.setTask(teams[j]);
            setColorArmor(gamePlayer, colors[j]);
            sendMessage(gamePlayer.getPlayer(), team.replace("<color>", colorTeams[j]));
        }
    }

    @Override
    public void secondStartGame() {
        ItemStack snowball = requireNonNull(XMaterial.SNOWBALL.parseItem());
        snowball.setAmount(64);
        String title = translate(getString("titles.start-game"));
        String subtitle = translate(getString("titles.task"));
        generate();
        Cuboid cuboid = getArena().getProperties().getCuboid();
        generateTeams();
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
            Block block = player.getLocation().getBlock();
            if (getItem(block).isSimilar(XMaterial.SNOW_BLOCK.parseItem())) 
                player.teleport(player.getLocation().add(0,4,0));
            sendTitle(player, title, subtitle,0,70,20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
            for (int i = 0; i < 9; i ++) player.getInventory().setItem(i, snowball);
        });
    }

    @Override
    public void check() {
        if (getTime() % 5 != 0) return;
        List<GamePlayer> playerList = getArena().getPlayers();
        if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
        int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
        getArena().getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            int y = player.getLocation().getBlockY();
            if (y <= param_y) onLose(player, true);
        });
    }

    @SuppressWarnings({"rawtypes", "SuspiciousListRemoveInLoop"})
    @Override
    public void end() {
        List<List<GamePlayer>> teams = new ArrayList<>(Arrays.asList(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        getArena().getPlayers().stream().filter(gamePlayer -> gamePlayer.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
            switch (gamePlayer.getTask()) {
                case "BLUE":
                    teams.get(0).add(gamePlayer);
                    break;
                case "RED":
                    teams.get(1).add(gamePlayer);
                    break;
                case "YELLOW":
                    teams.get(2).add(gamePlayer);
                    break;
                case "GREEN":
                    teams.get(3).add(gamePlayer);
                    break;
            }
        });
        for (int i = 0; i < teams.size(); i ++) if (teams.get(i).size() == 0) teams.remove(i);
        boolean equally = false; List<GamePlayer> winners = new ArrayList<>();
        for (List list : teams) {
            if (list.size() > winners.size()) winners = list;
            for (List twoList : teams) if (!list.equals(twoList) && list.size() == twoList.size()) {
                equally = true;
                break;
            }
        }
        if (teams.size() == 0 || equally) {
            getArena().getPlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                gamePlayer.setState(State.PLAYING_GAME);
                onWin(player, false);
            });
        } else {
            List<GamePlayer> all = new ArrayList<>(getArena().getPlayers());
            all.removeAll(winners);
            winners.forEach(gamePlayer -> {
                Player player = gamePlayer.getPlayer();
                gamePlayer.setState(State.PLAYING_GAME);
                onWin(player, false);
            });
            all.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
        }
        System.gc();
        super.end();
    }

    @Override
    public Game getGame() {
        return Game.SNOWBALL_FIGHT;
    }

    @Override
    public String getTask(GamePlayer gamePlayer) {
        return translate(getString("titles.task"));
    }

    @Override
    public ItemStack getGameItemStack() {
        return ItemBuilder.start(requireNonNull(XMaterial.SNOWBALL.parseItem())).setDisplayName("&f&lSNOWBALL FIGHT").build();
    }

    @Override
    public void event(Event event) {
        if (!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        UUID uuid = player.getUniqueId();
        GamePlayer entityGamePlayer = requireNonNull(getArena().getPlayer(uuid));
        e.setCancelled(true);
        if (e.getCause() == EntityDamageByEntityEvent.DamageCause.CUSTOM) return;
        if (requireNonNull(entityGamePlayer).getState() != State.PLAYING_GAME) return;
        if (!(e.getDamager() instanceof Projectile)) return;
        ProjectileSource damagerEn = ((Projectile) e.getDamager()).getShooter();
        if (!(damagerEn instanceof Player)) return;
        Player damager = (Player) damagerEn;
        UUID uuid2 = damager.getUniqueId();
        if (!isInGame(uuid2)) return;
        GamePlayer damagerGamePlayer = getArena().getPlayer(uuid2);
        if (requireNonNull(damagerGamePlayer).getState() != State.PLAYING_GAME) return;
        if (damagerGamePlayer.getTask().equals(entityGamePlayer.getTask())) return;
        onLose(player, true);
        if (damagerGamePlayer.getAchievement() == null) damagerGamePlayer.setAchievement("1");
        else damagerGamePlayer.setAchievement((Integer.parseInt(damagerGamePlayer.getAchievement()) + 1) + "");
        e.getDamager().remove();
    }

}