package org.infrastructure.parser;

import org.infrastructure.core.Problem;
import org.jdom2.Element;

public abstract class AbstractParser {
   protected Element root;
   protected Problem problem;

   public AbstractParser(Element root, Problem problem) {
      this.root = root;
      this.problem = problem;
   }

   public void parse(boolean sort) {
   }
}
