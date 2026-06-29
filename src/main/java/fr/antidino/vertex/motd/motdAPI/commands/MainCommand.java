package fr.antidino.vertex.motd.motdAPI.commands;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor , TabCompleter {
    MotdAPI motdAPI;

    public MainCommand(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pingflow.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission for this command.");
            return true;
        }

        if (args.length == 0) {
            help(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                motdAPI.reload();
                sender.sendMessage(ChatColor.GREEN + "[PingFlow] Plugin reload.");
            }

            case "ip" -> {
                if (args.length < 2) { help(sender); return true; }
                switch (args[1].toLowerCase()) {
                    case "set" -> {
                        // /pingFlow ip set <player|ip> <icon> <motd...>
                        if (args.length < 5) {
                            sender.sendMessage(ChatColor.RED + "Usage: /pingflow ip set <player|ip> <icon> <motd>");
                            return true;
                        }

                        // Résolution IP — par nom de joueur ou IP directe
                        String ip;
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target != null) {
                            ip = target.getAddress().getAddress().getHostAddress();
                        } else {
                            // Pas un joueur connecté, on suppose que c'est une IP directe
                            ip = args[2];
                        }
                        String icon = args[3];
                        String motd = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                        motdAPI.ipDatabaseManager.set(ip, motd, icon);
                        sender.sendMessage(ChatColor.GREEN + "[Pingflow] IP " + ChatColor.AQUA + ip + ChatColor.GREEN + " configurée.");
                        sender.sendMessage(ChatColor.GRAY + "Motd : " + ChatColor.WHITE + motd);
                        sender.sendMessage(ChatColor.GRAY + "Icon : " + ChatColor.WHITE + icon);
                    }
                    case "remove" -> {
                        // /pingflow ip remove <player|ip>
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "Usage: /pingflow ip remove <player|ip>");
                            return true;
                        }
                        String ip;
                        Player target = Bukkit.getPlayer(args[2]);
                        ip = target != null
                                ? target.getAddress().getAddress().getHostAddress()
                                : args[2];

                        motdAPI.ipDatabaseManager.remove(ip);
                        sender.sendMessage(ChatColor.GREEN + "[Pingflow] IP " + ChatColor.AQUA + ip + ChatColor.GREEN + " supprimée.");
                    }

                    case "get" -> {
                        // /pingflow ip get <player|ip>
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "Usage: /pingflow ip get <player|ip>");
                            return true;
                        }
                        String ip;
                        Player target = Bukkit.getPlayer(args[2]);
                        ip = target != null
                                ? target.getAddress().getAddress().getHostAddress()
                                : args[2];

                        motdAPI.ipDatabaseManager.get(ip).ifPresentOrElse(
                                entry -> {
                                    sender.sendMessage(ChatColor.GREEN + "[PingFlow] Entry for " + ChatColor.AQUA + ip);
                                    sender.sendMessage(ChatColor.GRAY + "Motd : " + ChatColor.WHITE + entry.motd());
                                    sender.sendMessage(ChatColor.GRAY + "Icon : " + ChatColor.WHITE + entry.icon());
                                },
                                () -> sender.sendMessage(ChatColor.RED + "[PingFlow] No entries for " + ip)
                        );
                    }

                    default -> help(sender);
                }
            }
            case "motd" -> {

                switch (args[1].toLowerCase()) {
                    case "get" -> {
                        if (args.length < 3){
                            sender.sendMessage(ChatColor.RED + "/motd motd get <name>");
                            return true;
                        }
                        String name = args[2];
                        String motd = motdAPI.ipDatabaseManager.getMotd(name);
                        sender.sendMessage(ChatColor.GREEN+"[PingFlow] Name : "+name+ " | Motd :"+ChatColor.translateAlternateColorCodes('&',motd));
                    }
                    case "set" -> {
                        // /motd motd add <name> <motd...>
                        if (args.length < 4) {
                            sender.sendMessage(ChatColor.RED + "Usage: /motd motd set <name> <motd...>");
                            return true;
                        }
                        String name = args[2];
                        String motd = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                        motdAPI.ipDatabaseManager.setMotd(name, motd);
                        sender.sendMessage(ChatColor.GREEN + "[PingFlow] Motd " + ChatColor.AQUA + name + ChatColor.GREEN + " registered: " + ChatColor.WHITE + motd);
                    }
                    case "remove" -> {
                        // /motd motd remove <name>
                        if (args.length != 3) {
                            sender.sendMessage(ChatColor.RED + "Usage: /motd motd remove <name>");
                            return true;
                        }
                        motdAPI.ipDatabaseManager.removeMotd(args[2]);
                        sender.sendMessage(ChatColor.GREEN + "[PingFlow] Motd " + ChatColor.AQUA + args[2] + ChatColor.GREEN + " removed.");
                    }
                    case "list" -> {
                        List<String> names = motdAPI.ipDatabaseManager.getMotdNames();
                        if (names.isEmpty()) {
                            sender.sendMessage(ChatColor.RED + "[PingFlow] No motd registered.");
                            return true;
                        }
                        sender.sendMessage(ChatColor.AQUA + "[PingFlow] Registered motds:");
                        names.forEach(n -> sender.sendMessage(ChatColor.GRAY + "  - " + n));
                    }
                    default -> help(sender);
                }
            }
            default -> help(sender);
        }
        return true;
    }


    private void help(CommandSender sender) {
        sender.sendMessage(ChatColor.RED+"=============");
        sender.sendMessage(ChatColor.AQUA+"/pingflow reload | reload config");
        sender.sendMessage(ChatColor.AQUA+"/pingflow help  | help command");
        sender.sendMessage(ChatColor.RED+"=============");
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        // /pingFlow <arg1>
        if (args.length == 1) {
            return filter(List.of("reload", "ip", "help","motd"), args[0]);
        }
        if (args[0].equalsIgnoreCase("motd")) {
            if (args.length == 2){
                return filter(List.of("set","get","remove","list"),args[1]);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("get")) {
                return motdAPI.ipDatabaseManager.getMotdNames();
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
                return motdAPI.ipDatabaseManager.getMotdNames();
            }
        }
        // /pingFlow ip <arg2>
        if (args.length == 2 && args[0].equalsIgnoreCase("ip")) {
            return filter(List.of("set", "remove", "get"), args[1]);
        }
        // /pingflow ip <set|remove|get> <arg3> → joueurs en ligne
        if (args.length == 3 && args[0].equalsIgnoreCase("ip")) {
            List<String> players = Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            players.add("<ip>"); // suggestion manuelle
            return filter(players, args[2]);
        }

        // /pingflow ip set <player> <arg4> → icône
        if (args.length == 4 && args[0].equalsIgnoreCase("ip") && args[1].equalsIgnoreCase("set")) {
            File iconFolder = new File(motdAPI.getDataFolder(), "icons");
            if (!iconFolder.exists()) return List.of();

            List<String> icons = Arrays.stream(iconFolder.listFiles())
                    .filter(f -> f.getName().endsWith(".png") || f.getName().endsWith(".jpg"))
                    .map(f -> "icons/" + f.getName())
                    .collect(Collectors.toList());

            return filter(icons, args[3]);
        }

        // /pingflow ip set <player> <icon> <arg5> → motd
        if (args.length == 5 && args[0].equalsIgnoreCase("ip") && args[1].equalsIgnoreCase("set")) {
            List<String> motds = motdAPI.ipDatabaseManager.getMotdNames();
            motds.add("<motd>");
            return filter(motds, args[4]);
        }

        return List.of();
    }

    // Filtre les suggestions selon ce que l'utilisateur a déjà tapé
    private List<String> filter(List<String> options, String current) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }
}
