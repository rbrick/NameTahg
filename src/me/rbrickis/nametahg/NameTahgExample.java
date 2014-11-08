package me.rbrickis.nametahg;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.rbrick.zeus.annotations.Command;
import me.rbrick.zeus.registers.Registrar;
import me.rbrick.zeus.registers.bukkit.BukkitRegistrar;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by Ryan on 10/21/2014
 * <p/>
 * Project: NameTahg
 */
public class NameTahgExample extends JavaPlugin {

    private Registrar registrar;

    private NameTahg api;

    @Override
    public void onEnable() {
        super.onEnable();

        registrar = new BukkitRegistrar();

        registrar.registerAll(this.getClass(), this);

        this.api = new NameTahg();

        // Sets up protocol lib
        this.api.setup();

    }

    @Command(name = "disguise", aliases = "d", minArgs = 1, maxArgs = 1, usage = "/<command> <Player>")
    public void disguise(Player player, String[] args) {
        api.setTag(player, ChatColor.translateAlternateColorCodes('&', args[0]));
    }

    @Command(name="undisguise", aliases = "und", usage = "/<command> <Player>")
    public void undisguise(Player player, String[] args) {
        api.unsetTag(player);
    }



}
