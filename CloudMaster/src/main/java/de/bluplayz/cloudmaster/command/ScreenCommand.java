package de.bluplayz.cloudmaster.command;

import de.bluplayz.cloudlib.command.Command;
import de.bluplayz.cloudlib.logging.Logger;

public class ScreenCommand extends Command {

    public ScreenCommand() {
        super( "Screen" );
    }

    @Override
    public void execute( String label, String[] args ) {
        Logger.getGlobal().info( "§eWork in progress!" );
    }
}
