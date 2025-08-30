package com.secondchance;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SecondChance extends JavaPlugin implements Listener {

    private Scoreboard main;

    @Override
    public void onEnable() {
        //Refister Listener
        getServer().getPluginManager().registerEvents(this, this);

        //Log for start plugin
        getLogger().info("Plugin 'Second Chance' is enable!");

        //Variable for scoreboard manager
        main = getServer().getScoreboardManager().getMainScoreboard();

        //Make sure that team exist
        Team team = main.getTeam("secondChance");
        if (team == null) {
            main.registerNewTeam("secondChance");
        }
    }

    @Override
    public void onDisable() {
        //Log for disable plugin
        getLogger().info("Plugin 'Second Chance' is disable!");
    }


    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getHealth() - event.getFinalDamage() <= 0) {
                Team team = player.getScoreboard().getEntryTeam(player.getName());

                //Main Function
                if (getConfig().getBoolean("secondChance")
                        && team != null
                        && team.getName().equalsIgnoreCase("secondChance")) {
                    event.setCancelled(true);
                    player.setHealth(1);
                    player.playSound(
                            player.getLocation(), // где слышно звук
                            Sound.ITEM_TOTEM_USE, // тип звука
                            1.3f, // громкость
                            0.5f  // высота тона (pitch)
                    );

                    //Remove Player from Team
                    team.removePlayer(player);
                    String a = String.valueOf('"');

                    //Effects
                    player.setAbsorptionAmount(4);
                    PotionEffect fireResistence = new PotionEffect(PotionType.FIRE_RESISTANCE.getEffectType(), 10, 0, false, false);
                    player.addPotionEffect(fireResistence);

                    //Send a Message
                    player.sendActionBar(Objects.requireNonNull(getConfig().getString(("secondChanceMessage").replaceAll(a, ""))));
                }
            }
        }
    }



    @EventHandler
    public void OnPlayerRMB(PlayerInteractEvent event) {
        //Check main hand
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            //Chack team
            if (!main.getTeam("secondChance").hasPlayer(event.getPlayer())) {
                Objects.requireNonNull(main.getTeam("secondChance")).addPlayer(event.getPlayer());
                event.getPlayer().getInventory().setItemInMainHand(null);
                event.getPlayer().sendActionBar("You've just given yourself a second chance!");
            }
        }
    }

    @EventHandler
    public void OnRespawn(PlayerRespawnEvent event) {
        //Add player to team on Respawn
        Player player = event.getPlayer();
        main.getTeam("secondChance").addPlayer(player);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        //Commands
        if (command.getName().equalsIgnoreCase("SecondChance")) {
            if (args.length == 0) {
                sender.sendMessage("Second Chance value : " + getConfig().getBoolean("secondChance"));
                return true;
            }
            if (args[0].equalsIgnoreCase("false")) {
                getConfig().set("secondChance", false);
                saveConfig();
                return true;
            } else if (args[0].equalsIgnoreCase("true")) {
                getConfig().set("secondChance", true);
                saveConfig();
                return true;
            } else {
                sender.sendMessage("Usage: /setSecondChance [<false|true>]");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("SecondChanceMessage")) {
            if (args.length == 0) {
                sender.sendMessage("SecondChance Message: " + getConfig().getString("secondChanceMessage"));
            } else {
                String arg1 = String.join(" ", args);
                getConfig().set("secondChanceMessage", arg1);
                saveConfig();
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("givesecondchance")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }
            if (args.length == 0) {
                Objects.requireNonNull(main.getTeam("secondChance")).addPlayer((Player) sender);
                sender.sendMessage("You've just given yourself a second chance!");
            } else if (args.length == 1) {
                try {
                    Objects.requireNonNull(main.getTeam("secondChance")).addPlayer(getServer().getPlayer(args[0]));
                    sender.sendMessage(getServer().getPlayer(args[0]).getName() + " is alive!");
                } catch (IllegalArgumentException e) {
                    getLogger().warning(String.valueOf(e));
                }
            } else {
                sender.sendMessage("Usage: /givesecondchance [player]");
            }

            return true;
        }
        return false;
    }

}
