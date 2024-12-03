package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;

import ch.heigvd.dai.network.TcpClient;

import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Connect as a client, so you will be a player")
public class Client implements Callable<Integer>{

    @CommandLine.ParentCommand protected Root parent;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "The port of the server where the game is hosted\n",
            required = false,
            defaultValue = "6433"
    )
    protected int port;

    @CommandLine.Option(
            names = {"-a", "--ip"},
            description = "The ip address of the server where the game is hosted\n",
            required = false,
            defaultValue = "127.0.0.1"
    )
    protected String ip;

    @Override
    public Integer call() {

        System.out.println("Connecting to the server with ip " + ip + "\n");
        TcpClient client = new TcpClient(ip, port);
        client.run();

        return 0;
    }
}