package de.bluplayz.cloudapi.bungee.locale;

import de.bluplayz.cloudapi.bungee.BungeeCloudAPI;
import de.bluplayz.cloudlib.localemanager.locale.Locale;

public class LocaleAPI {

    public static void log( String key, Object... args ) {
        String translatedMessage = translate( BungeeCloudAPI.getInstance().getConsoleLocale(), key, args );
        for ( String line : translatedMessage.split( "\\{NEXT_LINE}" ) ) {
            BungeeCloudAPI.getInstance().getLogger().info( line );
        }
    }

    public static String translate( String languageCode, String key, Object... args ) {
        return translate( BungeeCloudAPI.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
    }

    public static String translate( Locale locale, String key, Object... args ) {
        String message = BungeeCloudAPI.getInstance().getLocaleManager().getTranslatedMessage( locale, key, args );
        message = message.replaceAll( "\\{PREFIX}", BungeeCloudAPI.getInstance().getLocaleManager().getTranslatedMessage( locale, "prefix" ) );

        return message;
    }
}
