package tigerworkshop.webapphardwarebridge;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tigerworkshop.webapphardwarebridge.utils.TLSUtil;

public class Server {

    private static Logger logger = LoggerFactory.getLogger("tigerworkshop.webapphardwarebridge.Server");

    public static void main(String[] args) {
        // Parse Options
        Options options = new Options();

        options.addOption(Option.builder().longOpt("tls").desc("TLS Enabled").hasArg(false).required(false).build());
        options.addOption(Option.builder().longOpt("key").desc("Private Key Path").hasArg(true).required(false).build());
        options.addOption(Option.builder().longOpt("cert").desc("Certificate Path").hasArg(true).required(false).build());
        options.addOption(Option.builder().longOpt("ca-bundle").desc("CA Bundle Path").hasArg(true).required(false).build());
        options.addOption(Option.builder().longOpt("bind").desc("Bind Address").hasArg(true).required(false).build());
        options.addOption(Option.builder().longOpt("port").desc("Port").hasArg(true).required(false).build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        logger.info("Application Started");
        logger.info("Program Version: " + Config.VERSION);

        logger.debug("OS Name: " + System.getProperty("os.name"));
        logger.debug("OS Version: " + System.getProperty("os.version"));
        logger.debug("OS Architecture: " + System.getProperty("os.arch"));

        logger.debug("Java Version: " + System.getProperty("java.version"));
        logger.debug("Java Vendor: " + System.getProperty("java.vendor"));

        try {
            cmd = parser.parse(options, args);

            // Create WebSocket tigerworkshop.webapphardwarebridge.Server
            ProxyWebSocketServer webSocketServer = new ProxyWebSocketServer(cmd.getOptionValue("bind", "0.0.0.0"), Integer.parseInt(cmd.getOptionValue("port", "22212")));

            // Additional Options
            webSocketServer.setReuseAddr(true);
            webSocketServer.setTcpNoDelay(true);
            webSocketServer.setConnectionLostTimeout(3);

            // Start WebSocket tigerworkshop.webapphardwarebridge.Server
            webSocketServer.start();

            // WSS/TLS Options
            if (cmd.hasOption("tls")) {
                webSocketServer.setWebSocketFactory(TLSUtil.getSecureFactory(cmd.getOptionValue("cert"), cmd.getOptionValue("key"), cmd.getOptionValue("ca-bundle")));
            }

            logger.info("WebSocket started on " + webSocketServer.getAddress().getHostName() + ":" + webSocketServer.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
