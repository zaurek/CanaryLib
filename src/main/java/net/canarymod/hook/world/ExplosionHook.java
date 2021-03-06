package net.canarymod.hook.world;

import java.util.List;

import net.canarymod.api.entity.Entity;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.CancelableHook;
import net.canarymod.plugin.PluginListener;

/**
 * Explosion hook. Contains information about an explosion.
 * @author Jason Jones
 *
 */
public final class ExplosionHook extends CancelableHook{
    
    private Block block;
    private Entity entity;
    private List<Block> blocksaffected;
    
    public ExplosionHook(Block block, Entity entity, List<Block>blocksaffected){
        this.block = block;
        this.entity = entity;
        this.blocksaffected = blocksaffected;
        this.type = Type.EXPLOSION;
    }
    
    /**
     * Gets the base affected {@link Block}
     * @return block
     */
    public Block getBlock(){
        return block;
    }
    
    /**
     * Gets the {@link Entity} causing the explosion
     * @return entity
     */
    public Entity getEntity(){
        return entity;
    }
    
    /**
     * Gets the list of affected blocks
     * @return blocksaffected
     */
    public List<Block> getAffectedBlocks(){
        return blocksaffected;
    }
    
    /**
     * Return the set of Data in this order: BLOCK ENTITY BLOCKSAFFECTED ISCANCELLED
     */
    @Override
    public Object[] getDataSet(){
        return new Object[]{ block, entity, blocksaffected, isCanceled };
    }
    
    /**
     * Dispatches the hook to the given listener.
     * @param listener The listener to dispatch the hook to.
     */
    @Override
    public void dispatch(PluginListener listener) {
        listener.onExplosion(this);
    }

}
