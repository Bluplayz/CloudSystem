package de.bluplayz;

import de.bluplayz.command.*;
import de.bluplayz.config.Config;
import de.bluplayz.locale.LocaleAPI;
import de.bluplayz.localemanager.LocaleManager;
import de.bluplayz.localemanager.locale.Locale;
import de.bluplayz.logging.Logger;
import de.bluplayz.network.Network;
import de.bluplayz.server.BungeeCordProxy;
import de.bluplayz.server.CloudWrapper;
import de.bluplayz.server.ServerManager;
import de.bluplayz.server.SpigotServer;
import de.bluplayz.server.template.Template;
import lombok.Getter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudMaster {

    public static final String VERSION = "1.0.0";

    @Getter
    private static CloudMaster instance;

    @Getter
    private Logger logger;

    @Getter
    private LocaleManager localeManager;

    @Getter
    private Locale consoleLocale;

    @Getter
    private ServerManager serverManager;

    @Getter
    private Config mainConfig;

    @Getter
    private Config groupConfig;

    @Getter
    private CommandHandler commandHandler;

    @Getter
    private Network network;

    @Getter
    private ExecutorService pool = Executors.newCachedThreadPool();

    public CloudMaster() {
        // Save instance for further use
        instance = this;

        this.logger = new Logger( new File( CloudMaster.getRootDirectory(), "logs" ) );

        // Add Shutdown Hook
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
        } ) );

        // Rename Main-Thread
        Thread.currentThread().setName( "CloudMasterMain-Thread" );

        // Initialize Main Config
        this.initMainConfig();

        // Initialize Groups Config
        this.initGroupsConfig();

        // Initialize locale system
        this.initLocales();

        // Start initialize message
        LocaleAPI.log( "console_loading_message_start", "CloudMaster", VERSION );

        // Initialize command handler
        this.commandHandler = new CommandHandler();

        // Initialize ServerManager
        this.serverManager = new ServerManager();

        // Register Commands
        this.registerCommands();

        // Initialize console input
        this.getPool().execute( () -> {
            Thread.currentThread().setName( "Commands-Thread" );
            this.getCommandHandler().consoleInput();
        } );

        // Initialize Network
        this.network = new Network( this, this.getMainConfig().getInt( "network.cloudmaster.port" ), this.getMainConfig().getList( "network.cloudmaster.whitelist" ) );

        // Add Shutdown Thread
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            // Stop all Minecraft Server and Proxies
            LocaleAPI.log( "system_exit_loading" );
            for ( CloudWrapper cloudWrapper : this.getServerManager().getCloudWrappers() ) {
                for ( BungeeCordProxy bungeeCordProxy : cloudWrapper.getBungeeCordProxies() ) {
                    bungeeCordProxy.shutdown();
                }
                for ( SpigotServer spigotServer : cloudWrapper.getSpigotServers() ) {
                    spigotServer.shutdown();
                }
            }
            LocaleAPI.log( "system_exit_finished" );
        } ) );

        // Finish initialize message
        LocaleAPI.log( "console_loading_message_finish", "CloudMaster", VERSION );

        // Finish initialize message
        LocaleAPI.log( "console_language_set_success" );
    }

    public static void main( String[] args ) {
        try {
            // Load libs
            File libDirectory = new File( CloudMaster.getRootDirectory(), "libs" );
            if ( !libDirectory.isDirectory() ) {
                libDirectory.mkdir();
            }

            for ( File file : libDirectory.listFiles() ) {
                URL url = file.toURI().toURL();

                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                Method method = URLClassLoader.class.getDeclaredMethod( "addURL", URL.class );
                method.setAccessible( true );
                method.invoke( classLoader, url );
            }

            new CloudMaster();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static File getRootDirectory() {
        File directory = null;

        try {
            directory = new File( CloudMaster.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() ).getParentFile();
            if ( !directory.isDirectory() ) {
                directory.mkdir();
            }
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }

        return directory;
    }

    private void registerCommands() {
        getCommandHandler().registerCommand( new HelpCommand() );
        getCommandHandler().registerCommand( new StopCommand() );
        getCommandHandler().registerCommand( new ClearConsoleCommand() );
        getCommandHandler().registerCommand( new ListCommand() );
    }

    private void initMainConfig() {
        //File configDirectory = new File( this.getRootDirectory().getAbsolutePath() );
        File configDirectory = CloudMaster.getRootDirectory();

        if ( !configDirectory.isDirectory() ) {
            configDirectory.mkdir();
        }

        this.mainConfig = new Config( configDirectory.getAbsolutePath() + "/config.yml", Config.YAML );
        boolean shouldSave = false;

        // LANGUAGE
        if ( !this.getMainConfig().exists( "language.console" ) ) {
            this.getMainConfig().set( "language.console", "de_DE" );
            shouldSave = true;
        }
        if ( !this.getMainConfig().exists( "language.fallback" ) ) {
            this.getMainConfig().set( "language.fallback", "en_EN" );
            shouldSave = true;
        }

        // NETWORK
        if ( !this.getMainConfig().exists( "network.cloudmaster.port" ) ) {
            this.getMainConfig().set( "network.cloudmaster.port", 19132 );
            shouldSave = true;
        }
        if ( !this.getMainConfig().exists( "network.cloudmaster.whitelist" ) ) {
            this.getMainConfig().set( "network.cloudmaster.whitelist", new ArrayList<String>() {{
                this.add( "localhost" );
                this.add( "185.82.22.43" );
            }} );
            shouldSave = true;
        }

        if ( shouldSave ) {
            this.getMainConfig().save();
        }
    }

    private void initGroupsConfig() {
        File configDirectory = CloudMaster.getRootDirectory();

        if ( !configDirectory.isDirectory() ) {
            configDirectory.mkdir();
        }

        this.groupConfig = new Config( new File( CloudMaster.getRootDirectory(), "groups.yml" ), Config.YAML );
        boolean shouldSave = false;

        if ( this.getGroupConfig().getRootSection().size() == 0 ) {
            this.getGroupConfig().setAll( new LinkedHashMap<String, Object>() {{
                this.put( "Bungee", new LinkedHashMap<String, Object>() {{
                    this.put( "serverType", "PROXY" );
                    this.put( "minOnlineServers", 1 );
                    this.put( "maxOnlineServers", 1 );
                    this.put( "maxMemory", 1000 );
                    this.put( "templateFolder", "/home/server/Bungee/" );
                    this.put( "fallbackPriorities", new ArrayList<String>() {{
                        this.add( "Lobby" );
                        this.add( "Lobby" );
                    }} );
                }} );
                this.put( "Lobby", new LinkedHashMap<String, Object>() {{
                    this.put( "serverType", "SPIGOT" );
                    this.put( "minOnlineServers", 1 );
                    this.put( "maxOnlineServers", 2 );
                    this.put( "maxMemory", 500 );
                    this.put( "templateFolder", "/home/server/Lobby/" );
                }} );
            }} );

            shouldSave = true;
        }

        if ( shouldSave ) {
            this.getGroupConfig().save();
        }

        for ( Map.Entry entry : this.getGroupConfig().getAll().entrySet() ) {
            String name = (String) entry.getKey();
            Map<String, Object> data = (Map<String, Object>) entry.getValue();

            Template template = new Template();
            template.setName( name );
            template.setType( Template.Type.valueOf( (String) data.get( "serverType" ) ) );
            template.setMinOnlineServers( (int) data.get( "minOnlineServers" ) );
            template.setMaxOnlineServers( (int) data.get( "maxOnlineServers" ) );
            template.setMaxMemory( (int) data.get( "maxMemory" ) );
            template.setTemplateFolder( (String) data.get( "templateFolder" ) );
            if ( template.getType() == Template.Type.PROXY ) {
                template.setProxyFallbackPriorities( (List<String>) data.get( "fallbackPriorities" ) );
            }

            Template.getAllTemplates().add( template );
        }
    }

    private void initLocales() {
        LinkedHashMap<String, String> translations = new LinkedHashMap<>();

        // Initialize LocaleManager
        this.localeManager = new LocaleManager( CloudMaster.getRootDirectory() + "/locales" );

        /** GERMAN */
        Locale germanLocale = getLocaleManager().createLocale( "de_DE" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudMaster§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7{0} v{1} wird geladen..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7{0} v{1} wurde erfolgreich geladen!" );
        translations.put( "console_language_set_success", "{PREFIX} §7Die Sprache der Konsole ist §bDeutsch§7." );
        translations.put( "network_netty_started_successfully", "{PREFIX} §7Netty Server wurde erfolgreich auf Port §b{0} §7gestartet." );
        translations.put( "network_netty_starting_failed", "{PREFIX} §cFehler beim Starten des Netty Servers." );
        translations.put( "network_server_starting", "{PREFIX} §b{0} §7wird auf Port §b{1}§7 gestartet..." );
        translations.put( "network_server_started_successfully", "{PREFIX} §b{0} §7ist nun online auf Port §b{1}§7." );
        translations.put( "network_server_stopping", "{PREFIX} §b{0} §7wird heruntergefahren..." );
        translations.put( "network_server_stopped_successfully", "{PREFIX} §b{0} §7ist nun offline." );
        translations.put( "network_wrapper_connected", "{PREFIX} §b{0} §7hat sich verbunden mit §b{1}§7." );
        translations.put( "network_wrapper_disconnected", "{PREFIX} §b{0} §7hat die Verbindung getrennt von §b{1}§7." );
        translations.put( "system_exit_loading", "{PREFIX} §7CloudMaster wird heruntergefahren..." );
        translations.put( "system_exit_finished", "{PREFIX} §7CloudMaster wurde erfolgreich heruntergefahren." );

        germanLocale.addTranslations( translations, false );
        /** GERMAN */

        /** ENGLISH */
        Locale englishLocale = getLocaleManager().createLocale( "en_EN" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudMaster§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7Loading {0} v{1}..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7Successfully loaded {0} v{1}!" );
        translations.put( "console_language_set_success", "{PREFIX} §7The Language of the Console is §bEnglish§7." );
        translations.put( "network_netty_started_successfully", "{PREFIX} §7Netty Server was successfully started on port §b{0}§7." );
        translations.put( "network_netty_starting_failed", "{PREFIX} §cError while starting Netty Server." );
        translations.put( "network_server_starting", "{PREFIX} §b{0} §7starting on port §b{1}§7..." );
        translations.put( "network_server_started_successfully", "{PREFIX} §b{0} §7is now online on port §b{1}§7." );
        translations.put( "network_server_stopping", "{PREFIX} §b{0} §7shutting down..." );
        translations.put( "network_server_stopped_successfully", "{PREFIX} §b{0} §7is now offline." );
        translations.put( "network_wrapper_connected", "{PREFIX} §b{0} §7connected from §b{1}§7." );
        translations.put( "network_wrapper_disconnected", "{PREFIX} §b{0} §7disconnected from §b{1}§7." );
        translations.put( "system_exit_loading", "{PREFIX} §7CloudMaster shutting down..." );
        translations.put( "system_exit_finished", "{PREFIX} §7Shutdown CloudMaster successfully." );

        englishLocale.addTranslations( translations, false );
        /** ENGLISH */

        // Set Console locale
        this.consoleLocale = getLocaleManager().getLocale( this.getMainConfig().getString( "language.console" ) );

        // Set default locale
        this.getLocaleManager().setDefaultLocale( getLocaleManager().getLocale( this.getMainConfig().getString( "language.fallback" ) ) );
    }

    public void shutdown() {
        this.getPool().shutdown();
        System.exit( 0 );
    }
}
