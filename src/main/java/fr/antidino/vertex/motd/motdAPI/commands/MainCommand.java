package fr.antidino.vertex.motd.motdAPI.commands;

import fr.antidino.vertex.motd.motdAPI.MotdAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements CommandExecutor , TabCompleter {
    MotdAPI motdAPI;

    public MainCommand(MotdAPI motdAPI) {
        this.motdAPI = motdAPI;
    }

    @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0){
            help(sender);
            return true;
        }
        switch(args[0].toLowerCase()) {
            case "reload":
                motdAPI.reload();
                sender.sendMessage(ChatColor.GREEN+"The plugin has been reload");
                break;
            case "help":
                help(sender);
                break;
            default:
                help(sender);



        }
         return true;
     }
    private void help(CommandSender sender) {
        sender.sendMessage(ChatColor.RED+"=============");
        sender.sendMessage(ChatColor.AQUA+"/motd reload | reload config");
        sender.sendMessage(ChatColor.AQUA+"/motd help  | help command");
        sender.sendMessage(ChatColor.RED+"=============");
    }
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> list = new ArrayList<String>();
        list.add("reload");
        list.add("help");
        return list;
    }
}
