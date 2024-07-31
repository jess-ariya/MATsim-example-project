package org.matsim.analysis;


import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;
import java.util.stream.Collectors;

public class CountActivities {

    public static void main(String[] args){

        // shapefile
        var shapeFileName = " path to shapefile";
        var plansFileName = "path-to-plan"; // or get the network if there is no plan
        // Transform from MATSim into Geotools coordinate (Web mercator?)
        var transformation = TransformationFactory.getCoordinateTransformation("","");

        var features = ShapeFileReader.getAllFeatures(shapeFileName);

        var geometries = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("INT_CITY").equals("Vancouver"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList()); // we hope there's only 1 item on the list

        var VGeometry = geometries.get(0);

        var population = PopulationUtils.readPopulation(plansFileName);

        var counter = 0; // initialise to 0

        // .getPersons() will return a map
        for (Person person : population.getPersons().values()) {

            var plan = person.getSelectedPlan();
            // want to exclude stage activities
            var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

            for (Activity activity : activities) {
                var coord = activity.getCoord(); // Matsim coordinate data structure
                var transformedCoor = transformation.transform(coord);
                // Transform the MATsim coord into a geotool coord so it can be passed to the other codes
                var geotoolsPoint = MGC.coord2Point(transformedCoor); // Geotools coordinate point

                if (VGeometry.contains(geotoolsPoint)){
                    counter++;
                }

            }
        }
        System.out.println(counter + "activities in VGeometry.");

    }
}
