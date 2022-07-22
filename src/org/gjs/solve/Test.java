package org.gjs.solve;

import org.infrastructure.core.AgentManager;
import org.infrastructure.core.Constraint;
import org.gjs.algo.maxsum.MaxsumAgent;
import org.gjs.algo.maxsum.MaxsumMeasurement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        String problemPath = "/Users/songyan/Documents/DCOPBenchmarks-master/foo.xml";
        String algo = "BFS".toUpperCase(Locale.ROOT);
        int depth = 3;
        double t = 50;
        int c = 1000;
        String type = "STEP";
        File file = new File(problemPath);
        if (!file.isFile() || !file.getName().endsWith(".xml")) {
            System.out.println("please specify a problem file");
            System.exit(1);
        }
        StringBuilder identifier = new StringBuilder();
        switch (algo) {
            case "GDP":
                MaxsumAgent.ACCELERATION_ALGO = "GDP";
                MaxsumAgent.DYNAMIC = false;
                identifier.append("GDP");
                break;
            case "GD2P":
                MaxsumAgent.ACCELERATION_ALGO = "GDP";
                MaxsumAgent.DYNAMIC = true;
                identifier.append("GD2P");
                break;
            case "FDSP":
                MaxsumAgent.ACCELERATION_ALGO = "FDSP";
                identifier.append("FDSP");
                break;
            case "BFS":
                MaxsumAgent.ACCELERATION_ALGO = "BFS";
                identifier.append("BFS");
                break;
            case "CONC":
                MaxsumAgent.ACCELERATION_ALGO = "CONC";
                identifier.append("CONC").append("-").append(type).append("-").append("FDSP");
                break;
            case "PTS":
                MaxsumAgent.ACCELERATION_ALGO = "PTS";
                MaxsumAgent.SORTING_DEPTH = depth;
                MaxsumAgent.WEIGHTED_CRITERION = "MAX";
                identifier.append("PTS").append("-").append(t).append('-').append(2)
                        .append("-").append("MAX");
                break;
            case "ART-GD2P":
            case "ST-GD2P":
                MaxsumAgent.ACCELERATION_ALGO = "ART-GD2P";
                identifier.append("ST-GD2P").append("-").append(t);
                break;

            default:
                throw new RuntimeException("unknown algorithm!");
        }

        String am = "am.xml";
        AgentManager manager = new AgentManager(problemPath, am, "Maxsum", false);

        if ("ART-GD2P".equalsIgnoreCase(algo) || "ST-GD2P".equalsIgnoreCase(algo)) {
            MaxsumAgent.STEP_SIZE = (int) (t * Constraint.SCALE);
        }
        if ("CONC".equalsIgnoreCase(algo)) {
            MaxsumAgent.TYPE = type;
        }

        MaxsumAgent.CYCLE = c;
        MaxsumMeasurement measurement = (MaxsumMeasurement) manager.getMeasurement();
        manager.getMailer().setPrintCycle(true);
        manager.run();
        System.out.println(identifier);

        System.out.println("pruned rate:\t" + measurement.getPrunedRate());
        System.out.println("preprocessing elapse:\t" + measurement.getPreprocessingElapse());
        System.out.println("total elapse:\t" + measurement.getTotalElapse());
        System.out.println("Memory used:\t" + measurement.getMemoryUsage());
    }


}
