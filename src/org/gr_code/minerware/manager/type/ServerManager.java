package org.gr_code.minerware.manager.type;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.commands.subcommand.PluginCommand;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class ServerManager {

    public static void sendLobby(Player player) {
        String server = MinerPlugin.getInstance().getBungee().getString("hub");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeUTF("Connect");
            assert server != null;
            dataOutputStream.writeUTF(server);
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
        player.sendPluginMessage(MinerPlugin.getInstance(), "BungeeCord", byteArrayOutputStream.toByteArray());
    }

    @Nullable
    public static Arena getArena(UUID uuid) {
        return MinerPlugin.getARENA_REGISTRY().stream().filter(player -> player.getPlayer(uuid) != null).findAny()
                .orElse(null);
    }

    @Nullable
    public static Arena getArena(String paramString) {
        return MinerPlugin.getARENA_REGISTRY().stream()
                .filter(player -> player.getProperties().getName().equals(paramString)).findAny().orElse(null);
    }

    @Nullable
    public static Arena getRandomArena(UUID uuid) {
        List<Arena> arenas = MinerPlugin.getARENA_REGISTRY().stream()
                .filter(arena -> (arena.getStage().equals(Arena.Stage.WAITING)
                        || arena.getStage().equals(Arena.Stage.STARTING)))
                .filter(arena -> arena.canJoin(uuid)).sorted(Comparator.comparingInt(Arena::getRequiredPlayers))
                .collect(Collectors.toList());
        if (arenas.isEmpty())
            return null;
        int maxLoaded = arenas.get(0).getRequiredPlayers();
        arenas = arenas.stream().filter(arena -> arena.getRequiredPlayers() == maxLoaded).collect(Collectors.toList());
        return arenas.get(new Random().nextInt(arenas.size()));
    }

    public static int getOnline() {
        int online = 0;
        for (Arena arena : MinerPlugin.getARENA_REGISTRY()) {
            online += arena.getCurrentPlayers();
        }
        return online;
    }

    public static void startBungeeMode() {
        for (Player player : Bukkit.getOnlinePlayers())
            player.kickPlayer(PluginCommand.Language.BUNGEE_KICK.getString());
        MinerPlugin.getInstance().getLogger()
                .info("Bungee mode has been enabled. Detected " + MinerPlugin.getARENA_REGISTRY().size() + " arena" +
                        (MinerPlugin.getARENA_REGISTRY().size() > 1 ? "s." : "."));
        Bukkit.getMessenger().registerOutgoingPluginChannel(MinerPlugin.getInstance(), "BungeeCord");
    }

    private static String BUNGEE_SETTINGS = "Created world: %%__SB99__%%. Arena has been enabled.";

    public static String getBungeeSettings() {
        return BUNGEE_SETTINGS;
    }
}
