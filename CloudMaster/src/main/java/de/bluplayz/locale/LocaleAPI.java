package de.bluplayz.locale;

import de.bluplayz.CloudMaster;
import de.bluplayz.localemanager.locale.Locale;

public class LocaleAPI {

    public static void log( String key, Object... args ) {
        String translatedMessage = translate( CloudMaster.getInstance().getConsoleLocale(), key, args );
        for ( String line : translatedMessage.split( "\\{NEXT_LINE}" ) ) {
            CloudMaster.getInstance().getLogger().info( line );
        }
    }

    public static String translate( String languageCode, String key, Object... args ) {
        return translate( CloudMaster.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
    }

    public static String translate( Locale locale, String key, Object... args ) {
        String message = CloudMaster.getInstance().getLocaleManager().getTranslatedMessage( locale, key, args );
        message = message.replaceAll( "\\{PREFIX}", CloudMaster.getInstance().getLocaleManager().getTranslatedMessage( locale, "prefix" ) );

        return message;
    }
}
