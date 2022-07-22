package org.infrastructure.parser;

import org.infrastructure.core.Constraint;
import org.infrastructure.core.Problem;
import org.jdom2.Element;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class NAryParser extends AbstractParser {
    private String relationPath;

    public NAryParser(Element root, Problem problem, String relationPath) {
        super(root, problem);
        this.relationPath = relationPath;
    }

    public void parse(boolean sort) {
        List eleRelations = this.root.getChild("relations").getChildren();
        Iterator var2 = eleRelations.iterator();

        while (var2.hasNext()) {
            Element eleRelation = (Element) var2.next();
            int consId = Integer.parseInt(eleRelation.getAttributeValue("name").substring(1));
            Constraint cons = (Constraint) this.problem.constraintInfo.get(consId);
            String pth = Paths.get(this.relationPath, eleRelation.getAttributeValue("name")).toString();
            long maxValue = -2147483648L;
            long minValue = 2147483647L;

            try {
                int idx = 0;
                Scanner scanner = new Scanner(new FileInputStream(pth));

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split("\\|");
                    String[] var17 = parts;
                    int var18 = parts.length;

                    for (int var19 = 0; var19 < var18; ++var19) {
                        String c = var17[var19];
                        if (!c.equals("")) {
                            long cost;
                            if (c.contains(".")) {
                                Constraint.SCALE = 1000000;
                                cost = (long) (Double.parseDouble(c) * (double) Constraint.SCALE);
                            } else {
                                cost = Long.parseLong(c);
                            }

                            maxValue = Long.max(maxValue, cost);
                            minValue = Long.min(minValue, cost);
                            if (sort) {
                                int index = cons.indexOld2New(idx++);
                                cons.data[index] = cost;
                            } else {
                                cons.data[idx++] = cost;
                            }
                        }
                    }
                }

                cons.maxValue = maxValue;
                cons.minValue = minValue;
            } catch (FileNotFoundException var21) {
                var21.printStackTrace();
            }
        }

    }
}
