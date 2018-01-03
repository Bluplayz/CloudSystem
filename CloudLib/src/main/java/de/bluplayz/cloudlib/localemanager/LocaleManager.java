package de.bluplayz.cloudlib.localemanager;

import de.bluplayz.cloudlib.localemanager.locale.Locale;
import de.bluplayz.cloudlib.logging.Logger;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;

public class LocaleManager {

    @Getter
    @Setter
    private static boolean debugModeActivated = false;

    @Getter
    private static LocaleManager instance;

    @Getter
    @Setter
    private Locale defaultLocale;

    @Getter
    private ArrayList<Locale> locales = new ArrayList<>();

    @Getter
    private File directory;

    public LocaleManager( String directoryPath ) {
        this( new File( directoryPath ) );
    }

    public LocaleManager( File directory ) {
        // Save instance for further use
        instance = this;

        if ( directory == null ) {
            return;
        }

        this.directory = directory;

        if ( !directory.isDirectory() ) {
            if ( !directory.mkdir() ) {
                try {
                    throw new Exception( "cannot create directory" );
                } catch ( Exception e ) {
                    Logger.getGlobal().error( e.getMessage(), e );
                }
            }
        }

        initLocales();
    }

    /**
     * load all locales in the directory which was specified in the constructor
     */
    private void initLocales() {
        if ( isDebugModeActivated() ) {
            Logger.getGlobal().info( "loading locales..." );
        }

        if ( !directory.isDirectory() || directory == null ) {
            return;
        }

        // Load locales in locale directory
        for ( File file : getDirectory().listFiles() ) {
            Locale locale = new Locale( file.getName().split( "\\." )[0] );
            getLocales().add( locale );
        }

        if ( getLocale( "en_EN" ) != null ) {
            defaultLocale = getLocale( "en_EN" );
        } else {
            if ( getLocales().size() > 0 ) {
                defaultLocale = getLocales().get( 0 );
            }
        }

        if ( isDebugModeActivated() ) {
            Logger.getGlobal().info( getLocales().size() + " locales was loaded:" );
            Logger.getGlobal().info( getLocales().toString() );
        }
    }

    /**
     * get the locale object of the specified languageCode
     *
     * @param languageCode the language code like de_DE, en_EN
     * @return the locale object if exist otherwise return default locale
     */
    public Locale getLocale( String languageCode ) {
        for ( Locale locale : getLocales() ) {
            if ( locale.getName().contains( languageCode ) ) {
                return locale;
            }
        }

        if ( isDebugModeActivated() ) {
            Logger.getGlobal().warning( "Locale " + languageCode + " was not found! Active locales: " + getLocales().toString() );
        }
        return getDefaultLocale();
    }

    /**
     * return translated string from the given language, key and args
     *
     * @param languageCode the language code like de_DE, en_EN
     * @param key          the path in the language files
     * @param args         optional arguments to replace
     * @return translated message
     */
    public String getTranslatedMessage( String languageCode, String key, Object... args ) {
        Locale locale = getLocale( languageCode );

        // Check if no locales was loaded
        if ( locale == null ) {
            Logger.getGlobal().error( "No locales was loaded!" );
            return "error while translating, '" + key + "'";
        }

        return getTranslatedMessage( locale, key, args );
    }

    /**
     * return translated string from the given language, key and args
     *
     * @param locale the locale object for translation
     * @param key    the path in the language files
     * @param args   optional arguments to replace
     * @return translated message
     */
    public String getTranslatedMessage( Locale locale, String key, Object... args ) {
        String message = locale.getMessages().getOrDefault( key, "error while translating, '" + key + "'" );

        for ( int i = 0; i < args.length; i++ ) {
            message = message.replaceAll( "\\{" + i + "}", "" + args[i] );
        }

        return message;
    }

    /**
     * create locale if not exist with the given languageCode
     *
     * @param languageCode the languageCode for the locale
     * @return the created locale if success otherwise return null
     */
    public Locale createLocale( String languageCode ) {
        // Check for existing locale
        if ( getLocale( languageCode ) != null ) {
            return getLocale( languageCode );
        }

        Locale locale = new Locale( languageCode );
        getLocales().add( locale );

        return locale;
    }
}
