//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infrastructure.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.infrastructure.parser.GeneralParser;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class AgentManager {
   private Map<Integer, Agent> allAgent = new HashMap();
   private Mailer mailer = new Mailer();
   private boolean sort;

   public AgentManager(String problemPath, String manifestPath, String algo,boolean sort) {
      File f = new File(manifestPath);

      try {
         Element root = (new SAXBuilder()).build(f).getRootElement();
         List<Element> eleAgents = root.getChildren();
         String agentClazz = "";
         String confParserClazz = "";
         String measurementClazz = "";
         Iterator var10 = eleAgents.iterator();

         while(var10.hasNext()) {
            Element eleAgent = (Element)var10.next();
            if (eleAgent.getAttributeValue("name").toUpperCase().equals(algo.toUpperCase())) {
               agentClazz = eleAgent.getAttributeValue("class");
               confParserClazz = eleAgent.getAttributeValue("confParser", "");
               root = eleAgent;
               measurementClazz = eleAgent.getAttributeValue("measurement", "");
               break;
            }
         }

         if (agentClazz.equals("")) {
            throw new RuntimeException("unknown algo!");
         }

         GeneralParser generalParser = new GeneralParser(problemPath);
         if (!confParserClazz.equals("")) {
            Class clazz = Class.forName(confParserClazz);
            Constructor constructor = clazz.getConstructors()[0];
            AlgoConfParser algoConfParser = (AlgoConfParser)constructor.newInstance(root);
            algoConfParser.parse();
            algoConfParser.configParser(generalParser);
         }

         Problem problem = generalParser.parse(sort);

         Agent agent;
         int id;
         for(Iterator var24 = problem.agentId.iterator(); var24.hasNext(); this.allAgent.put(id, agent)) {
            id = (Integer)var24.next();
            agent = null;
            int[] neighbors = new int[((Set)problem.neighbours.get(id)).size()];
            int idx = 0;

            int n;
            for(Iterator var17 = ((Set)problem.neighbours.get(id)).iterator(); var17.hasNext(); neighbors[idx++] = n) {
               n = (Integer)var17.next();
            }

            try {
               Class clazz = Class.forName(agentClazz);
               Constructor constructor = clazz.getConstructors()[0];
               agent = (Agent)constructor.newInstance(id, problem.domainSize.get(id), neighbors, problem.constraints.get(id), this.mailer);
            } catch (Exception var19) {
               throw new RuntimeException("init exception");
            }
         }

         if (!measurementClazz.equals("")) {
            Class clazz = Class.forName(measurementClazz);
            Constructor constructor = clazz.getConstructors()[0];
            Measurement measurement = (Measurement)constructor.newInstance();
            this.mailer.setMeasurement(measurement);
         }

         this.mailer.initialize(this.allAgent);
      } catch (Exception var20) {
         var20.printStackTrace();
      }

   }

   public Mailer getMailer() {
      return this.mailer;
   }

   public void run() {
      Iterator var1 = this.allAgent.values().iterator();

      while(var1.hasNext()) {
         Agent agent = (Agent)var1.next();
         agent.start();
      }

      this.mailer.start();

      try {
         this.mailer.join();
      } catch (InterruptedException var3) {
         var3.printStackTrace();
      }

   }

   public void run(long timeout) {
      this.mailer.setTimeout(timeout);
      this.run();
   }

   public Measurement getMeasurement() {
      return this.mailer.getMeasurement();
   }

   public List<Double> getCosts() {
      return this.mailer.getCosts();
   }
}
