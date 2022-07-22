package org.infrastructure.utilities;

public class AlgoUtils {
   public static int argMin(long[] costs) {
      int bestIdx = 0;
      long bestCost = costs[0];

      for(int i = 1; i < costs.length; ++i) {
         if (costs[i] < bestCost) {
            bestCost = costs[i];
            bestIdx = i;
         }
      }

      return bestIdx;
   }

   public static int argMax(long[] utils) {
      int bestIdx = 0;
      long bestUtil = utils[0];

      for(int i = 1; i < utils.length; ++i) {
         if (utils[i] > bestUtil) {
            bestUtil = utils[i];
            bestIdx = i;
         }
      }

      return bestIdx;
   }

   public static int randomInt(int a, int b) {
      return a + (int)(Math.random() * (double)(b - a));
   }

   public static int randomInt(int a) {
      return (int)(Math.random() * (double)a);
   }

   public static int choice(int[] arr) {
      assert arr.length > 0;

      return arr[randomInt(arr.length)];
   }

   public static long max(long[] utils) {
      long best = utils[0];
      long[] var3 = utils;
      int var4 = utils.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         long u = var3[var5];
         best = Long.max(best, u);
      }

      return best;
   }
}
