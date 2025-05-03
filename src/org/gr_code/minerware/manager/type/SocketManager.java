package org.gr_code.minerware.manager.type;

import com.google.gson.Gson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.arena.Arena;
import org.gr_code.minerware.arena.Properties;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

public class SocketManager {

    private static final MinerPlugin minerPlugin = MinerPlugin.getInstance();

    protected static FileConfiguration fileConfiguration = minerPlugin.getBungee();

    private static DataInputStream dataInputStream;

    private static PrintWriter printWriter;

    private static boolean enabled = false;

    private static String host;

    private static int port;

    private static Socket socket;

    private static final String server = fileConfiguration.getString("server");

    public static void load() {
        new BukkitRunnable() {
            public void run() {
                enabled = fileConfiguration.getBoolean("socket.enabled") && !SetupManager.notBungeeMode();
                if (enabled) {
                    host = fileConfiguration.getString("socket.ip");
                    port = fileConfiguration.getInt("socket.port");
                    try {
                        socket = new Socket(host, port);
                        dataInputStream = new DataInputStream(socket.getInputStream());
                        printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        System.out.println("[MinerWare] Successfully enabled proxy-client!");
                    } catch (IOException e) {
                        System.out.println("[MinerWare] An error occurred while enabling the socket!");
                        enabled = false;
                    }
                }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    public static void disable(){
        if(!enabled)
            return;
        enabled = false;
        try {
            dataInputStream.close();
            printWriter.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("[MinerWare] An error occurred while disabling the socket!");
        }
    }

    public static void sendMessage(String paramString) {
        if(!enabled)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    printWriter.println(paramString);
                } catch (Exception e){
                    System.out.println("[MinerWare] An error occurred while sending message!");
                }
            }
        }.runTaskAsynchronously(minerPlugin);
    }

    public static void sendArenaUpdate(Arena arena) {
        Gson gson = new Gson();
        Properties properties = arena.getProperties();
        Map<String, String> map = new Hashtable<>();
        map.put("Server", server);
        map.put("ID", arena.getName());
        map.put("Stage", arena.getStage().name());
        map.put("Players", arena.getCurrentPlayers()+"");
        map.put("MaximumPlayers", properties.getMaxPlayers()+"");
        String string = gson.toJson(map);
        sendMessage(string);
    }

    public static String decode(String uuid, String action, String name){
        return action+":"+server+":"+name+":"+uuid;
    }

}
