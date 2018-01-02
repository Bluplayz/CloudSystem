package de.bluplayz.cloudwrapper.command;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.command.Command;

public class HelpCommand extends Command {

    public HelpCommand() {
        super( "Help" );

        getAliases().add( "?" );
        getAliases().add( "commands" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudWrapper.getInstance().getLogger().info( "Commands:" );
        for ( Command command : CloudWrapper.getInstance().getCommandHandler().getCommands() ) {
            CloudWrapper.getInstance().getLogger().info( "- " + command.getName() + " (Aliases: " + command.getAliases().toString().replace( "[", "" ).replace( "]", "" ) + ")" );
        }
    }
}
