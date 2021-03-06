package net.canarymod.hook.entity;

import net.canarymod.api.DamageSource;
import net.canarymod.api.entity.EntityLiving;
import net.canarymod.hook.CancelableHook;
import net.canarymod.plugin.PluginListener;

/**
 * Damage hook. Contains information about an entity taking damage.
 * @author Jason Jones
 *
 */
public final class DamageHook extends CancelableHook{
    
    private EntityLiving attacker, defender;
    private DamageSource source;
    private int dealt;
    
    public DamageHook(EntityLiving attacker, EntityLiving defender, DamageSource source, int dealt){
        this.attacker = attacker;
        this.defender = defender;
        this.source = source;
        this.dealt = dealt;
        this.type = Type.DAMAGE;
    }
    
    /**
     * Gets the attacking {@link EntityLiving} if present
     * @return attacker if there is one, null otherwise
     */
    public EntityLiving getAttacker(){
        return attacker;
    }
    
    /**
     * Gets the defending {@link EntityLiving}
     * @return defender
     */
    public EntityLiving getDefender(){
        return defender;
    }
    
    /**
     * Gets the {@link DamageSource} type
     * @return source
     */
    public DamageSource getDamageSource(){
        return source;
    }
    
    /**
     * Gets the amount of damage dealt
     * @return dealt
     */
    public int getDamageDealt(){
        return dealt;
    }
    
    /**
     * Return the set of Data in this order: ATTACKER DEFENDER SOURCE DELT ISCANCELLED
     */
    @Override
    public Object[] getDataSet(){
        return new Object[]{ attacker, defender, source, dealt, isCanceled };
    }
    
    /**
     * Dispatches the hook to the given listener.
     * @param listener The listener to dispatch the hook to.
     */
    @Override
    public void dispatch(PluginListener listener) {
        listener.onDamage(this);
    }

}
