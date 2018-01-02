package de.bluplayz;

import de.bluplayz.cloudwrapper.command.ClearConsoleCommand;
import de.bluplayz.cloudlib.command.CommandHandler;
import de.bluplayz.cloudwrapper.command.HelpCommand;
import de.bluplayz.cloudwrapper.command.StopCommand;
import de.bluplayz.cloudlib.config.Config;
import de.bluplayz.cloudwrapper.locale.LocaleAPI;
import de.bluplayz.cloudlib.localemanager.LocaleManager;
import de.bluplayz.cloudlib.localemanager.locale.Locale;
import de.bluplayz.cloudlib.logging.Logger;
import de.bluplayz.cloudwrapper.network.Network;
import de.bluplayz.cloudwrapper.server.BungeeCordProxy;
import de.bluplayz.cloudwrapper.server.SpigotServer;
import lombok.Getter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudWrapper {

    public static final String VERSION = "1.0.0";

    @Getter
    private static CloudWrapper instance;

    @Getter
    public List<BungeeCordProxy> bungeeCordProxies = new LinkedList<>();

    @Getter
    public List<SpigotServer> spigotServers = new LinkedList<>();

    @Getter
    private Logger logger;

    @Getter
    private LocaleManager localeManager;

    @Getter
    private Locale consoleLocale;

    @Getter
    private Config config;

    @Getter
    private CommandHandler commandHandler;

    @Getter
    private Network network;

    @Getter
    private ExecutorService pool = Executors.newCachedThreadPool();

    public CloudWrapper() {
        // Save instance for further use
        instance = this;

        this.logger = new Logger( new File( CloudWrapper.getRootDirectory(), "logs" ) );

        // Rename Main-Thread
        Thread.currentThread().setName( "CloudWrapperMain-Thread" );

        // Check configdata
        this.initConfig();

        // Initialize locale system
        this.initLocales();

        // Start initialize message
        LocaleAPI.log( "console_loading_message_start", "CloudWrapper", VERSION );

        // Initialize command handler
        this.commandHandler = new CommandHandler();

        // Register Commands
        this.registerCommands();

        // Initialize console input
        this.getPool().execute( () -> {
            Thread.currentThread().setName( "Commands-Thread" );
            this.getCommandHandler().consoleInput();
        } );

        // Initialize Network
        this.network = new Network( this, this.getConfig().getString( "network.cloudmaster.address" ), this.getConfig().getInt( "network.cloudmaster.port" ) );

        // Add Shutdown Thread
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            LocaleAPI.log( "system_exit_loading" );

            // Stop all Minecraft Server and Proxies
            for ( BungeeCordProxy bungeeCordProxy : this.getBungeeCordProxies() ) {
                bungeeCordProxy.shutdown();
            }
            for ( SpigotServer spigotServer : this.getSpigotServers() ) {
                spigotServer.shutdown();
            }

            LocaleAPI.log( "system_exit_finished" );
        } ) );

        // Finish initialize message
        LocaleAPI.log( "console_loading_message_finish", "CloudWrapper", VERSION );

        // Finish initialize message
        LocaleAPI.log( "console_language_set_success" );
    }

    public static void main( String[] args ) {
        try {
            File localDirectory = new File( CloudWrapper.getRootDirectory(), "local" );
            if ( !localDirectory.isDirectory() ) {
                localDirectory.mkdir();
            }

            File pluginDirectory = new File( localDirectory, "plugins" );
            if ( !pluginDirectory.isDirectory() ) {
                pluginDirectory.mkdir();
            }

            // Load libs
            File libDirectory = new File( CloudWrapper.getRootDirectory(), "libs" );
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

            new CloudWrapper();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static File getRootDirectory() {
        File directory = null;

        try {
            directory = new File( CloudWrapper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() ).getParentFile();
            if ( !directory.isDirectory() ) {
                directory.mkdir();
            }
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }

        return directory;
    }

    private void registerCommands() {
        this.getCommandHandler().registerCommand( new HelpCommand() );
        this.getCommandHandler().registerCommand( new StopCommand() );
        this.getCommandHandler().registerCommand( new ClearConsoleCommand() );
    }

    private void initConfig() {
        File configDirectory = new File( CloudWrapper.getRootDirectory().getAbsolutePath() );

        if ( !configDirectory.isDirectory() ) {
            configDirectory.mkdir();
        }

        config = new Config( configDirectory.getAbsolutePath() + "/config.yml", Config.YAML );
        boolean shouldSave = false;

        // LANGUAGE
        if ( !this.getConfig().exists( "language.console" ) ) {
            this.getConfig().set( "language.console", "de_DE" );
            shouldSave = true;
        }
        if ( !this.getConfig().exists( "language.fallback" ) ) {
            this.getConfig().set( "language.fallback", "en_EN" );
            shouldSave = true;
        }

        // NETWORK
        if ( !this.getConfig().exists( "network.cloudmaster.address" ) ) {
            this.getConfig().set( "network.cloudmaster.address", "185.82.22.43" );
            shouldSave = true;
        }
        if ( !this.getConfig().exists( "network.cloudmaster.port" ) ) {
            this.getConfig().set( "network.cloudmaster.port", 19132 );
            shouldSave = true;
        }

        if ( shouldSave ) {
            this.getConfig().save();
        }
    }

    private void initLocales() {
        LinkedHashMap<String, String> translations = new LinkedHashMap<>();

        // Initialize LocaleManager
        this.localeManager = new LocaleManager( CloudWrapper.getRootDirectory() + "/locales" );

        /** GERMAN */
        Locale germanLocale = getLocaleManager().createLocale( "de_DE" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudWrapper§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7{0} v{1} wird geladen..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7{0} v{1} wurde erfolgreich geladen!" );
        translations.put( "console_language_set_success", "{PREFIX} §7Die Sprache der Konsole ist §bDeutsch§7." );
        translations.put( "system_exit_loading", "{PREFIX} §7CloudMaster wird heruntergefahren..." );
        translations.put( "system_exit_finished", "{PREFIX} §7CloudMaster wurde erfolgreich heruntergefahren." );
        translations.put( "network_master_connected", "{PREFIX} §7Verbindung zum CloudMaster(§b{0}§7) wurde hergestellt." );
        translations.put( "network_master_connection_lost", "{PREFIX} §cVerbindung zum CloudMaster(§b{0}§c) verloren!" );
        translations.put( "network_master_failed_connection", "{PREFIX} §cVerbindung zum CloudMaster(§b{0}§c) ist fehlgeschlagen!" );
        translations.put( "network_master_failed_connection_reconnect", "{PREFIX} §cVerbinde erneut in 3 Sekunden..." );
        translations.put( "network_server_starting", "{PREFIX} §b{0} §7wird auf Port §b{1}§7 gestartet..." );
        translations.put( "network_server_started_successfully", "{PREFIX} §b{0} §7ist nun online auf Port §b{1}§7." );
        translations.put( "network_server_stopping", "{PREFIX} §b{0} §7wird heruntergefahren..." );
        translations.put( "network_server_stopped_successfully", "{PREFIX} §b{0} §7ist nun offline." );

        translations.put( "network_server_starting_no_template_folder", "{PREFIX} §b{0} §7konnte nicht gestartet werden. Der TemplatePfad von dem Template §6{1}§7(§6{2}§7) ist ungültig!" );


        germanLocale.addTranslations( translations, false );
        /** GERMAN */

        /** ENGLISH */
        Locale englishLocale = getLocaleManager().createLocale( "en_EN" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudWrapper§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7Loading {0} v{1}..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7Successfully loaded {0} v{1}!" );
        translations.put( "console_language_set_success", "{PREFIX} §7The Language of the Console is §bEnglish§7." );
        translations.put( "system_exit_loading", "{PREFIX} §7CloudMaster shutting down..." );
        translations.put( "system_exit_finished", "{PREFIX} §7Shutdown CloudMaster successfully." );
        translations.put( "network_master_connected", "{PREFIX} §7Successfully connected to CloudMaster(§b{0}§7)." );
        translations.put( "network_master_connection_lost", "{PREFIX} §cConnection to CloudMaster(§b{0}§c) was lost!" );
        translations.put( "network_master_failed_connection", "{PREFIX} §cConnection failed to CloudMaster(§b{0}§c)!" );
        translations.put( "network_master_failed_connection_reconnect", "{PREFIX} §cReconnect in 3 Seconds..." );
        translations.put( "network_server_starting", "{PREFIX} §b{0} §7starting on port §b{1}§7..." );
        translations.put( "network_server_started_successfully", "{PREFIX} §b{0} §7is now online on port §b{1}§7." );
        translations.put( "network_server_stopping", "{PREFIX} §b{0} §7shutting down..." );
        translations.put( "network_server_stopped_successfully", "{PREFIX} §b{0} §7is now offline." );

        englishLocale.addTranslations( translations, false );
        /** ENGLISH */

        // Set Console locale
        this.consoleLocale = getLocaleManager().getLocale( this.getConfig().getString( "language.console" ) );

        // Set default locale
        this.getLocaleManager().setDefaultLocale( getLocaleManager().getLocale( this.getConfig().getString( "language.fallback" ) ) );
    }

    public void shutdown() {
        this.getPool().shutdown();
        System.exit( 0 );
    }
}
