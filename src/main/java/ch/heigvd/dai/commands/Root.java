package ch.heigvd.dai.commands;

import picocli.CommandLine;

@CommandLine.Command(
        description = "This is a multiplayer Connect4 game. To play, you will need one server and two clients.",
        version = "1.0.0",
        subcommands = {
                Client.class,
                Server.class
        },
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true)
public class Root {

}