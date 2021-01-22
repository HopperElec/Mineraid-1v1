package uk.co.hopperelec.mc.mineraid1v1;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Mineraid1v1 extends JavaPlugin implements Listener {
    Location waitingArea;
    Location spawn1;
    Location spawn2;
    Player player1;
    Player player2;
    boolean lockPlayers = true;
    final PotionEffect goldenHeadEffect = new PotionEffect(PotionEffectType.REGENERATION,7,4);
    final ArrayList<Block> playerBlocks = new ArrayList<>();
    FileConfiguration config;

    final ItemStack[] kit = {
            new ItemStack(Material.DIAMOND_SWORD),
            new ItemStack(Material.FISHING_ROD),
            new ItemStack(Material.BOW),
            new ItemStack(Material.WOOD,64),
            new ItemStack(Material.LAVA_BUCKET,1),
            new ItemStack(Material.WATER_BUCKET,1),
            new ItemStack(Material.GOLDEN_APPLE,6),
            new ItemStack(Material.SKULL_ITEM,3),
            new ItemStack(Material.COOKED_BEEF,64),
            new ItemStack(Material.DIAMOND_AXE),
            new ItemStack(Material.LAVA_BUCKET,1),
            new ItemStack(Material.WATER_BUCKET,1),
            new ItemStack(Material.WOOD,64),
            new ItemStack(Material.ARROW,64)
    };

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();

        final World world = Bukkit.getWorlds().get(0);
        waitingArea = getLocation(world,"waitingArea");
        spawn1 = getLocation(world,"player1");
        spawn2 = getLocation(world,"player2");

        kit[2].addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        final SkullMeta skullMeta = (SkullMeta) kit[7].getItemMeta();
        skullMeta.setOwner("LegendaryJulien");
        kit[7].setItemMeta(skullMeta);

        getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    public Location getLocation(World world, String name) {
        return new Location(world,config.getInt(name+"X"),config.getInt(name+"Y"),config.getInt(name+"Z"),config.getInt(name+"Pitch"),config.getInt(name+"Yaw"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().clear();
        if (Bukkit.getOnlinePlayers().size() == 1) {
            player1 = event.getPlayer();
            player1.teleport(waitingArea);
            player1.sendMessage("Waiting for another player to go against. Please wait.");
        } else {
            player2 = event.getPlayer();
            player1.teleport(spawn1);
            player2.teleport(spawn2);
            for (Player player: Bukkit.getOnlinePlayers()) {
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                player.getInventory().addItem(kit);
            }
            lockPlayers = false;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (lockPlayers) {
            event.getPlayer().teleport(waitingArea);
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getType().equals(Material.SKULL)) {
            event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()-1);
            event.getPlayer().addPotionEffect(goldenHeadEffect);
        }
    }

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        playerBlocks.add(event.getBlock());
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        if (playerBlocks.contains(event.getBlock())) {
            playerBlocks.remove(event.getBlock());
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) throws IOException {
        final Player loser = event.getEntity();
        Player winner;
        if (player1 == loser) {winner = player2;
        } else {winner = player1;}
        loser.setGameMode(GameMode.SPECTATOR);
        for (Player player: Bukkit.getOnlinePlayers()) {player.sendMessage(winner.getDisplayName()+" has won!");}

        for (Block block : playerBlocks) {
            block.setType(Material.AIR);
        }
        playerBlocks.clear();

        for (Player player: Bukkit.getOnlinePlayers()) {
            ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(arrayOut);
            dataOut.writeUTF("Connect"); dataOut.writeUTF("MainLobby");
            player.sendPluginMessage(this, "BungeeCord", arrayOut.toByteArray());
        }
    }
}
