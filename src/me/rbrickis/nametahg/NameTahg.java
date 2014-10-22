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

            for(Player players : Bukkit.getOnlinePlayers()) {
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

        for(Player players : Bukkit.getOnlinePlayers()) {
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


}
