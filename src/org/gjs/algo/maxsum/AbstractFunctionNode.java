package org.gjs.algo.maxsum;

import org.infrastructure.core.Constraint;
import org.gjs.algo.maxsum.acceleration.*;

import java.util.*;

public abstract class AbstractFunctionNode {
    public Constraint function;
    public Map<Integer, long[]> incomeMsg = new HashMap<>();
    protected long vanillaBasicOp = 0;
    protected long currentBasicOp = 0;
    private long vbo;
    private long vcc;
    public long vanillaCC;
    public long currentCC;


    public AbstractFunctionNode(Constraint function){
        this.function = function;
        this.vbo = 1;
        for (int var : this.function.dimOrdering){
            int len = this.function.dimDomains.get(var);
            long[] msg = new long[len];
            Arrays.fill(msg, 0);
            this.vbo *= len;
            this.incomeMsg.put(var, msg);
        }
        this.vcc = this.vbo;
        this.vbo *= (this.function.dimDomains.size() - 1);
    }

    public long[] max(int id){
        this.vanillaBasicOp += this.vbo;
        this.vanillaCC += this.vcc;
        return null;
    }

    public void init() {

    }

    public void updateMsg(int id, long[] msg){
        this.incomeMsg.put(id, msg);
    }

    public static AbstractFunctionNode createFunctionNodes(Constraint function, String algo, String weightedCriterion, int stepSize, boolean dynamic, int sortingDepth,String type){
        if ("GDP".equalsIgnoreCase(algo)){
            return new GDP(function, dynamic);
        }
        else if ("FDSP".equalsIgnoreCase(algo)){
            return new FDSP(function);
        }
        else if ("ART-GD2P".equalsIgnoreCase(algo)){
            return new ART_GD2P(function, stepSize);
        }
        else if ("BFS".equalsIgnoreCase(algo)){
            return new BFS(function);
        }
        else if ("CONC".equalsIgnoreCase(algo)){
            return new CONC(function,type);
        }
        else if ("PTS".equalsIgnoreCase(algo)){
            return new PTS(function, sortingDepth, stepSize, weightedCriterion);
        }
        return new Vanilla(function);
    }
}
