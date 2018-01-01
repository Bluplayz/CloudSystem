package de.bluplayz.command;

import de.bluplayz.CloudServer;

public class HelpCommand extends Command {

    public HelpCommand() {
        super( "Help" );

        getAliases().add( "?" );
        getAliases().add( "commands" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudServer.getInstance().getLogger().info( "Commands:" );
        for ( Command command : CloudServer.getInstance().getCommandHandler().getCommands() ) {
            CloudServer.getInstance().getLogger().info( "- " + command.getName() + " (Aliases: " + command.getAliases().toString().replace( "[", "" ).replace( "]", "" ) + ")" );
        }
    }
}
