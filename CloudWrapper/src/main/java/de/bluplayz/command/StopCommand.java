package de.bluplayz.command;

import de.bluplayz.CloudWrapper;

public class StopCommand extends Command {

    public StopCommand() {
        super( "Stop" );

        this.getAliases().add( "disconnect" );
        this.getAliases().add( "close" );
    }

    @Override
    public void execute( String label, String[] args ) {
        CloudWrapper.getInstance().shutdown();
    }
}
