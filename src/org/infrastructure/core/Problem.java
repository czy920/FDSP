package org.infrastructure.core;

import java.util.*;

public class Problem {
   public List<Integer> agentId = new ArrayList<>();
   public Map<Integer, Integer> domainSize = new HashMap<>();
   public Map<Integer, Set<Integer>> neighbours = new HashMap<>();
   public Map<Integer, List<Constraint>> constraints = new HashMap<>();
   public Map<Integer, Constraint> constraintInfo = new HashMap<>();

   public Problem() {
   }
}
