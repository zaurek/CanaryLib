package net.canarymod;

/**
 * Callback interface for commands.
 * @author Willem Mulder
 */
public interface MessageReceiver {
    
    /**
     * Returns the name of this <tt>MessageReceiver</tt>.
     * @return the <tt>MessageReceiver</tt>'s name.
     */
    public String getName();

    /**
     * Sends a message to this <tt>MessageReceiver</tt>.
     * @param message The message to send.
     */
    public void notify(String message);
    
    /**
     * Checks whether this <tt>MessageReceiver</tt> has the given permission.
     * @param node The node to check for.
     * @return <tt>true</tt> if this <tt>MessageReceiver</tt> has the given
     * permission, <tt>false</tt> otherwise.
     */
    public boolean hasPermission(String node);
}
