package org.matsim.network;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.scenario.ScenarioUtils;

public class RunModifyNetwork {
    public static void main(String[] args) {

        // read in the network
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("network_NV_v2.xml"); // path to network.xml

        /*
         * First, create a new Config and a new Scenario. One always has to do this when working with the MATSim
         * data containers.
         *
         */
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        // iterate through all links
        for (Link l : network.getLinks().values()){
            //get current capacity
            double oldCapacity = l.getCapacity();
            double newCapacity = oldCapacity * 2.0 ;

            // set new capacity
            l.setCapacity(newCapacity);
        }
        /*
         * Clean the Network. Cleaning means removing disconnected components, so that afterwards there is a route from every link
         * to every other link. This may not be the case in the initial network converted from OpenStreetMap.
         */
        new NetworkCleaner().run(network);

        new NetworkWriter(network).write("path-to-modified-network.xml");
    }
}
