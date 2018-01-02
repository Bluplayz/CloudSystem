package de.bluplayz.cloudapi.bukkit.locale;

import de.bluplayz.cloudapi.bukkit.BukkitCloudAPI;
import de.bluplayz.cloudlib.localemanager.locale.Locale;

public class LocaleAPI {

    public static void log( String key, Object... args ) {
        String translatedMessage = translate( BukkitCloudAPI.getInstance().getConsoleLocale(), key, args );
        for ( String line : translatedMessage.split( "\\{NEXT_LINE}" ) ) {
            BukkitCloudAPI.getInstance().getLogger().info( line );
        }
    }

    public static String translate( String languageCode, String key, Object... args ) {
        return translate( BukkitCloudAPI.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
    }

    public static String translate( Locale locale, String key, Object... args ) {
        String message = BukkitCloudAPI.getInstance().getLocaleManager().getTranslatedMessage( locale, key, args );
        message = message.replaceAll( "\\{PREFIX}", BukkitCloudAPI.getInstance().getLocaleManager().getTranslatedMessage( locale, "prefix" ) );

        return message;
    }
}
