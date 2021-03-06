package net.canarymod.commandsys.commands;

import net.canarymod.Canary;
import net.canarymod.MessageReceiver;
import net.canarymod.api.Server;
import net.canarymod.api.entity.Player;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.CommandException;

public class Mute extends CanaryCommand {

    public Mute() {
        super("canary.command.mute", "Mute other players", "Usage: /mute <player>", 2);
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
        Player target = caller.matchPlayer(args[1]);
        if(target != null) {
            if(target.isMuted()) {
                target.setMuted(false);
                caller.notify(target.getName()+" has been unmuted.");
            }
            else {
                target.setMuted(true);
                caller.notify(target.getName()+" has been muted.");
            }
        }
        else {
            caller.notify(args[2]+" does not exist!");
        }
    }
    
    private void player(Player player, String[] args) {
        Player target = Canary.getServer().matchPlayer(args[1]);
        if(target != null) {
            if(target.isMuted()) {
                target.setMuted(false);
                player.notify(target.getName()+" has been unmuted.");
            }
            else {
                target.setMuted(true);
                player.notify(target.getName()+" has been muted.");
            }
        }
        else {
            player.notify(args[2]+" does not exist!");
        }
    }

}
