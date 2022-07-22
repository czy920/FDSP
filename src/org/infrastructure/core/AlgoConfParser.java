package org.infrastructure.core;

import org.infrastructure.parser.GeneralParser;
import org.jdom2.Element;

public abstract class AlgoConfParser {
   protected Element root;

   public AlgoConfParser(Element root) {
      this.root = root;
   }

   public abstract void parse();

   public void configParser(GeneralParser parser) {
   }
}
