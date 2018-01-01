package de.bluplayz.command;

import de.bluplayz.CloudServer;

public class StopCommand extends Command {

    public StopCommand() {
        super( "Stop" );

        this.getAliases().add( "disconnect" );
        this.getAliases().add( "close" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudServer.getInstance().shutdown();
    }
}
