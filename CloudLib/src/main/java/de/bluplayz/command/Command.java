package de.bluplayz.command;

import lombok.Getter;

import java.util.ArrayList;

public abstract class Command {

    @Getter
    private String name = "";

    @Getter
    private ArrayList<String> aliases = new ArrayList<>();

    public Command( String name ) {
        this.name = name;
    }

    public Command( String name, ArrayList<String> aliases ) {
        this.name = name;
        this.aliases = aliases;
    }

    public abstract void execute( String label, String[] args );
}
