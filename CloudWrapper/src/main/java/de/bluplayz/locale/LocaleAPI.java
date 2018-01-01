package de.bluplayz.locale;

import de.bluplayz.CloudWrapper;
import de.bluplayz.localemanager.locale.Locale;

public class LocaleAPI {

    public static void log( String key, Object... args ) {
        String translatedMessage = translate( CloudWrapper.getInstance().getConsoleLocale(), key, args );
        for ( String line : translatedMessage.split( "\\{NEXT_LINE}" ) ) {
            CloudWrapper.getInstance().getLogger().info( line );
        }
    }

    public static String translate( String languageCode, String key, Object... args ) {
        return translate( CloudWrapper.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
    }

    public static String translate( Locale locale, String key, Object... args ) {
        String message = CloudWrapper.getInstance().getLocaleManager().getTranslatedMessage( locale, key, args );
        message = message.replaceAll( "\\{PREFIX}", CloudWrapper.getInstance().getLocaleManager().getTranslatedMessage( locale, "prefix" ) );

        return message;
    }
}
