package de.bluplayz.command;

import de.bluplayz.CloudMaster;

public class HelpCommand extends Command {

    public HelpCommand() {
        super( "Help" );

        this.getAliases().add( "?" );
        this.getAliases().add( "commands" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudMaster.getInstance().getLogger().info( "Commands:" );
        for ( Command command : CloudMaster.getInstance().getCommandHandler().getCommands() ) {
            if ( command.getAliases().size() > 0 ) {
                CloudMaster.getInstance().getLogger().info( "- " + command.getName() + " (Aliases: " + command.getAliases().toString().replace( "[", "" ).replace( "]", "" ) + ")" );
            } else {
                CloudMaster.getInstance().getLogger().info( "- " + command.getName() );
            }
        }
    }
}
