package hr.algebra.uno.jndi;

import lombok.Getter;

public enum ConfigurationKey {
    PLAYER_1_SERVER_PORT("player.one.server.port"),
    PLAYER_2_SERVER_PORT("player.two.server.port"),
    RMI_SERVER_PORT("rmi.server.port"),
    HOSTNAME("host.name");

    @Getter
    private final String key;

    ConfigurationKey(final String key) {
        this.key = key;
    }
}
