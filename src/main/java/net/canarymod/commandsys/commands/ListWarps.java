package net.canarymod.commandsys.commands;

import java.util.List;

import net.canarymod.Canary;
import net.canarymod.Colors;
import net.canarymod.Logman;
import net.canarymod.MessageReceiver;
import net.canarymod.api.Server;
import net.canarymod.api.entity.Player;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.commandsys.CommandException;
import net.canarymod.warp.Warp;

public class ListWarps extends CanaryCommand {

    public ListWarps() {
        super("canary.command.listwarps", "Get a list of available warps", "Usage: /listwarps", 1);
    }
    
    @Override
    protected void execute(MessageReceiver caller, String[] parameters) {
        if(caller instanceof Server) {
            console((Server)caller);
        }
        else if(caller instanceof Player) {
            player((Player)caller);
        }
        else {
            throw new CommandException("Unknown MessageReceiver: "+caller.getClass().getSimpleName());
        }
    }
    
    private void console(Server caller) {
        caller.notify("**** WARPS ****");
        
        List<Warp> warps = Canary.warps().getAllWarps();
        StringBuilder warpList = new StringBuilder();
        
        for(Warp warp : warps) {
            warpList.append(warp.getName()).append(",");
        }
        if(warpList.length() > 0) {
            Logman.logInfo(warpList.toString());
        }
        else {
            Logman.logInfo("No warps loaded.");
        }
    }
    
    private void player(Player player) {
        player.sendMessage(Colors.Yellow+"Available Warps: ");
        
        List<Warp> warps = Canary.warps().getAllWarps();
        StringBuilder warpList = new StringBuilder();
        
        for(Warp w : warps) {
            if(w.getOwner() != null) {
                if(w.isPlayerHome() && w.getOwner().equals(player.getName())) {
                    warpList.append(Colors.LightGreen).append("Your Home").append(Colors.White).append(",");
                }
                else if(!w.isPlayerHome() && w.getOwner().equals(player.getName()) || (player.isAdmin() || player.hasPermission("canary.command.warp.admin"))) {
                    warpList.append(Colors.Gold).append(w.getName()).append("(private)").append(Colors.White).append(",");
                }
            }
            else if(w.isGroupRestricted() && w.isGroupAllowed(player.getGroup())) {
                warpList.append(Colors.Yellow).append(w.getName()).append("(group)").append(Colors.White).append(",");
            }
            else if(!w.isGroupRestricted()) {
                warpList.append(w.getName()).append(",");
            }
        }
        
        if(warpList.length() > 0) {
            player.sendMessage(warpList.toString());
        }
        else {
            player.notify("No warps loaded");
        }
        
    }

}
