package org.matsim.population;

import org.matsim.api.core.v01.Id;

import org.matsim.api.core.v01.events.LinkEnterEvent; //new
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent; //new
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent; //new
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler; //new
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler; //new
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler; //new

import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;

/**
 * Needs to be developed and tested!
 * */
public class SimpleEventHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler, PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler, LinkEnterEventHandler{

    private final Map<Id<Person>, Double> departureTimeByPerson = new HashMap<>();
    // Added below:
    private final Map<Id<Person>, Double> travelledDistance = new HashMap<>();
    private final Map<Id<Vehicle>, Id<Person>> vehicle2Persons = new HashMap<Id<org.matsim.vehicles.Vehicle>, Id<Person>>();
    private final Network network;

    private int[] distanceDistribution = new int[30];

    public SimpleEventHandler(Network network) {
        this.network = network;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {

        System.out.println("AgentDepartureEvent -- time: " + event.getTime() + " -- linkId: " + event.getLinkId() + " -- personId: " + event.getPersonId());
        departureTimeByPerson.put(event.getPersonId(), event.getTime());
        travelledDistance.put(event.getPersonId(), 0.0);

    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
//        System.out.println("PersonArrivalEvent -- time: " + event.getTime() + " -- linkId: " + event.getLinkId() + " -- personId: " + event.getPersonId());
//        double departureTime = departureTimeByPerson.get(event.getPersonId());
//        double travelTime = event.getTime() - departureTime;
//        System.out.println("Travel time of person " + event.getPersonId() + " is " + travelTime + " s.");

        if (this.departureTimeByPerson.containsKey(event.getPersonId())) {
            double departureTime = departureTimeByPerson.get(event.getPersonId());
            double travelTime = event.getTime() - departureTime;
            System.out.println("Travel time of person " + event.getPersonId() + " is " + travelTime + " s.");

            departureTimeByPerson.remove(event.getPersonId());

            if (this.travelledDistance.containsKey(event.getPersonId())) {
                double distance = this.travelledDistance.get(event.getPersonId());
                this.travelledDistance.remove(event.getPersonId());
                int distanceInKm = (int) (distance / 1000);
                if (distanceInKm > 29) {
                    distanceInKm = 29; //everything above 29km goes into the last bin
                }
                this.distanceDistribution[distanceInKm]++;
            }
        }
    }

    public void handleEvent(LinkEnterEvent event){
        if (this.vehicle2Persons.containsKey(event.getVehicleId())){
            Id<Person> personId = this.vehicle2Persons.get(event.getVehicleId());
            double distanceSoFarTravelled = this.travelledDistance.get(personId);
            double length = this.network.getLinks().get(event.getLinkId()).getLength();
            double newDistanceTravelled = distanceSoFarTravelled + length;
            this.travelledDistance.put(personId, newDistanceTravelled);
        }
    }

    public void handleEvent(PersonEntersVehicleEvent event){
        if (travelledDistance.containsKey(event.getPersonId())){
            vehicle2Persons.put(event.getVehicleId(), event.getPersonId());
        }
    }

    public void handleEvent(PersonLeavesVehicleEvent event){
        if (this.vehicle2Persons.containsKey(event.getVehicleId())){
            this.vehicle2Persons.remove(event.getVehicleId());
        }
    }

    public void reset(int interation){
        distanceDistribution = new int[30];
    }

    public int[] getDistanceDistribution(){
        return distanceDistribution;
    }


}