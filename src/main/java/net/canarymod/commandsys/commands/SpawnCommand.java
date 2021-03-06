package net.canarymod.commandsys.commands;

import net.canarymod.Canary;
import net.canarymod.Colors;
import net.canarymod.MessageReceiver;
import net.canarymod.api.Server;
import net.canarymod.api.entity.Player;
import net.canarymod.api.world.World;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.CommandException;

public class SpawnCommand extends CanaryCommand {

    public SpawnCommand() {
        super("canary.command.spawn", "Go to a worlds spawn location", "Usage: /spawn [world name] [other player]", 1, 3);
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
        if(args.length < 3) {
            caller.notify("Please specify a world and a player");
        }
        else {
            Player player = caller.matchPlayer(args[2]);
            World w = caller.getWorld(args[1]);
            if(player != null && w != null) {
                player.teleportTo(w.getNormal().getSpawnLocation());
                caller.notify(player.getName()+" has been teleported to the specified spawn");
            }
            else {
                caller.notify("World or player does not exist!");
            }
        }
        
    }

    private void player(Player player, String[] args) {
        if(args.length == 1) {
            player.teleportTo(player.getDimension().getSpawnLocation());
        }
        else if(args.length == 2){
            World w = Canary.getServer().getWorld(args[1]);
            if(w == null) {
                player.notify(args[1] + " is not a valid world");
            }
            else {
                player.teleportTo(w.getNormal().getSpawnLocation());
            }
        }
        else {
            World w = Canary.getServer().getWorld(args[1]);
            Player target = Canary.getServer().matchPlayer(args[2]);
            if(target != null && w != null) {
                target.teleportTo(w.getNormal().getSpawnLocation());
                player.sendMessage(Colors.Yellow + target.getName() + " has been teleported to "+w.getName());
            }
        }
    }

}
