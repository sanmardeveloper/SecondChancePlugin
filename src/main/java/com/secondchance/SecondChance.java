package com.secondchance;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SecondChance extends JavaPlugin implements Listener {

    private Scoreboard main;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Plugin 'Second Chance' is enable!");

        main = getServer().getScoreboardManager().getMainScoreboard();

        Team team = main.getTeam("alive");
        if (team == null) {
            main.registerNewTeam("alive");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin 'Second Chance' is disable!");
    }


    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getHealth() - event.getFinalDamage() <= 0) {
                Team team = player.getScoreboard().getEntryTeam(player.getName());
                if (getConfig().getBoolean("secondChance")
                        && team != null
                        && team.getName().equalsIgnoreCase("alive")) {
                    event.setCancelled(true);
                    player.setHealth(1);
                    Objects.requireNonNull(main.getTeam("alive")).removePlayer(player);
                    player.sendActionBar(Objects.requireNonNull(getConfig().getString("secondChanceMessage")));
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
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
        } else if (command.getName().equalsIgnoreCase("alive")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by a player!");
                return true;
            }
            if (args.length == 0) {
                Objects.requireNonNull(main.getTeam("alive")).addPlayer((Player) sender);
                sender.sendMessage("You are alive!");
            } else if (args.length == 1) {
                try {
                    Objects.requireNonNull(main.getTeam("alive")).addPlayer(getServer().getPlayer(args[0]));
                    sender.sendMessage(getServer().getPlayer(args[0]).getName() + " is alive!");
                } catch (IllegalArgumentException e) {
                    getLogger().warning(String.valueOf(e));
                }
            } else {
                sender.sendMessage("Usage: /alive [player]");
            }

            return true;
        }
        return false;
    }

}
