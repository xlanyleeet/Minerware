package org.gr_code.minerware.games.microgames;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.GamePlayer;
import org.gr_code.minerware.arena.GamePlayer.State;
import org.gr_code.minerware.builders.ItemBuilder;
import org.gr_code.minerware.cuboid.Cuboid;
import org.gr_code.minerware.games.Game;
import org.gr_code.minerware.games.MicroGame;
import org.gr_code.minerware.manager.ManageHandler;
import org.gr_code.minerware.manager.type.resources.XMaterial;
import org.gr_code.minerware.manager.type.resources.XSound;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.gr_code.minerware.cuboid.Cuboid.getRandomLocation;
import static org.gr_code.minerware.manager.type.Utils.*;

public class FeedAnimals extends MicroGame {

    public List<Entity> entityList;
    private final EntityType[] typeList;
    public ItemStack[] itemList;

	public FeedAnimals(Arena arena) {
		super(380, arena, "feed-animals");
		entityList = new ArrayList<>();
		typeList = new EntityType[]{EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN};
		itemList = new ItemStack[]{XMaterial.WHEAT.parseItem(), XMaterial.WHEAT.parseItem(), XMaterial.WHEAT_SEEDS.parseItem()};
	}

    @Override
    public void onWin(Player player, boolean teleport) {
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(player.getUniqueId()));
		if (requireNonNull(gamePlayer).getState() != State.PLAYING_GAME) return;
		super.onWin(player, teleport);
		String feedAll = translate(requireNonNull(getString("messages.fed-eight-animals")));
		sendMessage(player, feedAll);
    }

    @Override
	public String getAchievementForMsg() {
		String achievementMsg = getString("messages.achievement");
		List<GamePlayer> achievement = getArena().getPlayers().stream().filter(x -> Integer.parseInt(x.getTask()) > 0).collect(Collectors.toList());
		if (achievement.isEmpty()) return "";
		int maximum = 0;
		GamePlayer gamePlayer = null;
		for (GamePlayer key : achievement) {
			int doubleKey = Integer.parseInt(key.getTask());
			if (doubleKey <= maximum) continue;
			maximum = doubleKey;
			gamePlayer = key;
		}
		assert achievementMsg != null;
		String name = gamePlayer.getPlayer().getName();
		return achievementMsg.replace("<name>", name).replace("<count>", Integer.toString(maximum));
	}
	
	private void generateHay() {
		getArena().getProperties().destroySquares();
		Location first = getArena().getProperties().getFirstLocation();
		Location second = getArena().getProperties().getSecondLocation();
		Cuboid cuboid = getArena().getProperties().getCuboid();
		cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY())
				.forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.GRASS_BLOCK.parseItem()), l.getBlock()));
		cuboid.getLocations().stream().filter(l -> l.getBlockY() == first.getBlockY() + 1)
				.filter(l -> l.getBlockX() == first.getBlockX() || l.getBlockZ() == first.getBlockZ()
						|| l.getBlockX() == second.getBlockX() || l.getBlockZ() == second.getBlockZ())
				.forEach(l -> ManageHandler.getModernAPI().setBlock(requireNonNull(XMaterial.OAK_FENCE.parseItem()), l.getBlock()));
	}
	
	private void spawnAllEntities(int random) {
		for (int i = 0; i < (getArena().getCurrentPlayers() * 6); i ++) {
			Entity entity = requireNonNull(getArena().getProperties().getFirstLocation().getWorld()).spawnEntity(getRandomLocation(getArena()), typeList[random]);
			if (!ManageHandler.getModernAPI().oldVersion()) {
				entity.setGlowing(true);
				entity.setSilent(true);
			}
			entity.setMetadata("not fed", new FixedMetadataValue(MinerPlugin.getInstance(), "not fed"));
			entityList.add(entity);
		}
	}

	@Override
    public void secondStartGame() {
		int random = getArena().isHardMode() ? 2 : new Random().nextInt(2);
		generateHay();
		ItemStack item = itemList[random];
		item.setAmount(64);
		String title = translate(getString("titles.start-game"));
		String subtitle = translate(getString("titles.task"));
		Cuboid cuboid = getArena().getProperties().getCuboid();
    	getArena().getPlayers().forEach(gamePlayer -> {
    		gamePlayer.setTask("0");
    		Player player = gamePlayer.getPlayer();
			if (cuboid.notInside(player.getLocation())) player.teleport(getRandomLocation(getArena()));
			sendTitle(player, title, subtitle, 0, 70, 20);
            gamePlayer.setState(State.PLAYING_GAME);
            player.getInventory().setItem(0, item);
            player.getInventory().setHeldItemSlot(0);
			player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
    	});
    	spawnAllEntities(random);
    }
    
    @Override
    public void check() {
		if (getTime() % 5 != 0) return;
		List<GamePlayer> playerList = getArena().getPlayers();
		if (playerList.stream().allMatch(gamePlayer -> gamePlayer.getState() != State.PLAYING_GAME)) setTime(1);
		int param_y = getArena().getProperties().getFirstLocation().getBlockY() - 1 ;
		playerList.stream().filter(x->x.getState() == State.PLAYING_GAME).forEach(gamePlayer -> {
    		Player player = gamePlayer.getPlayer();
    		int y = player.getLocation().getBlockY();
        	if (y <= param_y) onLose(player, true);
    	});
    }

    @Override
	public void end() {
		getArena().getPlayers().stream().filter(x -> x.getState() == State.PLAYING_GAME)
				.forEach(gamePlayer -> onLose(gamePlayer.getPlayer(), false));
		entityList.forEach(Entity::remove);
		entityList.clear();
		super.end();
	}

    @Override
    public Game getGame() {
        return Game.FEED_ANIMALS;
    }

	@Override
	public String getTask(GamePlayer gamePlayer) {
		return translate(getString("titles.task"));
	}

	@Override
    public void aFinish(boolean forceStop) {
    	super.aFinish(forceStop);
    	entityList.forEach(Entity::remove);
        entityList.clear();
    }
    
    @Override
	public ItemStack getGameItemStack() {
    	return ItemBuilder.start(requireNonNull(XMaterial.WHEAT.parseItem())).setDisplayName("&e&lFEED ANIMALS").build();
	}

	private boolean containsItem(List<ItemStack> list, ItemStack itemStack) {
    	return list.stream().anyMatch(item -> item.isSimilar(itemStack));
	}
    
    @Override
	public void event(Event event) {
		if (event instanceof PlayerInteractEntityEvent) playerInteractEntity(event);
		else if (event instanceof CreatureSpawnEvent) creatureSpawn(event);
	}

	@SuppressWarnings("deprecation")
	private void playerInteractEntity(Event event) {
		PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		if (!(e.getRightClicked() instanceof Cow || e.getRightClicked() instanceof Sheep || e.getRightClicked() instanceof Chicken)) return;
		ItemStack itemStack = ManageHandler.getModernAPI().oldVersion() ? e.getPlayer().getInventory().getItemInHand() : e.getPlayer().getInventory().getItemInMainHand();
		if (itemStack == null) return;
		if (!entityList.contains(e.getRightClicked())) return;
		if (!containsItem(Arrays.asList(itemList.clone()), itemStack)) return;
		if (e.getRightClicked().hasMetadata("fed")) return;
		GamePlayer gamePlayer = requireNonNull(getArena().getPlayer(uuid));
		gamePlayer.setTask((Integer.parseInt(gamePlayer.getTask()) + 1) + "");
		String msg = translate(requireNonNull(getString("messages.plus-animal")).replace("<countAnimals>", gamePlayer.getTask()));
		if (!ManageHandler.getModernAPI().oldVersion()) e.getRightClicked().setGlowing(false);
		e.getRightClicked().setMetadata("fed", new FixedMetadataValue(MinerPlugin.getInstance(), "fed"));
		player.playSound(player.getLocation(), requireNonNull(XSound.ENTITY_ARROW_HIT_PLAYER.parseSound()), 5, 1);
		if (Integer.parseInt(gamePlayer.getTask()) == 8) onWin(player, false);
		else sendMessage(player, msg);
	}

	private void creatureSpawn(Event event) {
		CreatureSpawnEvent e = (CreatureSpawnEvent) event;
		if (!(e.getEntity() instanceof Cow || e.getEntity() instanceof Sheep || e.getEntity() instanceof Chicken)) return;
		e.setCancelled(false);
	}

}


