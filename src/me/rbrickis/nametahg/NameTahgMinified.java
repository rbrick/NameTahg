package me.rbrickis.nametahg;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Ryan on 11/2/2014
 * <p/>
 * Project: NameTahg
 *
 * About: This is a minified version of NameTahg. This contains everything needed in one class.
 * You can then shade this class into your plugin for any of your needs with ease.
 * Enjoy, rbrick :)
 */
public class NameTahgMinified {
   private static class WrappedNamedEntitySpawnPacket {
        Object packet;


        public WrappedNamedEntitySpawnPacket(Player player, WrappedGameProfile gameProfile) {
            try {
                this.packet = getPacketClass().getConstructor(getEntityHumanClass()).newInstance(getHandle(player));

                setValue(packet, "b", getGameProfileClass().getConstructor(UUID.class, String.class).newInstance(gameProfile.getUUID(), gameProfile.getName()));

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        public Class<?> getPacketClass() {
            return getMinecraftClass("PacketPlayOutNamedEntitySpawn");
        }

        public Class<?> getEntityHumanClass() {
            return getMinecraftClass("EntityHuman");
        }

        public Class<?> getGameProfileClass() {
            try {
                return Class.forName("net.minecraft.util.com.mojang.authlib.GameProfile");
            } catch (Exception ex) {
                return null;
            }
        }

        public Object getPacket() {
            return packet;
        }
    }

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
                   sendPacket(players, new WrappedNamedEntitySpawnPacket(player, new WrappedGameProfile(UUID.randomUUID(), name)).getPacket());
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
                    sendPacket(players, new WrappedNamedEntitySpawnPacket(player, new WrappedGameProfile(player.getUniqueId(), player.getName())).getPacket());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        tags.remove(player.getName());
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

    public static void sendPacket(Player p, Object packet) {
        try {
            Object nmsPlayer = getHandle(p);
            Field con_field = nmsPlayer.getClass().getField("playerConnection");
            Object con = con_field.get(nmsPlayer);
            Method packet_method = getMethod(con.getClass(), "sendPacket");
            packet_method.invoke(con, packet);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static Class<?> getCraftClass(String ClassName) {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        String className = "net.minecraft.server." + version + ClassName;
        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }

    private static Object getHandle(Entity entity) {
        Object nms_entity = null;
        Method entity_getHandle = getMethod(entity.getClass(), "getHandle");
        try {
            nms_entity = entity_getHandle.invoke(entity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return nms_entity;
    }

    private static Object getHandle(Object entity) {
        Object nms_entity = null;
        Method entity_getHandle = getMethod(entity.getClass(), "getHandle");
        try {
            nms_entity = entity_getHandle.invoke(entity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return nms_entity;
    }

    public static Method getMethod(Class<?> cl, String method) {
        for (Method m : cl.getMethods()) {
            if (m.getName().equals(method)) {
                return m;
            }
        }
        return null;
    }

    private static String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static String NMS = "net.minecraft.server";

    private static String OBC = "org.bukkit.craftbukkit";



    public static Class<?> getCraftBukkitClass(String className) {
        try {
            return Class.forName(OBC + "."  + VERSION + "." + className);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Class<?> getMinecraftClass(String className) {
        try {
            return Class.forName(NMS + "." + VERSION + "." + className);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void setValue(Object instance, String fieldName, Object value)
            throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
