package me.rbrickis.nametahg;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Ryan on 10/21/2014
 * <p/>
 * Project: NameTahg
 */
// Because i can that's why.
public class WrappedNamedEntitySpawnPacket {

    Object packet;

    public WrappedNamedEntitySpawnPacket(Player player, WrappedGameProfile gameProfile) {
        try {
          this.packet = getPacketClass().getConstructor(getEntityHumanClass()).newInstance(ReflectionUtils.getHandle(player));

            ReflectionUtils.setValue(packet, "b", getGameProfileClass().getConstructor(UUID.class, String.class).newInstance(gameProfile.getUUID(), gameProfile.getName()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public Class<?> getPacketClass() {
        return ReflectionUtils.getCraftClass("PacketPlayOutNamedEntitySpawn");
    }

    public Class<?> getEntityHumanClass() {
        return ReflectionUtils.getCraftClass("EntityHuman");
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
