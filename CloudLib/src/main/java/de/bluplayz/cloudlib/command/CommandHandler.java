package de.bluplayz.cloudlib.command;

import de.bluplayz.cloudlib.logging.Logger;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CommandHandler {

    @Getter
    public ArrayList<Command> commands = new ArrayList<>();

    public void registerCommand( Command command ) {
        for ( Command cmd : getCommands() ) {
            if ( command.getName().equalsIgnoreCase( cmd.getName() ) ) {
                Logger.getGlobal().warning( "The name of the command '" + command.getName() + "' is already in use." );
            }

            if ( cmd.getAliases().contains( command.getName() ) ) {
                Logger.getGlobal().warning( "An alias is already registered with the name " + command.getName() + "." );
            }

            for ( String alias : command.getAliases() ) {
                if ( cmd.getAliases().contains( alias ) ) {
                    Logger.getGlobal().warning( "The alias '" + alias + "' is already an alias of an other command." );
                    break;
                }
            }
        }

        getCommands().add( command );
    }

    public void onExecute( String message ) {
        String commandname = message.split( " " )[0];
        Command command = getCommandByName( commandname );

        if ( command == null ) {
            Logger.getGlobal().error( "Command " + commandname + " was not found!" );
            return;
        }

        ArrayList<String> args = new ArrayList<>();
        for ( String argument : message.substring( commandname.length() ).split( " " ) ) {
            if ( argument.equalsIgnoreCase( "" ) || argument.equalsIgnoreCase( " " ) ) {
                continue;
            }

            args.add( argument );
        }

        command.execute( commandname, args.toArray( new String[args.size()] ) );
    }

    public Command getCommandByName( String name ) {
        for ( Command command : getCommands() ) {
            if ( command.getName().equalsIgnoreCase( name ) ) {
                return command;
            }

            for ( String alias : command.getAliases() ) {
                if ( alias.equalsIgnoreCase( name ) ) {
                    return command;
                }
            }
        }

        return null;
    }

    public boolean commandExist( Command command ) {
        return getCommandByName( command.getName() ) != null;
    }

    public void consoleInput() {
        InputStreamReader in = new InputStreamReader( System.in );
        BufferedReader reader = new BufferedReader( in );

        try {
            String input;
            while ( ( input = reader.readLine() ) != null ) {
                if ( !input.equalsIgnoreCase( "" ) ) {
                    this.onExecute( input );
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        this.consoleInput();
    }
}
