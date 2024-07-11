package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.net.URL;

class RunMatsimFromExamplesUtils{

	public static void main( String[] args ){

		URL context = org.matsim.examples.ExamplesUtils.getTestScenarioURL( "equil" ); // static; directory
		URL url = IOUtils.extendUrl( context, "config.xml" ); //extend url; file

		Config config = ConfigUtils.loadConfig( url );
		config.controller().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		// config.controller().setLastIteration(0); // added from lec 3
		// ---

		Scenario scenario = ScenarioUtils.loadScenario( config );

		// ---
		// can also add persons, can edit in xml like normal

		Controler controler = new Controler( scenario );

		// ---

		controler.run();

	}

}
