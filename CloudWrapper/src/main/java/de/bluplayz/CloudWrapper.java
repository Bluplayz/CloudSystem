package de.bluplayz;

import de.bluplayz.command.ClearConsoleCommand;
import de.bluplayz.command.CommandHandler;
import de.bluplayz.command.HelpCommand;
import de.bluplayz.command.StopCommand;
import de.bluplayz.config.Config;
import de.bluplayz.locale.LocaleAPI;
import de.bluplayz.localemanager.LocaleManager;
import de.bluplayz.localemanager.locale.Locale;
import de.bluplayz.logging.Logger;
import de.bluplayz.network.Network;
import lombok.Getter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudWrapper {

    public static final String VERSION = "1.0.0";

    @Getter
    private static CloudWrapper instance;

    @Getter
    private Logger logger = Logger.getLogger( "CloudSystem" );

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

        // Finish initialize message
        LocaleAPI.log( "console_loading_message_finish", "CloudWrapper", VERSION );

        // Finish initialize message
        LocaleAPI.log( "console_language_set_success" );
    }

    public static void main( String[] args ) {
        try {
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
        getCommandHandler().registerCommand( new HelpCommand() );
        getCommandHandler().registerCommand( new StopCommand() );
        getCommandHandler().registerCommand( new ClearConsoleCommand() );
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

        germanLocale.addTranslations( translations, false );
        /** GERMAN */

        /** ENGLISH */
        Locale englishLocale = getLocaleManager().createLocale( "en_EN" );

        translations.clear();
        translations.put( "prefix", "§7[§3CloudWrapper§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §7Loading {0} v{1}..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §7Successfully loaded {0} v{1}!" );
        translations.put( "console_language_set_success", "{PREFIX} §7The Language of the Console is §bEnglish§7." );

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
