package de.bluplayz.logging;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class Logger {

    @Getter
    private static Set<Logger> loggers = new HashSet<>();

    @Getter
    @Setter
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "HH:mm:ss" );

    @Getter
    private String name;

    public Logger( String name ) {
        this.name = name;

        // Set TimeZone
        this.getSimpleDateFormat().setTimeZone( TimeZone.getTimeZone( "Europe/Berlin" ) );

        // Add to Loggerlist
        Logger.getLoggers().add( this );
    }

    /**
     * Get the Logger by a specified name
     *
     * @param name the name of the logger
     * @return the logger with that name, create a logger if doesn't exist
     */
    public static Logger getLogger( String name ) {
        for ( Logger logger : Logger.getLoggers() ) {
            if ( logger.getName().equalsIgnoreCase( name ) ) {
                return logger;
            }
        }

        return new Logger( name );
    }

    /**
     * logs a normal text into the console
     *
     * @param message the message which should print into the console
     */
    public void info( String message ) {
        if ( message.equalsIgnoreCase( "" ) ) {
            return;
        }

        message = this.translateColorCodes( message );

        System.out.println( Color.RESET + Color.CYAN + "[" + this.getSimpleDateFormat().format( new Date() ) + "] [" + Thread.currentThread().getName() + "]: " + Color.RESET + message + Color.RESET );
    }

    /**
     * logs an error text into the console
     *
     * @param message the message which should print into the console
     */
    public void error( String message ) {
        if ( message.equalsIgnoreCase( "" ) ) {
            return;
        }

        message = this.translateColorCodes( message );

        System.out.println( Color.RESET + Color.CYAN + "[" + this.getSimpleDateFormat().format( new Date() ) + "] [" + Thread.currentThread().getName() + "]: " + Color.RESET + Color.RED + "[ERROR] " + message + Color.RESET );
    }

    /**
     * logs a debug text into the console
     *
     * @param message the message which should print into the console
     */
    public void debug( String message ) {
        if ( message.equalsIgnoreCase( "" ) ) {
            return;
        }

        message = this.translateColorCodes( message );

        System.out.println( Color.RESET + Color.CYAN + "[" + this.getSimpleDateFormat().format( new Date() ) + "] [" + Thread.currentThread().getName() + "]: " + Color.RESET + Color.CYAN + "[DEBUG] " + message + Color.RESET );
    }

    /**
     * logs a warning text into the console
     *
     * @param message the message which should print into the console
     */
    public void warning( String message ) {
        if ( message.equalsIgnoreCase( "" ) ) {
            return;
        }

        message = this.translateColorCodes( message );

        System.out.println( Color.RESET + Color.CYAN + "[" + this.getSimpleDateFormat().format( new Date() ) + "] [" + Thread.currentThread().getName() + "]: " + Color.RESET + Color.YELLOW + "[WARNING] " + message + Color.RESET );
    }

    private String translateColorCodes( String message ) {
        message = message.replaceAll( "§a", Color.GREEN );
        message = message.replaceAll( "§b", Color.CYAN );
        message = message.replaceAll( "§c", Color.RED );
        message = message.replaceAll( "§d", Color.MAGENTA );
        message = message.replaceAll( "§e", Color.YELLOW );
        message = message.replaceAll( "§f", Color.RESET );
        //message = message.replaceAll( "§0", Color.UNKNOWN );
        message = message.replaceAll( "§1", Color.BLUE );
        message = message.replaceAll( "§2", Color.GREEN );
        message = message.replaceAll( "§3", Color.CYAN );
        message = message.replaceAll( "§4", Color.RED );
        message = message.replaceAll( "§5", Color.MAGENTA );
        message = message.replaceAll( "§6", Color.YELLOW );
        message = message.replaceAll( "§7", Color.GRAY );
        message = message.replaceAll( "§8", Color.GRAY );
        message = message.replaceAll( "§9", Color.BLUE );

        message = message.replaceAll( "§r", Color.RESET );
        message = message.replaceAll( "§l", Color.BOLD );
        message = message.replaceAll( "§n", Color.UNDERLINED );

        return message;
    }
}
