package de.bluplayz.cloudapi.bungee;

import de.bluplayz.cloudapi.bungee.listener.ServerConnectListener;
import de.bluplayz.cloudapi.bungee.locale.LocaleAPI;
import de.bluplayz.cloudapi.bungee.network.Network;
import de.bluplayz.cloudlib.config.Config;
import de.bluplayz.cloudlib.localemanager.LocaleManager;
import de.bluplayz.cloudlib.localemanager.locale.Locale;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BungeeCloudAPI extends Plugin {

    public static final String VERSION = "1.0.0";

    @Getter
    private static BungeeCloudAPI instance;

    @Getter
    private LocaleManager localeManager;

    @Getter
    private Locale consoleLocale;

    @Getter
    private Config mainConfig;

    @Getter
    private Network network;

    @Getter
    private String serverName;

    @Getter
    private UUID serverUniqueId;

    @Getter
    private List<String> proxyFallbackPriorities = new ArrayList<>();

    @Getter
    private ExecutorService pool = Executors.newCachedThreadPool();

    public BungeeCloudAPI() {
        // Save instance for further use
        instance = this;
    }

    @Override
    public void onEnable() {
        // Initialize Main Config
        this.initMainConfig();

        // Initialize locale system
        this.initLocales();

        // Start initialize message
        LocaleAPI.log( "console_loading_message_start", "CloudMaster", VERSION );

        // Register Commands
        this.registerCommands();

        // Register Events
        this.registerEvents();

        // Edit Config
        if ( ProxyServer.getInstance().getConfig().getListeners().iterator().next().getServerPriority().contains( "lobby" ) ) {
            ProxyServer.getInstance().getConfig().getListeners().iterator().next().getServerPriority().remove( "lobby" );
        }
        if ( ProxyServer.getInstance().getServers().containsKey( "lobby" ) ) {
            ProxyServer.getInstance().getServers().remove( "lobby" );
        }

        // Initialize Network
        Config dataConfig = new Config( new File( this.getDataFolder(), "data.yml" ), Config.YAML );
        String host = dataConfig.getString( "address" );
        int port = dataConfig.getInt( "port" );
        this.serverName = dataConfig.getString( "servername" );
        this.serverUniqueId = UUID.fromString( dataConfig.getString( "uuid" ) );
        this.proxyFallbackPriorities = dataConfig.getStringList( "fallbackPriorities" );
        this.network = new Network( this, host, port );

        FileUtils.deleteQuietly( new File( this.getDataFolder(), "data.yml" ) );

        // Finish initialize message
        LocaleAPI.log( "console_loading_message_finish", "CloudMaster", VERSION );

        // Finish initialize message
        LocaleAPI.log( "console_language_set_success" );
    }

    @Override
    public void onDisable() {
    }

    private void initMainConfig() {
        if ( !this.getDataFolder().isDirectory() ) {
            this.getDataFolder().mkdir();
        }

        File configFile = new File( this.getDataFolder(), "config.yml" );

        this.mainConfig = new Config( configFile, Config.YAML );
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

        if ( shouldSave ) {
            this.getMainConfig().save();
        }
    }

    private void initLocales() {
        LinkedHashMap<String, String> translations = new LinkedHashMap<>();

        // Initialize LocaleManager
        this.localeManager = new LocaleManager( new File( this.getDataFolder(), "locales" ) );

        /** GERMAN */
        Locale germanLocale = getLocaleManager().createLocale( "de_DE" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudAPI§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7{0} v{1} wird geladen..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7{0} v{1} wurde erfolgreich geladen!" );
        translations.put( "console_language_set_success", "{PREFIX} §7Die Sprache der Konsole ist §bDeutsch§7." );
        translations.put( "network_master_connected", "{PREFIX} §7Verbindung zum CloudMaster(§b{0}§7) wurde hergestellt." );
        translations.put( "network_master_connection_lost", "{PREFIX} §cVerbindung zum CloudMaster(§b{0}§c) verloren!" );
        translations.put( "network_master_failed_connection", "{PREFIX} §cVerbindung zum CloudMaster(§b{0}§c) ist fehlgeschlagen!" );
        translations.put( "network_master_failed_connection_reconnect", "{PREFIX} §cVerbinde erneut in 3 Sekunden..." );

        germanLocale.addTranslations( translations, false );
        /** GERMAN */

        /** ENGLISH */
        Locale englishLocale = getLocaleManager().createLocale( "en_EN" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudAPI§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7Loading {0} v{1}..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7Successfully loaded {0} v{1}!" );
        translations.put( "console_language_set_success", "{PREFIX} §7The Language of the Console is §bEnglish§7." );
        translations.put( "network_master_connected", "{PREFIX} §7Successfully connected to CloudMaster(§b{0}§7)." );
        translations.put( "network_master_connection_lost", "{PREFIX} §cConnection to CloudMaster(§b{0}§c) was lost!" );
        translations.put( "network_master_failed_connection", "{PREFIX} §cConnection failed to CloudMaster(§b{0}§c)!" );
        translations.put( "network_master_failed_connection_reconnect", "{PREFIX} §cReconnect in 3 Seconds..." );

        englishLocale.addTranslations( translations, false );
        /** ENGLISH */

        // Set Console locale
        this.consoleLocale = getLocaleManager().getLocale( this.getMainConfig().getString( "language.console" ) );

        // Set default locale
        this.getLocaleManager().setDefaultLocale( getLocaleManager().getLocale( this.getMainConfig().getString( "language.fallback" ) ) );
    }

    private void registerCommands() {
        //this.getCommandHandler().registerCommand( new HelpCommand() );
    }

    private void registerEvents() {
        this.getProxy().getPluginManager().registerListener( this, new ServerConnectListener( this ) );
    }
}
