package org.gjs.solve;

import org.apache.commons.cli.*;
import org.infrastructure.core.AgentManager;
import org.infrastructure.core.Constraint;
import org.gjs.algo.maxsum.MaxsumAgent;
import org.gjs.algo.maxsum.MaxsumMeasurement;

import java.io.File;
import java.io.IOException;

public class Solve {
    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption("p", true, "problem file");
        options.addOption("c", true, "cycles");
        options.addOption("t", true, "step size");
        options.addOption("a", true, "acceleration algorithm");
        options.addOption("ty", true, "type");
        options.addOption("am", true, "agent manifest file");
        options.addOption("cr", true, "sorting criterion");
        options.addOption("s", true, "sorting depth");
        options.addOption("sort", true, "sort");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        File file = new File(cmd.getOptionValue("p"));
        if (!file.isFile() || !file.getName().endsWith(".xml")) {
            System.out.println("please specify a problem file");
            System.exit(1);
        }
        StringBuilder identifier = new StringBuilder();
        String algo = cmd.getOptionValue("a").toUpperCase();
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
                identifier.append("CONC").append("-").append(cmd.getOptionValue("ty"));
                break;
            case "ART-GD2P":
            case "ST-GD2P":
                MaxsumAgent.ACCELERATION_ALGO = "ART-GD2P";
                identifier.append("ST-GD2P").append("-").append(cmd.getOptionValue("t"));
                break;
            case "PTS":
                MaxsumAgent.ACCELERATION_ALGO = "PTS";
                MaxsumAgent.SORTING_DEPTH = Integer.parseInt(cmd.getOptionValue("s"));
                MaxsumAgent.WEIGHTED_CRITERION = cmd.getOptionValue("cr");
                identifier.append("PTS").append("-").append(cmd.getOptionValue("t")).append('-').append(cmd.getOptionValue("s"))
                        .append("-").append(cmd.getOptionValue("cr"));
                break;
            case "HOP":
                identifier.append("HOP");
                break;
            default:
                throw new RuntimeException("unknown algorithm!");
        }
        double t = Double.parseDouble(cmd.getOptionValue("t", "1"));
        int c = Integer.parseInt(cmd.getOptionValue("c", "1000"));
        boolean sort = Boolean.parseBoolean(cmd.getOptionValue("sort", "false"));
        String am = cmd.getOptionValue("am");
        String type = cmd.getOptionValue("ty");
        AgentManager manager = new AgentManager(cmd.getOptionValue("p"), am, "Maxsum", sort);
        if ("ART-GD2P".equalsIgnoreCase(algo) || "PTS".equalsIgnoreCase(algo)) {
            MaxsumAgent.STEP_SIZE = (int) (t * Constraint.SCALE);
            System.out.println(t);
        }
        if ("CONC".equalsIgnoreCase(algo)) {
            MaxsumAgent.TYPE = type;
        }
        MaxsumAgent.CYCLE = c;
        manager.getMailer().setPrintCycle(true);
        manager.run();
        System.out.println(identifier);
        MaxsumMeasurement measurement = (MaxsumMeasurement) manager.getMeasurement();

        System.out.println("pruned rate:\t" + measurement.getPrunedRate());
        System.out.println("preprocessing elapse:\t" + measurement.getPreprocessingElapse());
        System.out.println("total elapse:\t" + measurement.getTotalElapse());
        System.out.println("memory Usage:\t" + measurement.getMemoryUsage());

    }
}

