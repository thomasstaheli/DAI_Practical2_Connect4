package ch.heigvd.dai.commands;

import picocli.CommandLine;
import java.util.concurrent.Callable;
import ch.heigvd.dai.network.TcpServerFixedThreadPool;

@CommandLine.Command(name = "server", description = "Hosts a server for the connect 4 game on this machine.")

public class Server implements Callable<Integer> {

    @CommandLine.ParentCommand protected Root parent;
    @CommandLine.Option(
            names = {"-h", "--height"},
            description = "The height of the game board.\n",
            defaultValue = "5",
            required = false
    )
    protected int height;

    @CommandLine.Option(
            names = {"-w", "--width"},
            description = "The width of the game board.\n",
            defaultValue = "5",
            required = false
    )
    protected int width;

    @CommandLine.Option(
            names = {"-wl", "--winlenght"},
            description = "The win lenght necessary to win the game.\n",
            defaultValue = "4",
            required = false
    )
    protected int winLenght;


    @Override
    public Integer call() {
        //create a new serverClass, it will start the server
        TcpServerFixedThreadPool server = new TcpServerFixedThreadPool(height, width, winLenght);
        return 0;
    }

}
