//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infrastructure.core;

import org.infrastructure.utilities.MultiDimDataUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Constraint {
    public static int SCALE = 1;
    private static int nextId = 0;
    public long[] data;
    public Map<Integer, Integer> dimDomains;
    public int[] dimOrdering;
    private Map<Integer, Integer> assign;
    public Map<Integer, Integer> weight;
    private int hostId;
    private String id;
    public long maxValue;
    public long minValue;
    public int[] indexes;
    public Map<Integer, Integer> weight_old;
    private int[] dimOrdering_old;


    public Constraint(Map<Integer, Integer> dimDomains, int[] dimOrdering) {
        this.dimDomains = dimDomains;
        this.dimOrdering = dimOrdering;
        this.data = new long[this.totalLength()];
        this.indexes = new int[this.totalLength()];
        this.assign = new HashMap<Integer, Integer>();
        this.computeWeight();
        this.hostId = dimOrdering[0];
        this.id = Arrays.toString(dimOrdering) + "/" + this.getNextId();
        for (int i = 0; i < this.data.length; i++) {
            indexes[i] = i;
        }
    }


    private synchronized int getNextId() {
        ++nextId;
        return nextId;
    }

    public String getId() {
        return this.id;
    }

    public int getHostId() {
        return this.hostId;
    }

    private void computeWeight() {
        int weight = 1;
        this.weight = new HashMap<>();

        for (int i = this.dimOrdering.length - 1; i >= 0; --i) {
            this.weight.put(this.dimOrdering[i], weight);
            weight *= (Integer) this.dimDomains.get(this.dimOrdering[i]);
        }

    }
    private void computeWeight_raw() {
        int weight = 1;
        this.weight_old = new HashMap<>();

        for (int i = dimOrdering_old.length - 1; i >= 0; --i) {
            this.weight_old.put(dimOrdering_old[i], weight);
            weight *= (Integer) this.dimDomains.get(dimOrdering_old[i]);
        }

    }


    private int totalLength() {
        int i = 1;

        int d;
        for (Iterator var2 = this.dimDomains.values().iterator(); var2.hasNext(); i *= d) {
            d = (Integer) var2.next();
        }

        return i;
    }

    public int getNeighbor(int curId) {
        assert this.dimOrdering.length == 2;

        return this.dimOrdering[0] == curId ? this.dimOrdering[1] : this.dimOrdering[0];
    }

    public synchronized long eval(int curId, int i, int j) {
        assert this.dimOrdering.length == 2;

        int otherId = curId == this.dimOrdering[0] ? this.dimOrdering[1] : this.dimOrdering[0];
        this.assign.put(curId, i);
        this.assign.put(otherId, j);
        return this.eval(this.assign);
    }

    public long eval(Map<Integer, Integer> assign) {
        return this.data[this.getIndex(assign)];
    }

    public long eval(int[] assign) {
        int idx = this.getIndex(assign);
        return this.data[idx];
    }

    public int getIndex(int[] assign) {
        int idx = 0;

        for (int i = 0; i < this.dimOrdering.length; ++i) {
            idx += (Integer) this.weight.get(this.dimOrdering[i]) * assign[i];
        }

        return idx;
    }

    public int getIndex(Map<Integer, Integer> assign) {
        int idx = 0;
        int[] var3 = this.dimOrdering;
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            int dim = var3[var5];
            idx += (Integer) this.weight.get(dim) * (Integer) assign.get(dim);
        }

        return idx;
    }

    public void setDimOrdering_old(int[] dimOrdering_old){
        this.dimOrdering_old=dimOrdering_old;
        computeWeight_raw();
    }

    public void setValue(Map<Integer, Integer> assign, long data) {
        this.data[this.getIndex(assign)] = data;
    }

    private Constraint project(int id, String operator) {
        int skipIdx = 0;
        int[] curAssign = new int[this.dimDomains.size()];
        int[] domainLength = new int[this.dimDomains.size()];
        int[] remainingVars = new int[this.dimDomains.size() - 1];
        Map<Integer, Integer> remainingDomainLength = new HashMap();
        int idx = 0;

        for (int i = 0; i < this.dimDomains.size(); ++i) {
            int curId = this.dimOrdering[i];
            curAssign[i] = 0;
            domainLength[i] = (Integer) this.dimDomains.get(curId);
            if (curId == id) {
                skipIdx = i;
            } else {
                remainingVars[idx++] = curId;
                remainingDomainLength.put(curId, (Integer) this.dimDomains.get(curId));
            }
        }

        Constraint data = new Constraint(remainingDomainLength, remainingVars);

        do {
            long best = 0L;
            int bestIndex = 0;
            if (operator.equals("max")) {
                best = -9223372036854775808L;
            } else if (operator.equals("min")) {
                best = 9223372036854775807L;
            }

            int i;
            for (i = 0; i < domainLength[skipIdx]; ++i) {
                curAssign[skipIdx] = i;
                long curValue = this.eval(curAssign);
                int curIndex = this.indexes[assign2Index(curAssign)];
                if (operator.equals("max")) {
                    if (curValue > best) {
                        best = curValue;
                        bestIndex = curIndex;
                    }
//                    best = Long.max(curValue, best);
                } else if (operator.equals("min")) {
                    if (curValue < best) {
                        best = curValue;
                        bestIndex = curIndex;
                    }
//                    best = Long.min(best, curValue);
                }
            }

            idx = 0;

            for (i = 0; i < curAssign.length; ++i) {
                if (i != skipIdx) {
                    int curId = this.dimOrdering[i];
                    idx += (Integer) data.weight.get(curId) * curAssign[i];
                }
            }

            data.data[idx] = best;
            data.indexes[idx]=bestIndex;
        } while (MultiDimDataUtils.next(curAssign, domainLength, skipIdx));

        return data;
    }

    public Constraint max(int id) {
        return this.project(id, "max");
    }

    public Constraint conditionOn(int id, int val) {
        int skipIdx = 0;
        int[] curAssign = new int[this.dimDomains.size()];
        int[] domainLength = new int[this.dimDomains.size()];
        int[] remainingVars = new int[this.dimDomains.size() - 1];
        Map<Integer, Integer> remainingDomainLength = new HashMap<>();
        int idx = 0;

        for (int i = 0; i < this.dimDomains.size(); ++i) {
            int curId = this.dimOrdering[i];
            curAssign[i] = 0;
            domainLength[i] = (Integer) this.dimDomains.get(curId);
            if (curId == id) {
                skipIdx = i;   // find the conditional id
            } else {
                remainingVars[idx++] = curId;
                remainingDomainLength.put(curId, (Integer) this.dimDomains.get(curId));
            }
        }

        Constraint data = new Constraint(remainingDomainLength, remainingVars);

        do {
            curAssign[skipIdx] = val;
            long best = this.eval(curAssign);
            idx = 0;

            for (int i = 0; i < curAssign.length; ++i) {
                if (i != skipIdx) {
                    int curId = this.dimOrdering[i];
                    idx += (Integer) data.weight.get(curId) * curAssign[i];
                }
            }
            data.data[idx] = best;
            data.indexes[idx] = this.indexes[this.assign2Index(curAssign)];
        } while (MultiDimDataUtils.next(curAssign, domainLength, skipIdx));

        return data;
    }


    public int[] index2Assign(int index) {
        int[] assign = new int[this.dimOrdering.length];
        int tmpIndex = index;
        for (int i = 0; i < assign.length; i++) {
            int currId = this.dimOrdering[i];
            int currIdx = tmpIndex / this.weight.get(currId);
            assign[i] = currIdx;
            tmpIndex -= this.weight.get(currId) * currIdx;
        }
        return assign;
    }

    public int indexOld2New(int index){
        int[] assign = index2Assign_old(index);
        Map<Integer,Integer> ass = new HashMap<>();
        for (int i = 0; i < assign.length; i++) {
            ass.put(dimOrdering_old[i],assign[i]);
        }
        return getIndex(ass);
    }

    public int[] index2Assign_old(int index) {
        int[] assign = new int[this.dimOrdering_old.length];
        int tmpIndex = index;
        for (int i = 0; i < assign.length; i++) {
            int currId = this.dimOrdering_old[i];
            int currIdx = tmpIndex / this.weight_old.get(currId);
            assign[i] = currIdx;
            tmpIndex -= this.weight_old.get(currId) * currIdx;
        }
        return assign;
    }

    private int assign2Index(int[] assign) {
        int idx = 0;
        for (int i = 0; i < assign.length; i++) {
            int curId = this.dimOrdering[i];
            idx += assign[i] * this.weight.get(curId);
        }
        return idx;
    }

}
