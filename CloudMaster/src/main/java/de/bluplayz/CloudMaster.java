package de.bluplayz;

import de.bluplayz.cloudlib.command.CommandHandler;
import de.bluplayz.cloudlib.config.Config;
import de.bluplayz.cloudlib.localemanager.LocaleManager;
import de.bluplayz.cloudlib.localemanager.locale.Locale;
import de.bluplayz.cloudlib.logging.Logger;
import de.bluplayz.cloudlib.server.template.Template;
import de.bluplayz.cloudmaster.command.*;
import de.bluplayz.cloudmaster.locale.LocaleAPI;
import de.bluplayz.cloudmaster.network.Network;
import de.bluplayz.cloudmaster.server.BungeeCordProxy;
import de.bluplayz.cloudmaster.server.CloudWrapper;
import de.bluplayz.cloudmaster.server.ServerManager;
import de.bluplayz.cloudmaster.server.SpigotServer;
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

    @Getter
    private ExecutorService synchronizedPool = Executors.newSingleThreadExecutor();

    /**
     * TODO
     * - StartServer Command
     * - StopServer Command
     * - Logs speichern
     * - Screen Funktion
     *
     * - ServerDataUpdatePacket
     * -
     *
     * - Spieler connectet über einen Proxy
     * - Beim ServerSwitch Und/oder ServerJoin schickt er zum Server ein packet (über den Master)
     * und die BukkitAPI schaltet ihn in einer List für 3 Sekunden frei zu joinen
     * Andernfalls wird er gekickt, weil er nicht von einem CloudSystem proxy kam sondern von einem anderen Proxy
     */
    public CloudMaster() {
        // Save instance for further use
        instance = this;

        this.logger = new Logger( new File( CloudMaster.getRootDirectory(), "logs" ) );

        this.getLogger().info( "" );
        this.getLogger().info( "Source: https://github.com/Bluplayz/CloudSystem" );
        this.getLogger().info( "  ____ _                 _ ____            _" );
        this.getLogger().info( " / ___| | ___  _   _  __| / ___| _   _ ___| |_ ___ _ __ ___" );
        this.getLogger().info( "| |   | |/ _ \\| | | |/ _` \\___ \\| | | / __| __/ _ \\ '_ ` _ \\" );
        this.getLogger().info( "| |___| | (_) | |_| | (_| |___) | |_| \\__ \\ ||  __/ | | | | |" );
        this.getLogger().info( " \\____|_|\\___/ \\__,_|\\__,_|____/ \\__, |___/\\__\\___|_| |_| |_|" );
        this.getLogger().info( "Developed by Bluplayz            |___/" );
        this.getLogger().info( "" );

        // Add Shutdown Hook
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
        } ) );

        // Rename Main-Thread
        Thread.currentThread().setName( "CloudMasterMain-Thread" );

        // Initialize Main Config
        this.initMainConfig();

        // Initialize locale system
        this.initLocales();

        // Initialize Groups Config
        this.initGroupsConfig();

        // Initialize command handler
        this.commandHandler = new CommandHandler();

        // Initialize ServerManager
        this.serverManager = new ServerManager();

        // Register Commands
        this.registerCommands();

        // Initialize console input
        this.getPool().execute( () -> {
            Thread.currentThread().setName( "Commands-Thread" );
            this.getCommandHandler().consoleInput( command -> LocaleAPI.log( "command_not_found", command ) );
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
            Logger.getGlobal().error( e.getMessage(), e );
            try {
                System.out.println( "Stopping in 3 Seconds..." );
                Thread.sleep( 3000 );
            } catch ( InterruptedException e1 ) {
                Logger.getGlobal().error( e1.getMessage(), e1 );
            }
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
            Logger.getGlobal().error( e.getMessage(), e );
        }

        return directory;
    }

    private void registerCommands() {
        this.getCommandHandler().registerCommand( new HelpCommand() );
        this.getCommandHandler().registerCommand( new StopCommand() );
        this.getCommandHandler().registerCommand( new ListCommand() );
        this.getCommandHandler().registerCommand( new DispatchCommand() );
        this.getCommandHandler().registerCommand( new ScreenCommand() );
        this.getCommandHandler().registerCommand( new SaveServerCommand() );
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

        LocaleAPI.log( "network_templates_loading" );
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
            LocaleAPI.log( "network_template_loaded", template.getName() );
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
        translations.put( "console_language_set_success", "§7Die Sprache der Konsole ist §bDeutsch§7." );
        translations.put( "command_not_found", "§cCommand §b{0} §cwurde nicht gefunden!" );
        translations.put( "network_netty_started_successfully", "§7Netty Server wurde auf Port §b{0} §7gestartet." );
        translations.put( "network_netty_starting_failed", "§cFehler beim Starten des Netty Servers." );
        translations.put( "network_server_starting", "§b{0}§7(§6{1}§7) §7wird auf Port §b{2}§7 gestartet..." );
        translations.put( "network_server_started_successfully", "§b{0}§7(§6{1}§7) §7ist nun online auf Port §b{2}§7." );
        translations.put( "network_server_stopping", "§b{0}§7(§6{1}§7) §7wird heruntergefahren..." );
        translations.put( "network_server_stopped_successfully", "§b{0}§7(§6{1}§7) §7ist nun offline." );
        translations.put( "network_wrapper_connected", "§b{0} §7hat sich verbunden mit §b{1}§7." );
        translations.put( "network_wrapper_disconnected", "§b{0} §7hat die Verbindung getrennt von §b{1}§7." );
        translations.put( "system_exit_loading", "§7CloudMaster wird heruntergefahren..." );
        translations.put( "system_exit_finished", "§7CloudMaster wurde heruntergefahren." );
        translations.put( "network_templates_loading", "§7Templates werden geladen..." );
        translations.put( "network_template_loaded", "§7Template geladen: §b{0}§7." );

        translations.put( "command_dispatch_usage", "§7Benutzung: dispatch <Servername> <Commandline>" );
        translations.put( "command_dispatch_server_not_exist", "§cDer Server §b{0} §cexistiert nicht!" );
        translations.put( "command_dispatch_success", "§7Du hast §b{0} §7den Command §b{1} §7geschickt." );

        translations.put( "command_saveserver_usage", "§7Benutzung: saveserver <Servername> <Templatename>" );
        translations.put( "command_saveserver_server_not_exist", "§cDer Server §b{0} §cexistiert nicht!" );
        translations.put( "command_saveserver_template_not_exist", "§cDas Template §b{0} §cexistiert nicht!" );
        translations.put( "command_saveserver_success", "§7Der Server §b{0} §7wurde in das Template §b{1} §7kopiert." );

        germanLocale.addTranslations( translations, false );
        /** GERMAN */

        /** ENGLISH */
        Locale englishLocale = getLocaleManager().createLocale( "en_EN" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudMaster§7]§r" );
        translations.put( "console_language_set_success", "§7The Language of the Console is §bEnglish§7." );
        translations.put( "command_not_found", "§cCommand §b{0} §cwas not found!" );
        translations.put( "network_netty_started_successfully", "§7Netty Server was started on port §b{0}§7." );
        translations.put( "network_netty_starting_failed", "§cError while starting Netty Server." );
        translations.put( "network_server_starting", "§b{0}§7(§6{1}§7) §7starting on port §b{2}§7..." );
        translations.put( "network_server_started_successfully", "§b{0}§7(§6{1}§7) §7is now online on port §b{2}§7." );
        translations.put( "network_server_stopping", "§b{0}§7(§6{1}§7) §7shutting down..." );
        translations.put( "network_server_stopped_successfully", "§b{0}§7(§6{1}§7) §7is now offline." );
        translations.put( "network_wrapper_connected", "§b{0} §7connected from §b{1}§7." );
        translations.put( "network_wrapper_disconnected", "§b{0} §7disconnected from §b{1}§7." );
        translations.put( "system_exit_loading", "§7CloudMaster shutting down..." );
        translations.put( "system_exit_finished", "§7CloudMaster shutdown." );
        translations.put( "network_templates_loading", "§7Loading Templates..." );
        translations.put( "network_template_loaded", "§7Template loaded: §b{0}§7." );

        translations.put( "command_dispatch_usage", "§7Usage: dispatch <Servername> <Commandline>" );
        translations.put( "command_dispatch_server_not_exist", "§cThe Server §b{0} §cdoesn't exist!" );
        translations.put( "command_dispatch_success", "§7You send the Command §b{1} §7to §b{0}§7." );

        translations.put( "command_saveserver_usage", "§7Usage: saveserver <Servername> <Templatename>" );
        translations.put( "command_saveserver_server_not_exist", "§cThe Server §b{0} §cdoesn't exit!" );
        translations.put( "command_saveserver_template_not_exist", "§cThe Template §b{0} §cdoesn't exist!" );
        translations.put( "command_saveserver_success", "§7The Server §b{0} §7was copied in the Template §b{1}§7." );

        englishLocale.addTranslations( translations, false );
        /** ENGLISH */

        // Set Console locale
        this.consoleLocale = getLocaleManager().getLocale( this.getMainConfig().getString( "language.console" ) );

        // Set default locale
        this.getLocaleManager().setDefaultLocale( getLocaleManager().getLocale( this.getMainConfig().getString( "language.fallback" ) ) );
    }

    public void shutdown() {
        this.getSynchronizedPool().shutdown();
        this.getPool().shutdown();
        System.exit( 0 );
    }
}
