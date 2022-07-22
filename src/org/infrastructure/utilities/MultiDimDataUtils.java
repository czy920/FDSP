package org.infrastructure.utilities;

public class MultiDimDataUtils {
   public static boolean next(int[] curAssign, int[] domainLen, int skipIdx) {
      int lastIdx = curAssign.length - 1;
      if (lastIdx == skipIdx) {
         --lastIdx;
      }

      int prev = curAssign[skipIdx];
      int var10002 = curAssign[lastIdx]++;
      int firstIdx = skipIdx == 0 ? 1 : 0;

      for(int i = lastIdx; i >= firstIdx; --i) {
         if (i != skipIdx) {
            if (curAssign[i] < domainLen[i]) {
               break;
            }

            if (i == firstIdx) {
               if (prev != curAssign[skipIdx]) {
                  throw new RuntimeException("error");
               }

               return false;
            }

            curAssign[i] = 0;
            if (i - 1 != skipIdx) {
               ++curAssign[i - 1];
            } else {
               ++curAssign[i - 2];
            }
         }
      }

      if (prev != curAssign[skipIdx]) {
         throw new RuntimeException("error");
      } else {
         return true;
      }
   }
}
