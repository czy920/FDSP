//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infrastructure.parser;

import org.infrastructure.core.Constraint;
import org.infrastructure.core.Problem;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class GeneralParser {
    private String filePath;
    private AbstractParser parser;
    private boolean sort;

    public GeneralParser(String filePath) {
        this.filePath = filePath;
    }

    public void setParser(AbstractParser parser) {
        this.parser = parser;
    }

    public Problem parse(boolean sort) {
        Problem problem = new Problem();
        File f = new File(this.filePath);
        this.sort = sort;

        try {
            Element root = (new SAXBuilder()).build(f).getRootElement();
            String type = root.getChild("presentation").getAttributeValue("type");
            if (this.parser == null) {
                if (type.equals("NDCOP")) {
                    this.parser = new NAryParser(root, problem, Paths.get(f.getParent(), "relations", f.getName().substring(0, f.getName().lastIndexOf(46))).toString());
                } else {
                    this.parser = new BinaryParser(root, problem);
                }
            }

            List<Element> agents = root.getChild("agents").getChildren();
            Iterator var6 = agents.iterator();

            while (var6.hasNext()) {
                Element eleAgent = (Element) var6.next();
                problem.agentId.add(Integer.valueOf(eleAgent.getAttributeValue("id")));
            }

            var6 = problem.agentId.iterator();

            while (var6.hasNext()) {
                int id = (Integer) var6.next();
                problem.neighbours.put(id, new HashSet());
                problem.constraints.put(id, new ArrayList());
            }

            Map<String, Integer> domains = new HashMap();
            List<Element> eleDomains = root.getChild("domains").getChildren();
            Iterator var8 = eleDomains.iterator();

            while (var8.hasNext()) {
                Element eleDomain = (Element) var8.next();
                domains.put(eleDomain.getAttributeValue("name"), Integer.parseInt(eleDomain.getAttributeValue("nbValues")));
            }

            List<Element> eleVariables = root.getChild("variables").getChildren();
            Iterator var32 = eleVariables.iterator();

            while (var32.hasNext()) {
                Element eleVariable = (Element) var32.next();
                int agentId = Integer.parseInt(eleVariable.getAttributeValue("agent").substring(1));
                String domainId = eleVariable.getAttributeValue("domain");
                problem.domainSize.put(agentId, (Integer) domains.get(domainId));
            }

            List<Element> eleConstrains = root.getChild("constraints").getChildren();
            Iterator var34 = eleConstrains.iterator();

            while (var34.hasNext()) {
                Element eleConstraint = (Element) var34.next();
                int id = Integer.parseInt(eleConstraint.getAttributeValue("name").substring(1));
                String[] scp = eleConstraint.getAttributeValue("scope").split(" ");
                int[] dimOrdering = new int[scp.length];
                Map<Integer, Integer> domainSize = new HashMap<>();
                for (int i = 0; i < scp.length; ++i) {
                    String dimVar = scp[i].split("\\.")[0];
                    dimOrdering[i] = Integer.parseInt(dimVar.substring(1));
                    domainSize.put(dimOrdering[i], (Integer) problem.domainSize.get(dimOrdering[i]));
                }
                Constraint constraint;
                if (sort) {
                    int[] dimOrdering_old = new int[scp.length];
                    List<Map.Entry<Integer, Integer>> list = new ArrayList<>(domainSize.entrySet());
                    list.sort(new Comparator<Map.Entry<Integer, Integer>>() {
                        @Override
                        public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                            return o1.getValue() - o2.getValue();
                        }
                    });
                    for (int i = 0; i < list.size(); i++) {
                        dimOrdering_old[i] = list.get(i).getKey();
                    }
                    constraint = new Constraint(domainSize, dimOrdering_old);
                    constraint.setDimOrdering_old(dimOrdering);

                } else {
                    constraint = new Constraint(domainSize, dimOrdering);
                }

                int[] var38 = dimOrdering;
                int var18 = dimOrdering.length;

                for (int var19 = 0; var19 < var18; ++var19) {
                    int agentId = var38[var19];
                    ((List) problem.constraints.get(agentId)).add(constraint);
                    Set<Integer> neighbors = (Set) problem.neighbours.get(agentId);
                    int[] var22 = dimOrdering;
                    int var23 = dimOrdering.length;

                    for (int var24 = 0; var24 < var23; ++var24) {
                        int i = var22[var24];
                        if (i != agentId) {
                            neighbors.add(i);
                        }
                    }
                }

                problem.constraintInfo.put(id, constraint);
            }

            this.parser.parse(sort);
        } catch (JDOMException var26) {
            var26.printStackTrace();
        } catch (IOException var27) {
            var27.printStackTrace();
        }

        return problem;
    }
}
