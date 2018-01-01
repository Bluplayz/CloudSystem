package de.bluplayz.locale;

import de.bluplayz.CloudServer;

public class LocaleAPI {

    public static void log( String key, Object... args ) {
        String translatedMessage = translate( CloudServer.getInstance().getConsoleLocale(), key, args );
        for ( String line : translatedMessage.split( "\\{NEXT_LINE}" ) ) {
            CloudServer.getInstance().getLogger().info( line );
        }
    }

    public static String translate( String languageCode, String key, Object... args ) {
        return translate( CloudServer.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
    }

    public static String translate( Locale locale, String key, Object... args ) {
        String message = CloudServer.getInstance().getLocaleManager().getTranslatedMessage( locale, key, args );
        message = message.replaceAll( "\\{PREFIX}", CloudServer.getInstance().getLocaleManager().getTranslatedMessage( locale, "prefix" ) );

        return message;
    }
}
