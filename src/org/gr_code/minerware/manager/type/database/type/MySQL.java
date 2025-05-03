package org.gr_code.minerware.manager.type.database.type;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.gr_code.minerware.MinerPlugin;
import org.gr_code.minerware.manager.type.database.cached.Cached;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("CatchMayIgnoreException")
public class MySQL {

    private static Connection connection;

    private static boolean enabled;

    private static final FileConfiguration fileConfiguration = MinerPlugin.getInstance().getOptions();

    protected static String user;

    protected static String password;

    protected static String database;

    protected static String host;

    protected static String table;

    public static Path parsePath(String string){
        try {
            return Path.valueOf(string);
        }catch (Exception e){
            return null;
        }
    }

    public enum Path {
        WINS, MAX_POINTS, GAMES_PLAYED, LEVEL, EXP
    }

    public static void initialize(boolean enable) {
        MySQL.enabled = enable;
        if (enable)
            startConnection();
    }

    private static void createData(UUID uuid, String name) {
        if (!enabled)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
                    preparedStatement.setString(1, uuid.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    PreparedStatement insertInto = getConnection().prepareStatement("INSERT INTO " +
                            table + " (UUID,NAME,WINS,MAX_POINTS,GAMES_PLAYED,LEVEL,EXP) VALUES (?,?,?,?,?,?,?)");
                    insertInto.setString(1, uuid.toString());
                    insertInto.setString(2, name);
                    insertInto.setInt(3, 0);
                    insertInto.setInt(4, 0);
                    insertInto.setInt(5, 0);
                    insertInto.setInt(6, 0);
                    insertInto.setInt(7, 0);
                    insertInto.executeUpdate();
                } catch (SQLException ignored) { }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    public static void set(UUID uuid, Path path, int paramInt) {
        if (!enabled)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = getConnection().
                            prepareStatement("UPDATE " + table + " SET <path>=? WHERE UUID=?".replace("<path>", path.toString()));
                    preparedStatement.setString(2, uuid.toString());
                    preparedStatement.setInt(1, paramInt);
                    preparedStatement.executeUpdate();
                } catch (SQLException ignored) { }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    public static void get(UUID uuid, CallBack<Cached, ResultSet> callback, Cached cached) {
        if (!enabled) {
            callback.onError(cached);
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
                    preparedStatement.setString(1, uuid.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if(resultSet.next())
                        callback.onSuccess(cached, resultSet);
                    else {
                        callback.onError(cached);
                        createData(uuid, Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName());
                    }
                } catch (SQLException e) { }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    private static void startConnection() {
        user = fileConfiguration.getString("database.user");
        password = fileConfiguration.getString("database.password");
        database = fileConfiguration.getString("database.database");
        host = fileConfiguration.getString("database.host");
        int port = fileConfiguration.getInt("database.port");
        table = fileConfiguration.getString("database.table");
        try {
            synchronized (MinerPlugin.getInstance()) {
                if (connection != null && !connection.isClosed())
                    return;
                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false", user, password));
                Statement statement = connection.createStatement();
                String a = "CREATE TABLE IF NOT EXISTS <table> " +
                        "(`UUID` TEXT, `NAME` TEXT, `WINS` INT, `MAX_POINTS` INT, `GAMES_PLAYED` INT, `LEVEL` INT, `EXP` INT);";
                statement.executeUpdate(a.replace("<table>", table));
                System.out.print("[MinerWare] MySQL enabled successful!");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("[MinerWare] MySQL wasn't enabled due to the unexpected error.");
            enabled = false;
        }
    }

    private static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed())
                refresh();
        } catch (SQLException throwable) {
            refresh();
        }
        return connection;
    }

    private static void setConnection(Connection connection) {
        MySQL.connection = connection;
    }

    public static void start() {
        MySQL.initialize(MinerPlugin.getInstance().getOptions().getBoolean("database.enabled"));
    }

    private static void refresh() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + fileConfiguration.getInt("database.port") + "/" +
                    database + "?autoReconnect=true&useSSL=false", user, password));
        } catch (ClassNotFoundException | SQLException ignored) {
            //do nothing
        }
    }

    public static void loadLeaderboards(CallBack<String[][], ResultSet[]> callBack, String[][] strings) {
        if (!enabled) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                ResultSet[] resultSets = new ResultSet[Path.values().length];
                try {
                    int i = 0;
                    for (Path path : Path.values()) {
                        PreparedStatement preparedStatement = getConnection()
                                .prepareStatement("SELECT * FROM " + table + " ORDER BY " + path.name() + " DESC LIMIT 10");
                        ResultSet resultSet = preparedStatement.executeQuery();
                        resultSets[i] = resultSet;
                        i++;
                    }
                    callBack.onSuccess(strings, resultSets);
                } catch (SQLException exception) {
                }
            }
        }.runTaskAsynchronously(MinerPlugin.getInstance());
    }

    public interface CallBack<V, T>{

        void onSuccess(V object, T manager);

        default void onError(V object) {}

    }

}
