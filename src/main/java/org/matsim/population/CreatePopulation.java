package org.matsim.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ScoringConfigGroup.ActivityParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreatePopulation {

    private static final Random random = new Random();

    public static void main(String[] args) {

        Config config = ConfigUtils.createConfig(); // Creates 'Config' object

        config.controller().setLastIteration(0);

        ActivityParams home = new ActivityParams("home");
        home.setTypicalDuration(getRandomDuration(random, 14*60*60,18*60*60));
        config.scoring().addActivityParams(home);
        ActivityParams work = new ActivityParams("work");
        work.setTypicalDuration(getRandomDuration(random, 6*60*60, 10*60*60));
        config.scoring().addActivityParams(work);

        Scenario scenario = ScenarioUtils.createScenario(config);

        new MatsimNetworkReader(scenario.getNetwork()).readFile("scenarios/equil/new-west_v2.xml");

        fillScenario(scenario);

        // Write the population to the plans.xml file
        new PopulationWriter(scenario.getPopulation()).write("scenarios/equil/plans500v3.xml");

        // Initialize the controller
        Controler controler = new Controler(scenario);

        // Added this
        // Initialize the EventsManager and SimpleEventsHandler
        EventsManager eventsManager = EventsUtils.createEventsManager();
        SimpleEventHandler simpleEventHandler = new SimpleEventHandler(scenario.getNetwork());
        eventsManager.addHandler(simpleEventHandler);

        controler.getConfig().controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        controler.run();

        // Added this
        // After the simulation, you can retrieve and process the data from the CombinedEventHandler
        int[] distanceDistribution = simpleEventHandler.getDistanceDistribution();
        writeDistancesToFile(distanceDistribution, "output/distanceDistribution.txt");
    }


    private static int getRandomDuration(Random random, int minDuration, int maxDuration){
        return minDuration + random.nextInt(maxDuration-minDuration);
    }

    private static int getRandomEndTime(Random random, int minHour, int maxHour){
        int minSeconds = minHour * 60 * 60;
        int maxSeconds = maxHour * 60 * 60;
        return minSeconds + random.nextInt(maxSeconds-minSeconds);
    }

    private static Population fillScenario(Scenario scenario){
        Population population = scenario.getPopulation();
        Network network = scenario.getNetwork();

        // Collect all nodes from the network
        List<Node> nodes = new ArrayList<>(network.getNodes().values());


        for (int i = 0; i< 500; i++){
            //Randomly select nodes
            Node startNode = nodes.get(random.nextInt(nodes.size()));
            Node endNode = nodes.get(random.nextInt(nodes.size()));

            Coord coord = startNode.getCoord();
            Coord coordWork = endNode.getCoord();
            createOnePerson(scenario, population, i, coord, coordWork);
        }

        return population;
    }

    private static void createOnePerson(Scenario scenario, Population population, int i, Coord coord, Coord coordWork){

        Person person = population.getFactory().createPerson(Id.createPersonId("p_"+i));

        Plan plan = population.getFactory().createPlan();

        int homeDuration = getRandomDuration(random, 14 * 60 * 60, 18 * 60 * 60);
        int homeEndTime = getRandomEndTime(random, 6, 12);
        Activity home = population.getFactory().createActivityFromCoord("home", coord);
        home.setEndTime(homeEndTime);
        home.setMaximumDuration(homeDuration);
        plan.addActivity(home);

        Leg hinweg = population.getFactory().createLeg("car");
        plan.addLeg(hinweg);

        int workDuration = getRandomDuration(random, 6 * 60 * 60, 10*60*60);
        int workEndTime = getRandomEndTime(random, 14, 22);
        Activity work = population.getFactory().createActivityFromCoord("work", coordWork);
        work.setEndTime(workEndTime);
        work.setMaximumDuration(workDuration);
        plan.addActivity(work);

        Leg rueckweg = population.getFactory().createLeg("car");
        plan.addLeg(rueckweg);

        Activity home2 = population.getFactory().createActivityFromCoord("home", coord);
        plan.addActivity(home2);

        person.addPlan(plan);
        population.addPerson(person);

    }

    private static void writeDistancesToFile(int[] distanceDistribution, String fileName) {
        BufferedWriter bw = IOUtils.getBufferedWriter(fileName);
        try {
            bw.write("Distance\tRides");
            for (int i = 0; i < distanceDistribution.length; i++) {
                bw.newLine();
                bw.write(i + "\t" + distanceDistribution[i]);
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
