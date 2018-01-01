package de.bluplayz.command;

import de.bluplayz.CloudMaster;

public class StopCommand extends Command {

    public StopCommand() {
        super( "Stop" );

        this.getAliases().add( "disconnect" );
        this.getAliases().add( "close" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudMaster.getInstance().shutdown();
    }
}
