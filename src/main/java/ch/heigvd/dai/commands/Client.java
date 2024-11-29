package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;

import ch.heigvd.dai.Display;
import ch.heigvd.dai.GameBoard;
import ch.heigvd.dai.network.TcpClient;
import ch.heigvd.dai.userIO;

import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Connect as a client, so you will be a player")
public class Client implements Callable<Integer>{

    @CommandLine.ParentCommand protected Root parent;
    @CommandLine.Option(
            names = {"--ip"},
            description = "The ip address of the server where the game is hosted\n",
            required = true
    )

    protected String ip;

    @Override
    public Integer call() {

        System.out.println("Connecting to the server with ip " + ip + "\n");
        TcpClient client = new TcpClient(ip);
        client.run();

        return 0;
    }
}