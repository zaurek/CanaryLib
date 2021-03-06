package net.canarymod.commandsys.commands;

import net.canarymod.Canary;
import net.canarymod.Colors;
import net.canarymod.MessageReceiver;
import net.canarymod.api.Server;
import net.canarymod.api.entity.Player;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.CommandException;

public class TeleportCommand extends CanaryCommand {

    public TeleportCommand() {
        super("canary.command.tp", "Teleport to a player", "Usage: /tp <player>", 2);
    }

    @Override
    protected void execute(MessageReceiver caller, String[] parameters) {
        if(caller instanceof Server) {
            console((Server)caller, parameters);
        }
        else if(caller instanceof Player) {
            player((Player)caller, parameters);
        }
        else {
            throw new CommandException("Unknown MessageReceiver: "+caller.getClass().getSimpleName());
        }
    }
    
    private void console(Server caller, String[] args) {
        caller.notify("The allmighty console may not visit a mortal!");
    }
    
    private void player(Player player, String[] args) {
       Player target = Canary.getServer().matchPlayer(args[1]);
       if(target != null) {
           player.teleportTo(target.getLocation());
           player.sendMessage(Colors.Yellow + "Teleported to "+target.getName());
       }
       else {
           player.notify(args[1] + " does not exist.");
       }
    }

}
