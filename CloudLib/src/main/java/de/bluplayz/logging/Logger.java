package de.bluplayz.logging;

import lombok.Getter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Logger {

    @Getter
    private static Set<Logger> loggers = new HashSet<>();

    @Getter
    private org.apache.log4j.Logger apacheLogger;

    @Getter
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" ) {{
        this.setTimeZone( TimeZone.getTimeZone( "Europe/Berlin" ) );
    }};

    public Logger( File logsDirectory ) {
        Date date = new Date( System.currentTimeMillis() );
        File file = new File( logsDirectory.getAbsolutePath() + "/unknown.log" );
        for ( int id = 1; id < Integer.MAX_VALUE; id++ ) {
            file = new File( logsDirectory, this.getSimpleDateFormat().format( date ) + "-" + id + ".log" );
            if ( file.exists() ) {
                continue;
            }
            break;
        }

        if ( !logsDirectory.exists() ) {
            logsDirectory.mkdir();
        }

        org.apache.log4j.Logger.getRootLogger().setLevel( Level.OFF );
        this.apacheLogger = org.apache.log4j.Logger.getLogger( "CloudSystem" );
        PatternLayout layout = new PatternLayout( "[%d{HH:mm:ss}] [%t] %m%n" );
        ConsoleAppender consoleAppender = new ConsoleAppender( layout );
        this.getApacheLogger().addAppender( consoleAppender );

        try {
            FileAppender fileAppender = new FileAppender( layout, file.getAbsolutePath(), false );
            FileAppender fileAppender2 = new FileAppender( layout, logsDirectory.getAbsolutePath() + "/latest.log", false );
            this.getApacheLogger().addAppender( fileAppender );
            this.getApacheLogger().addAppender( fileAppender2 );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        this.getApacheLogger().setLevel( Level.INFO );

        // Add to Loggerlist
        Logger.getLoggers().add( this );
    }

    /**
     * Get a Logger
     */
    public static Logger getGlobal() {
        return Logger.getLoggers().iterator().next();
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

        this.getApacheLogger().info( "[INFO]: " + message );
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

        this.getApacheLogger().info( "[ERROR]: " + message );
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

        this.getApacheLogger().info( "[DEBUG]: " + message );
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

        this.getApacheLogger().info( "[WARNING]: " + message );
    }

    private String translateColorCodes( String message ) {
        Map<String, String> replace = new HashMap<String, String>() {{
            this.put( "§a", "" );
            this.put( "§b", "" );
            this.put( "§c", "" );
            this.put( "§d", "" );
            this.put( "§e", "" );
            this.put( "§f", "" );

            this.put( "§0", "" );
            this.put( "§1", "" );
            this.put( "§2", "" );
            this.put( "§3", "" );
            this.put( "§4", "" );
            this.put( "§5", "" );
            this.put( "§6", "" );
            this.put( "§7", "" );
            this.put( "§8", "" );
            this.put( "§9", "" );

            this.put( "§r", "" );
            this.put( "§l", "" );
            this.put( "§n", "" );
        }};
        for ( Map.Entry entry : replace.entrySet() ) {
            message = message.replaceAll( (String) entry.getKey(), (String) entry.getValue() );
        }

        return message;

    }
}
