package de.bluplayz.command;

public class ClearConsoleCommand extends Command {

    public ClearConsoleCommand() {
        super( "ClearConsole" );

        getAliases().add( "clear" );
    }

    @Override
    public void execute( String label, String[] args ) {
        for ( int i = 0; i < 50; ++i ) {
            System.out.println();
        }
    }
}
