package de.bluplayz.cloudwrapper.command;

import de.bluplayz.CloudWrapper;
import de.bluplayz.cloudlib.command.Command;

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
