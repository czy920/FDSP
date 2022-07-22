//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infrastructure.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.infrastructure.core.Measurement.DefaultMeasurement;

public class Mailer extends Thread {
   private Map<Integer, Agent> allAgents;
   protected AtomicInteger readyCnt;
   protected Queue<Message> messages;
   private int stopCnt;
   private Map<Integer, Integer> allAssign;
   private List<Double> costs;
   private boolean printCycle = true;
   private Measurement measurement = new DefaultMeasurement();
   private long timeout;

   public Mailer() {
      super("mailer");
   }

   public void setMeasurement(Measurement measurement) {
      this.measurement = measurement;
   }

   public Measurement getMeasurement() {
      return this.measurement;
   }

   public void setTimeout(long timeout) {
      this.timeout = timeout;
   }

   public void initialize(Map<Integer, Agent> allAgents) {
      this.allAgents = allAgents;
      this.readyCnt = new AtomicInteger(0);
      this.messages = new ConcurrentLinkedQueue<>();
      this.allAssign = new HashMap<>();
      this.costs = new ArrayList<>();
   }

   public void setPrintCycle(boolean printCycle) {
      this.printCycle = printCycle;
   }

   public void run() {
      this.measurement.onStart();
      long startTime = System.currentTimeMillis();

      while(this.allAgents.size() != this.stopCnt) {
         synchronized(this.readyCnt) {
            while(this.readyCnt.get() != this.allAgents.size() - this.stopCnt) {
               try {
                  this.readyCnt.wait();
               } catch (InterruptedException var13) {
                  var13.printStackTrace();
               }
            }

            this.readyCnt.set(0);
         }

         long curTime = System.currentTimeMillis();

         Agent agent;
         while(!this.messages.isEmpty()) {
            Message msg = (Message)this.messages.poll();
            agent = (Agent)this.allAgents.get(msg.getDst());
            if (!agent.finished.get()) {
               ((Agent)this.allAgents.get(agent.id)).messages.add(msg);
            }
         }

         this.stopCnt = 0;

         for(Iterator var15 = this.allAgents.values().iterator(); var15.hasNext(); this.allAssign.put(agent.id, agent.val)) {
            agent = (Agent)var15.next();
            if (agent.finished.get()) {
               ++this.stopCnt;
            } else {
               this.measurement.measure(agent);
            }
         }

         this.measurement.onCycleEnd();
         double cost = 0.0D;

//         agent;
         for(Iterator var7 = this.allAgents.values().iterator(); var7.hasNext(); cost += agent.getLocalCost(this.allAssign)) {
            agent = (Agent)var7.next();
         }

         this.costs.add(cost);
         if (this.printCycle) {
            System.out.println("cycle " + this.costs.size() + " " + cost);
         }

         boolean terminate = false;
         if (this.timeout > 0L && curTime - startTime > this.timeout) {
            terminate = true;
         }

         Iterator var18 = this.allAgents.values().iterator();

         while(var18.hasNext()) {
            agent = (Agent)var18.next();
            synchronized(agent.run) {
               if (terminate) {
                  agent.terminate();
               }

               agent.run.set(true);
               agent.run.notify();
            }
         }

         if (terminate) {
            break;
         }
      }

      this.measurement.onFinished();
   }

   public List<Double> getCosts() {
      return this.costs;
   }
}
