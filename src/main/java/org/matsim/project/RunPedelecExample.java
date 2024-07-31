package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

public class RunPedelecExample {
    public static void main(String[] args){

        var url = IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("equil"),"configv1.xml");
        Config config = ConfigUtils.loadConfig(url);
        config.controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.controller().setLastIteration(1);

        // Continue watching lecture 7!
//        config.strategy().addStrategySettings(params);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);

        controler.run();

    }


}
