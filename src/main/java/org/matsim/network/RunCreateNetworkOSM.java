package org.matsim.network;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.contrib.osm.networkReader.LinkProperties;
import org.matsim.contrib.osm.networkReader.OsmTags;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.Set;

/**
 * Example on how to convert osm data from e.g. http://download.geofabrik.de into a MATSim network. This examle puts all
 * motorways and primary roads into the MATSim network. If a link is contained in the supplied shape, also minor and
 * residential raods are put into the MATsim network.
 * <p>
 * After parsing the OSM-data, unreachable areas of the network are removed by using the network cleaner
 */

public final class RunCreateNetworkOSM {

//    private static String UTM7nAsEpsg = "EPSG:26910";
    private static final String osmFile = "C:/Users/HW5P/Downloads/merged.osm.pbf";
    private static final String outputFile = "./src/main/java/org/matsim/network/new-west_v2.xml"; // network_new_west_osm.xml


    public static void main(String[] args) {
        RunCreateNetworkOSM createNetwork = new RunCreateNetworkOSM();
        createNetwork.run();
    }

    public void run() {

        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:26910");
        Set<String> allowedModes = Set.of(TransportMode.car);


        Network network = new SupersonicOsmNetworkReader.Builder()

                .addOverridingLinkProperties(OsmTags.UNCLASSIFIED, new LinkProperties(LinkProperties.LEVEL_UNCLASSIFIED, 1, 15 / 3.6, 720, false))
                .addOverridingLinkProperties(OsmTags.SERVICE, new LinkProperties(9, 1, 10 / 3.6, 720, false))
                .setPreserveNodeWithId((id) -> true) // make sure we keep the geometry of the roads...
                .setCoordinateTransformation(ct)
                .setAdjustCapacityLength(1.)


//				.setIncludeLinkAtCoordWithHierarchy((coord, hierachyLevel) ->
//						hierachyLevel <= 9 &&
//								coord.getX() >= 537566 && coord.getX() <= 548009 &&
//								coord.getY() >= 5841715 && coord.getY() <= 5848804
//				)
                .setAfterLinkCreated((link, map, direction) -> {
                    link.setAllowedModes(allowedModes);

                    if (link.getLength() <= 7.5) link.setLength(8);
                })
                .build()
                .read(osmFile);

        new NetworkCleaner().run(network);

        new NetworkWriter(network).write(outputFile);
    }

}
