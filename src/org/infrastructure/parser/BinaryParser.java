//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infrastructure.parser;

import java.util.HashMap;
import java.util.List;
import org.infrastructure.core.Constraint;
import org.infrastructure.core.Problem;
import org.jdom2.Element;

public class BinaryParser extends AbstractParser {
   public BinaryParser(Element root, Problem problem) {
      super(root, problem);
   }

   public void parse() {
      List<Element> eleRelations = this.root.getChild("relations").getChildren();

      for (Element eleRelation : eleRelations) {
         int consId = Integer.parseInt(eleRelation.getAttributeValue("name").substring(1));
         Constraint cons = (Constraint) this.problem.constraintInfo.get(consId);
         String[] tokens = eleRelation.getText().split("\\|");
         HashMap<Integer, Integer> assign = new HashMap<>();
         String[] var8 = tokens;
         int var9 = tokens.length;

         for (int var10 = 0; var10 < var9; ++var10) {
            String token = var8[var10];
            String[] parts = token.split(":");
            long cost = Long.parseLong(parts[0]);
            parts = parts[1].split(" ");

            for (int i = 0; i < cons.dimOrdering.length; ++i) {
               int dim = cons.dimOrdering[i];
               assign.put(dim, Integer.parseInt(parts[i]) - 1);
            }

            cons.setValue(assign, cost);
         }
      }

   }
}
