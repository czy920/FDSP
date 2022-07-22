//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infrastructure.core;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Agent extends Thread {
   protected int id;
   protected int[] neighbors;
   protected List<Constraint> constraints;
   protected int val;
   protected Queue<Message> messages;
   protected AtomicBoolean finished;
   protected AtomicBoolean run;
   protected int domain;
   private Mailer mailer;
   private long simulatedTime;

   public Agent(int id, int domain, int[] neighbors, List<Constraint> constraints, Mailer mailer) {
      super("A" + id);
      this.id = id;
      this.domain = domain;
      this.neighbors = neighbors;
      this.constraints = constraints;
      this.finished = new AtomicBoolean(false);
      this.run = new AtomicBoolean(true);
      this.messages = new ArrayDeque();
      this.mailer = mailer;
   }

   public double getLocalCost(Map<Integer, Integer> view) {
      double cost = 0.0D;

      Constraint cons;
      for(Iterator var4 = this.constraints.iterator(); var4.hasNext(); cost += (double)cons.eval(view) * 1.0D / (double)cons.dimOrdering.length) {
         cons = (Constraint)var4.next();
      }

      return cost;
   }

   protected abstract void onStart();

   public long getSimulatedTime() {
      return this.simulatedTime;
   }

   public void resetSimulatedTime() {
      this.simulatedTime = 0L;
   }

   public void run() {
      long start = System.currentTimeMillis();
      this.onStart();
      this.simulatedTime = System.currentTimeMillis() - start;

      while(!this.finished.get()) {
         synchronized(this.run) {
            while(!this.run.get()) {
               try {
                  this.run.wait();
               } catch (InterruptedException var8) {
                  var8.printStackTrace();
               }
            }

            this.run.set(false);
         }

         if (this.finished.get()) {
            break;
         }

         start = System.currentTimeMillis();

         while(!this.messages.isEmpty()) {
            Message msg = (Message)this.messages.poll();
            this.disposeMessage(msg.getSrc(), msg.getTyp(), msg.getContent());
         }

         this.onTimestepAdvanced();
         this.simulatedTime += System.currentTimeMillis() - start;
         synchronized(this.mailer.readyCnt) {
            int i = this.mailer.readyCnt.incrementAndGet();
            this.mailer.readyCnt.notify();
         }
      }

   }

   protected void sendMessage(int dest, int msgType, Object content) {
      synchronized(this.mailer.messages) {
         this.mailer.messages.add(new Message(this.id, dest, msgType, content));
      }
   }

   protected void broadcastMessages(int msgType, Object content) {
      synchronized(this.mailer.messages) {
         int[] var4 = this.neighbors;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int n = var4[var6];
            this.mailer.messages.add(new Message(this.id, n, msgType, content));
         }

      }
   }

   protected void terminate() {
      this.finished.set(true);
   }

   protected abstract void disposeMessage(int var1, int var2, Object var3);

   protected abstract void onTimestepAdvanced();
}
