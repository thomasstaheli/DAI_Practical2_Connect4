package ch.heigvd.dai.commands;
import picocli.CommandLine;
import java.util.concurrent.Callable;
import ch.heigvd.dai.network.TcpServerFixedThreadPool;

@CommandLine.Command(name = "server", description = "Hosts a server for the connect 4 game on this machine.")

public class Server implements Callable<Integer> {

    @CommandLine.ParentCommand protected Root parent;

    @Override
    public Integer call() {
        //create a new serverClass, it will start the server
        TcpServerFixedThreadPool server = new TcpServerFixedThreadPool();
        return 0;
    }

}
