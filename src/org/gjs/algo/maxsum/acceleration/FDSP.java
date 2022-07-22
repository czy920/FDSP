package org.gjs.algo.maxsum.acceleration;

import org.infrastructure.core.Constraint;
import org.gjs.algo.maxsum.AbstractFunctionNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FDSP extends AbstractFunctionNode {
    private final Map<Integer, Constraint> uninformedEst;
    private final Map<Integer, Map<Integer, Constraint[]>> informedEst;
    private long maxUtil;
    private final Map<Integer, Long> msgEst = new HashMap<>();
    private final Map<Integer, Map<Integer, int[]>> MaxEntryView;
    private final Map<Integer, Map<Integer, Long>> StoredMaxUtil;

    public FDSP(Constraint function) {
        super(function);
        this.uninformedEst = new HashMap<>();
        this.informedEst = new HashMap<>();
        this.MaxEntryView = new HashMap<>();
        this.StoredMaxUtil = new HashMap<>();
        for (int i = 0; i < this.function.dimOrdering.length; i++) {
            int id = this.function.dimOrdering[i];
            this.MaxEntryView.put(id, new HashMap<>(this.function.dimDomains.get(id)));
            this.StoredMaxUtil.put(id, new HashMap<>(this.function.dimDomains.get(id)));
        }
    }

    @Override
    public void init() {
        super.init();
        Constraint data = this.function;
        for (int i = this.function.dimDomains.size() - 2; i >= 0; i--) {
            int curId = this.function.dimOrdering[i];
            int succId = this.function.dimOrdering[i + 1];
            data = data.max(succId);
            this.uninformedEst.put(curId, data);
            this.informedEst.put(curId, new HashMap<>());
        }

        for (int i = this.function.dimDomains.size() - 2; i >= 0; i--) {
            int curId = this.function.dimOrdering[i];
            for (int j = this.function.dimDomains.size() - 1; j > i; j--) {
                int informerId = this.function.dimOrdering[j];
                Constraint[] allData = new Constraint[this.function.dimDomains.get(informerId)];
                for (int informVal = 0; informVal < this.function.dimDomains.get(informerId); informVal++) {
                    data = this.uninformedEst.getOrDefault(informerId, this.function);
                    data = data.conditionOn(informerId, informVal);
                    for (int k = j - 1; k > i; k--) {
                        data = data.max(this.function.dimOrdering[k]);
                    }
                    allData[informVal] = data;
                    if (i == 0) {
                        int MaxId = 0;
                        long MaxValue = Long.MIN_VALUE;
                        for (int k = 0; k < data.data.length; k++) {
                            if (data.data[k] > MaxValue) {
                                MaxId = data.indexes[k];
                                MaxValue = data.data[k];
                            }
                        }
                        Map<Integer, int[]> bestEntry = this.MaxEntryView.get(informerId);
                        Map<Integer, Long> bestUtil = this.StoredMaxUtil.get(informerId);
                        bestEntry.put(informVal, this.function.index2Assign(MaxId));
                        bestUtil.put(informVal, MaxValue);
                        this.MaxEntryView.put(informerId, bestEntry);
                        this.StoredMaxUtil.put(informerId, bestUtil);
                    }
                }
                this.informedEst.get(curId).put(informerId, allData);
            }
        }
        int firstId = this.function.dimOrdering[0];
        Map<Integer, int[]> bestEntry = this.MaxEntryView.get(firstId);
        Map<Integer, Long> bestUtil = this.StoredMaxUtil.get(firstId);
        for (int i = 0; i < this.uninformedEst.get(firstId).indexes.length; i++) {
            int[] maxAssign = this.function.index2Assign(this.uninformedEst.get(firstId).indexes[i]);
            bestEntry.put(i, maxAssign);
            bestUtil.put(i, this.function.eval(maxAssign));
        }
        this.MaxEntryView.put(firstId, bestEntry);
        this.StoredMaxUtil.put(firstId, bestUtil);
    }

    @Override
    public long[] max(int id) {
        super.max(id);
        msgEst.clear();
        int[] curAssign = new int[this.function.dimDomains.size()];
        int[] domainLength = new int[this.function.dimDomains.size()];
        int skipIdx = 0;
        for (int i = 0; i < this.function.dimDomains.size(); i++) {
            int curId = this.function.dimOrdering[i];
            curAssign[i] = 0;
            domainLength[i] = this.function.dimDomains.get(curId);
            if (curId == id) {
                skipIdx = i;
            }
        }
        long acc = 0;
        for (int i = this.function.dimDomains.size() - 1; i > 0; i--) {
            int prevId = this.function.dimOrdering[i - 1];
            int curId = this.function.dimOrdering[i];
            if (curId != id) {
                long maxUtil = Long.MIN_VALUE;
                for (long val : this.incomeMsg.get(curId)) {
                    maxUtil = Long.max(maxUtil, val);
                }
                acc += maxUtil;
            }
            if (prevId != id)
                this.msgEst.put(prevId, acc);
        }

        long[] result = new long[this.function.dimDomains.get(id)];
        long norm = 0;
        for (int val = 0; val < result.length; val++) {
            int[] maxAssign = this.MaxEntryView.get(id).get(val);
            this.maxUtil = this.StoredMaxUtil.get(id).get(val);
            for (int i = 0; i < this.function.dimOrdering.length; i++) {
                int curId = this.function.dimOrdering[i];
                this.currentCC++;
                if (curId != id) {
                    this.maxUtil += this.incomeMsg.get(curId)[maxAssign[i]];
                    this.currentBasicOp++;
                }
            }
            curAssign = new int[this.function.dimDomains.size()];
            Arrays.fill(curAssign, 0);
            curAssign[skipIdx] = val;
            rec(curAssign, domainLength, 0, skipIdx, 0);
            result[val] = this.maxUtil;
            norm += this.maxUtil;
        }
        norm = norm / result.length;
        for (int i = 0; i < result.length; i++) {
            result[i] -= norm;
        }

        return result;
    }

    private void rec(int[] curAssign, int[] domainLength, int curIdx, int skipIdx, long partialUtil) {
        boolean last = curIdx == domainLength.length - 1;
        int curId = this.function.dimOrdering[curIdx];
        if (last) {
            if (curIdx == skipIdx) {
                long util = partialUtil + this.function.eval(curAssign);
                this.currentCC++;
                maxUtil = Long.max(maxUtil, util);
                return;
            }
            for (int val = 0; val < this.function.dimDomains.get(curId); val++) {
                this.currentBasicOp++;
                this.currentCC++;
                curAssign[curIdx] = val;
                long util = partialUtil + this.function.eval(curAssign);
                util += this.incomeMsg.get(curId)[val];
                if (util > maxUtil) {
                    maxUtil = util;
                }

            }
        } else {
            if (curIdx == skipIdx) {
                rec(curAssign, domainLength, curIdx + 1, skipIdx, partialUtil);
                return;
            }
            int target = this.function.dimOrdering[skipIdx];
            for (int val = 0; val < this.function.dimDomains.get(curId); val++) {
                curAssign[curIdx] = val;
                long util = partialUtil + this.incomeMsg.get(curId)[val];
                long ub = util + msgEst.get(curId);
                this.currentBasicOp++;
                if (this.informedEst.get(curId).containsKey(target)) {
                    ub += this.informedEst.get(curId).get(target)[curAssign[skipIdx]].eval(curAssign);
                } else {
                    ub += this.uninformedEst.get(curId).eval(curAssign);
                }
                if (ub > maxUtil) {
                    rec(curAssign, domainLength, curIdx + 1, skipIdx, util);
                }
            }
        }
    }

    private long rec(int[] curAssign, long lb, int[] domainLength, int curIdx, int skipIdx, long partialUtil) {
        boolean last = curIdx == domainLength.length - 1;
        long maxUtil = Long.MIN_VALUE;
        int curId = this.function.dimOrdering[curIdx];
        if (last) {
            if (curIdx == skipIdx) {
                long util = partialUtil + this.function.eval(curAssign);
                this.currentCC++;
                maxUtil = util;
                return maxUtil;
            }
            for (int val = 0; val < this.function.dimDomains.get(curId); val++) {
                this.currentBasicOp++;
                this.currentCC++;
                curAssign[curIdx] = val;
                long util = partialUtil + this.function.eval(curAssign);
                util += this.incomeMsg.get(curId)[val];
                maxUtil = Long.max(maxUtil, util);
            }
        } else {
            if (curIdx == skipIdx) {
                return rec(curAssign, lb, domainLength, curIdx + 1, skipIdx, partialUtil);
            }
            int target = this.function.dimOrdering[skipIdx];
            for (int val = 0; val < this.function.dimDomains.get(curId); val++) {
                curAssign[curIdx] = val;
                long util = partialUtil + this.incomeMsg.get(curId)[val];
                long ub = util + msgEst.get(curId);
                this.currentBasicOp++;
                if (this.informedEst.get(curId).containsKey(target)) {
                    ub += this.informedEst.get(curId).get(target)[curAssign[skipIdx]].eval(curAssign);
                } else {
                    ub += this.uninformedEst.get(curId).eval(curAssign);
                }
                if (ub > lb) {
                    maxUtil = Long.max(lb, Long.max(maxUtil, rec(curAssign, lb, domainLength, curIdx + 1, skipIdx, util)));
                    lb = maxUtil;
                }
            }
        }
        return maxUtil;
    }

}
