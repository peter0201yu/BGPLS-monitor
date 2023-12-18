package server;

import models.Topologies;
import models.bgpls.NLRI;
import models.bgpls.UpdateMessage;
import models.igp.IGPInstance;
import models.igp.ospf.OSPFInstance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import parser.exabgp.ExabgpParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class Server {

    @Bean
    public Topologies topologies() {
        return new Topologies();
    }

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Server.class, args);

        String resourceName;
        if (args.length < 1) {
            System.out.println("No resourceName given, use default.");
            resourceName = "dummydata/bgpls-examples.json";
        }
        else {
            resourceName = args[0];
        }

        InputStream inputStream = Server.class.getClassLoader().getResourceAsStream(resourceName);
        Topologies topologies = context.getBean(Topologies.class);

        ExabgpParser parser = new ExabgpParser();
        try {
            List<UpdateMessage> messages = parser.readMessage(inputStream);
            for (UpdateMessage message : messages) {
                for (NLRI nlri : message.nlris) {
                    // TODO: refactor this code into separate function, also used in OSPFInstanceTest
                    IGPInstance topology = topologies.get(
                            nlri.getBgplsId(),
                            nlri.getAs(),
                            nlri.protocolId,
                            nlri.instanceId
                    );

                    if (topology == null) {
                        // TODO: better way of creating topologies when its not just ospf
                        topology = new OSPFInstance();
                        topologies.add(
                                nlri.getBgplsId(),
                                nlri.getAs(),
                                nlri.protocolId,
                                nlri.instanceId,
                                topology
                        );
                    }
                    topology.handleNLRI(message.attributes, nlri);
                }
            }
        } catch (IOException e) {
            System.out.println("boo hoo");
        }
    }
}
