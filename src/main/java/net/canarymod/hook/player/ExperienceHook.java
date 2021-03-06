package net.canarymod.hook.player;

import net.canarymod.api.entity.Player;
import net.canarymod.hook.CancelableHook;
import net.canarymod.plugin.PluginListener;

/**
 * Experience hook. Contains information about player experience changes.
 * @author Jason Jones
 *
 */
public final class ExperienceHook extends CancelableHook{
    
    private Player player;
    private int oldval, newval;

    public ExperienceHook(Player player, int oldval, int newval){
        this.player = player;
        this.oldval = oldval;
        this.newval = newval;
        this.type = Type.EXPERIENCE_CHANGE;
    }
    
    /**
     * Gets the {@link Player}
     * @return player
     */
    public Player getPlayer(){
        return player;
    }
    
    /**
     * Gets the old experience value
     * @return oldval
     */
    public int getOldValue(){
        return oldval;
    }
    
    /**
     * Gets the new experience value
     * @return newval
     */
    public int getNewValue(){
        return newval;
    }
    
    /**
     * Return the set of Data in this order: PLAYER OLDVAL NEWVAL ISCANCELLED
     */
    @Override
    public Object[] getDataSet(){
        return new Object[]{ player, oldval, newval, isCanceled };
    }
    
    /**
     * Dispatches the hook to the given listener.
     * @param listener The listener to dispatch the hook to.
     */
    @Override
    public void dispatch(PluginListener listener) {
        listener.onExpChange(this);
    }
}
