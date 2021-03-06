package net.canarymod;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.canarymod.config.Configuration;

/**
 * CanaryMod Log manager. All static methods.
 * 
 * @author Chris Ksoll
 * @author Jos Kuijpers
 * 
 */
public class Logman {
    private final static Logger mclog = Logger.getLogger("Minecraft");

    /**
     * Log with INFO level
     * 
     * @param message
     */
    public static void logInfo(String message) {
        mclog.log(Level.INFO, message);
    }
    
    /**
     * Logs messages only if the system runs in debug mode
     * @param message
     */
    public static void logDebug(String message) {
        if(Configuration.getServerConfig().isDebugMode()) {
            mclog.log(Level.INFO, message);
        }
    }

    /**
     * Log with warning level
     * 
     * @param message
     */
    public static void logWarning(String message) {
        mclog.log(Level.WARNING, message);
    }

    /**
     * Log with severe level
     * 
     * @param message
     */
    public static void logSevere(String message) {
        mclog.log(Level.SEVERE, message);
    }

    /**
     * Print a stacktrace to the server.log with WARNING level
     * 
     * @param e
     */
    public static void logStackTrace(String message, Throwable e) {
        mclog.log(Level.WARNING, message, e);
    }
    
    /**
     * Convenience shortcut to System.out.println().
     * Prints to the output stream on a new line
     * @param message
     */
    public static void println(String message) {
        System.out.println(message);
    }
    
    /**
     * Convenience shortcut to System.out.print().<br>
     * Prints to the output stream on the same line
     * @param message
     */
    public static void print(String message) {
        System.out.print(message);
    }
}
