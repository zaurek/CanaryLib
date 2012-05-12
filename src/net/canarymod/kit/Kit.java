package net.canarymod.kit;

import java.util.ArrayList;
import java.util.HashMap;

import net.canarymod.ICanary;
import net.canarymod.api.entity.IPlayer;
import net.canarymod.api.inventory.IItem;
import net.canarymod.group.Group;

public class Kit {
    /**
     * Time between uses as unix timestamp applicable number
     */
    private int delay;
    
    
    /**
     * Owner if applicable
     */
    private String[] owners;
    
    /**
     * Groups if applicable
     */
    private Group[] groups;
    
    /**
     * List of last usages per player
     */
    private HashMap<String, Long> lastUsages = new HashMap<String, Long>(); 
    
    private String name;
    /**
     * The content of this kit as IItems
     * Each list entry shall be a different Item
     */
    private ArrayList<IItem> content;

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String[] getOwner() {
        return owners;
    }

    public void setOwner(String[] owner) {
        this.owners = owner;
    }

    public Group[] getGroups() {
        return groups;
    }

    public void setGroups(Group[] groups) {
        this.groups = groups;
    }

    public ArrayList<IItem> getContent() {
        return content;
    }

    public void setContent(ArrayList<IItem> content) {
        this.content = content;
    }
    
    /**
     * Give this kit to player, if possible
     * @param player
     * @return
     */
    public boolean giveKit(IPlayer player) {
        Long lastUsed = lastUsages.get(player.getName());
        if(lastUsed == null) {
            lastUsed = new Long(ICanary.getUnixTimestamp()-delay);
        }
        if(lastUsed.longValue()+delay < ICanary.getUnixTimestamp()) {
            if(owners != null) {
                for(String owner : owners) {
                    if(owner.equals(player.getName())) {
                        apply(player);
                        return true;
                    }
                }
            }
            if(groups != null) {
                for(Group g: groups) {
                    if(player.getGroup().hasControlOver(g)) {
                        apply(player);
                        return true;
                    }
                    else if(player.isInGroup(g, false)) {
                        apply(player);
                        return true;
                    }
                }
            }
            //Both null so it is a public kit
            apply(player);
            return true;
        }
        else {
            return false;
        }
    }
    
    private void apply(IPlayer player) {
        for(IItem item : content) {
            player.giveItem(item);
        }
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
