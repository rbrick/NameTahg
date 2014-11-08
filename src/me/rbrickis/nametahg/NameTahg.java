package me.rbrickis.nametahg;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ryan on 10/21/2014
 * <p/>
 * Project: NameTahg
 */
public class NameTahg {

    // Credits to @sk89q.
    private static final Pattern DASHLESS_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");

    private ProtocolManager protocolManager;

    private PacketType TYPE = PacketType.Play.Server.NAMED_ENTITY_SPAWN;

    private HashMap<String, Integer> tags = new HashMap<String, Integer>();


    public void setup() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }



    public void setTag(Player player, String name) {
        try {

            for(Player players : getOnlinePlayers()) {
               if(!players.equals(player))
                ReflectionUtils.sendPacket(players, new WrappedNamedEntitySpawnPacket(player, new WrappedGameProfile(UUID.randomUUID(), name)).getPacket());
            }
            tags.put(player.getName(), player.getEntityId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void unsetTag(Player player) {
        if(!tags.containsKey(player.getName())) {
            return;
        }
         PacketContainer container = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);

         container.getIntegerArrays().write(0, new int[]{tags.get(player.getName())});

        // We could simply use broadcastPacket but we do not want to send the packet to the disguised player

        for(Player players : getOnlinePlayers()) {
           try {
               if(!players.equals(player)) {
                   protocolManager.sendServerPacket(players, container);
                   ReflectionUtils.sendPacket(players, new WrappedNamedEntitySpawnPacket(player, new WrappedGameProfile(player.getUniqueId(), player.getName())).getPacket());
               }
           } catch (Exception ex) {
               ex.printStackTrace();
           }
        }

        tags.remove(player.getName());
    }

    public String getUUID(String name) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + ChatColor.stripColor(name)).openConnection();

            connection.setDoOutput(true);

            connection.setRequestMethod("GET");

            Scanner scanner = new Scanner(connection.getInputStream());

            JSONParser parser = new JSONParser();

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());

            JSONObject object = (JSONObject) parser.parse(reader);

            if(object.get("error") != null) {
                return UUID.randomUUID().toString();
            }
            return addDashes((String) object.get("id"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return UUID.randomUUID().toString();
    }

    // Again, credits to @sk89q
    public static String addDashes(String uuid) {
        uuid = uuid.replace("-", ""); // Remove dashes
        Matcher matcher = DASHLESS_PATTERN.matcher(uuid);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid UUID format");
        }
        return matcher.replaceAll("$1-$2-$3-$4-$5");
    }

    /**
     * Gets a list of players currently online
     * <p>
     * This implementation includes a workaround for {@link org.bukkit.Bukkit#getOnlinePlayers()} returning an array in
     * older releases of CraftBukkit, instead of a Collection in more recent releases. Essentially, this adds backwards
     * compatibility with older versions of CraftBukkit without having to adjust much in your plugin.
     * <p>
     * It's ugly, but it works and provides backwards compatibility
     *
     * @return a list of all online players
     *
     * @author DSH105
     * Credits to DSH105, taken from his plugin Commodus. I probably just could of done this myself by i am lazy....plus that documentation is OP!
     */
    public static List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();
        try {
            Method onlinePlayersMethod = Bukkit.class.getMethod("getOnlinePlayers");
            if (onlinePlayersMethod.getReturnType().equals(Collection.class)) {
                Collection<Player> playerCollection = (Collection<Player>) onlinePlayersMethod.invoke(null, new Object[0]);
                if (playerCollection instanceof List) {
                    onlinePlayers = (List<Player>) playerCollection;
                } else {
                    onlinePlayers = new ArrayList<>(playerCollection);
                }
            } else {
                onlinePlayers = Arrays.asList((Player[]) onlinePlayersMethod.invoke(null, new Object[0]));
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
        }
        return onlinePlayers;
    }


    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
