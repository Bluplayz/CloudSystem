package de.bluplayz.localemanager.locale;

import de.bluplayz.localemanager.LocaleManager;
import de.bluplayz.localemanager.util.LocaleFile;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Locale {

    @Getter
    private String name = "";

    @Getter
    private LocaleFile localeFile;

    @Getter
    private HashMap<String, String> messages = new HashMap<>();

    public Locale( String name ) {
        this.name = name;

        // Store config file
        localeFile = new LocaleFile( LocaleManager.getInstance().getDirectory() + "/" + this.getName() + ".properties", LocaleFile.PROPERTIES );

        // Load messages
        load();
    }

    /**
     * load translations from the locale file
     */
    private void load() {
        // Clear map before adding data
        this.messages = new HashMap<>();

        for ( Map.Entry entry : this.getLocaleFile().getAll().entrySet() ) {
            String key = (String) entry.getKey();
            String value = String.valueOf( entry.getValue() );

            getMessages().put( key, value );
        }
    }


    /**
     * add the specified translation to the file and replace if already exist
     *
     * @param key   the key from the translation
     * @param value the value from the translation
     */
    public void addTranslation( String key, String value ) {
        addTranslation( key, value, true );
    }

    /**
     * add the specified translations to the file and replace if already exist
     *
     * @param translations the map with all translation keys and values
     */
    public void addTranslations( LinkedHashMap<String, String> translations ) {
        addTranslations( translations, true );
    }

    /**
     * add the specified translations to the file
     *
     * @param key     the key from the translation
     * @param value   the value from the translation
     * @param replace if the key already exist should they be replaced with given translation
     */
    public void addTranslation( String key, String value, boolean replace ) {
        if ( getLocaleFile() == null ) {
            return;
        }

        if ( !replace ) {
            if ( getLocaleFile().exists( key ) ) {
                return;
            }
        }

        getLocaleFile().set( key, value );
        getLocaleFile().save();
        load();
    }

    /**
     * add the specified translations to the file
     *
     * @param translations the map with all translation keys and values
     * @param replace      if the keys already exists should they be replaced with given translations
     */
    public void addTranslations( LinkedHashMap<String, String> translations, boolean replace ) {
        if ( getLocaleFile() == null ) {
            return;
        }

        boolean edited = false;
        for ( Map.Entry entry : translations.entrySet() ) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if ( !replace ) {
                if ( getLocaleFile().exists( key ) ) {
                    continue;
                }
            }

            getLocaleFile().set( key, value );
            edited = true;
        }

        if ( edited ) {
            getLocaleFile().save();
            load();
        }
    }

    /**
     * remove the specified translation from the file
     *
     * @param key the key which should deleted
     */
    public void removeTranslation( String key ) {
        if ( getLocaleFile() == null ) {
            return;
        }

        if ( !getLocaleFile().exists( key ) ) {
            return;
        }

        getLocaleFile().remove( key );
        getLocaleFile().save();
        load();
    }

    /**
     * remove the specified translations from the file
     *
     * @param translations the map with all
     */
    public void removeTranslations( ArrayList<String> translations ) {
        if ( getLocaleFile() == null ) {
            return;
        }

        boolean edited = false;
        for ( String key : translations ) {
            if ( !getLocaleFile().exists( key ) ) {
                continue;
            }


            getLocaleFile().remove( key );
            edited = true;
        }

        if ( edited ) {
            getLocaleFile().save();
            load();
        }
    }

    @Override
    public String toString() {
        return "Locale{" +
                "name='" + name + '\'' +
                '}';
    }
}
