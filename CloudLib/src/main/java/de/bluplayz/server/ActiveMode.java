package de.bluplayz.server;

import lombok.Getter;

public enum ActiveMode {
    STARTING( 0 ),
    STARTED( 1 ),
    ONLINE( 1 ),
    STOPPING( 2 ),
    OFFLINE( 3 );

    @Getter
    private int id;

    ActiveMode( int id ) {
        this.id = id;
    }
}
