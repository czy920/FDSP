package org.infrastructure.core;

public abstract class Measurement {
   public abstract void onStart();

   public abstract void measure(Agent var1);

   public abstract void onFinished();

   public abstract void onCycleEnd();

   public static class DefaultMeasurement extends Measurement {
      public void onStart() {
      }

      public void measure(Agent agent) {
      }

      public void onFinished() {
      }

      public void onCycleEnd() {
      }
   }
}
