package org.gjs.algo.maxsum.acceleration;

import org.infrastructure.core.Constraint;
import org.gjs.algo.maxsum.AbstractFunctionNode;

import java.util.*;

public class CONC extends AbstractFunctionNode {
    private final Map<Integer, Constraint> uninformedEst;
    private final Map<Integer, Map<Integer, Constraint[]>> informedEst;
    private long maxUtil;
    private List<Integer> trimmedOrderedIdx;
    private final Map<Integer, Long> msgEst = new HashMap<>();
    public final String type;
    private int K = 1;
    private long sharedLB;
    private final Map<Integer, Map<Integer, int[]>> MaxEntryView;
    private int skipIdx;
    public PriorityQueue<SearchProcess> orderedProcess;
    private final Map<Integer, Map<Integer, Long>> StoredMaxUtil;

    public CONC(Constraint function, String type) {
        super(function);
        this.uninformedEst = new HashMap<>();
        this.informedEst = new HashMap<>();
        this.type = type;
        this.MaxEntryView = new HashMap<>();
        this.StoredMaxUtil = new HashMap<>();
        for (int i = 0; i < this.function.dimOrdering.length; i++) {
            int id = this.function.dimOrdering[i];
            this.MaxEntryView.put(id, new HashMap<>(this.function.dimDomains.get(id)));
            this.StoredMaxUtil.put(id, new HashMap<>(this.function.dimDomains.get(id)));
        }
        this.K = Integer.min(K, this.function.dimOrdering.length - 1);
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
        this.trimmedOrderedIdx = new ArrayList<>();
        for (int i = 0; i < this.function.dimDomains.size(); i++) {
            int curId = this.function.dimOrdering[i];
            curAssign[i] = -1;
            if (curId == id) {
                skipIdx = i;
            } else {
                trimmedOrderedIdx.add(i);
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
            if (prevId != id) {
                this.msgEst.put(prevId, acc);
            }
        }

        long[] result = new long[this.function.dimDomains.get(id)];
        List<Integer> splitIdxList = new LinkedList<>();
        for (int i = 0; i < K; i++) {
            splitIdxList.add(this.trimmedOrderedIdx.get(0));
            trimmedOrderedIdx.remove(0);
        }
        long norm = 0;
        for (int targetVal = 0; targetVal < this.function.dimDomains.get(id); targetVal++) {
            int[] maxAssign = this.MaxEntryView.get(id).get(targetVal);
            sharedLB = this.StoredMaxUtil.get(id).get(targetVal);
            for (int i = 0; i < this.function.dimOrdering.length; i++) {
                int curId = this.function.dimOrdering[i];
                if (curId != id) {
                    sharedLB += this.incomeMsg.get(curId)[maxAssign[i]];
                    this.currentBasicOp++;
                    this.currentCC++;
                }
            }
            orderedProcess = new PriorityQueue<>((t0, t1) -> {
                return Long.compare(t1.getUb(), t0.getUb());
            });
            curAssign[skipIdx] = targetVal;
            int splitIdx = splitIdxList.get(0);
            int splitId = this.function.dimOrdering[splitIdx];
            for (int i = 0; i < this.function.dimDomains.get(splitId); i++) {
                curAssign[splitIdx] = i;
                currentCC++;
                SearchProcess sp = new SearchProcess(splitIdxList, id, targetVal, curAssign.clone());
                if (sp.getUb() > sharedLB) {
                    orderedProcess.add(sp);
                }
            }

            while (!orderedProcess.isEmpty()) {
                SearchProcess process = orderedProcess.poll();
                if ("UPDATE".equals(type)) {
                    boolean flag;
                    flag = process.updateExplore();
                    if (flag) {
                        orderedProcess.add(process);
                    }
                } else {
                    boolean flag;
                    flag = process.stepExplore();
                    if (flag) {
                        orderedProcess.add(process);
                    }
                }

            }

            result[targetVal] = sharedLB;
            norm += sharedLB;
        }
        norm = norm / result.length;
        for (int i = 0; i < result.length; i++) {
            result[i] -= norm;
        }
        return result;
    }


    private class SearchProcess {
        private final int target;
        private final int targetVal;
        private int[] assign;
        private long partialUtil = 0;
        private int depth = 0;
        private final List<Integer> splitIdxList;


        public SearchProcess(List<Integer> splitIdxList, int target, int targetVal, int[] assign) {
            this.splitIdxList = splitIdxList;
            this.target = target;
            this.targetVal = targetVal;
            this.assign = assign;
            for (int splitIdx : splitIdxList) {
                this.partialUtil += incomeMsg.get(function.dimOrdering[splitIdx])[assign[splitIdx]];
            }
        }

        long ub = -1;

        public long getUb() {
            if (ub == -1) {
                int curId = function.dimOrdering[splitIdxList.get(splitIdxList.size() - 1)];
                Constraint funEst = (informedEst.containsKey(curId) && informedEst.get(curId).containsKey(target)) ?
                        informedEst.get(curId).get(target)[targetVal] :
                        uninformedEst.get(curId);
                if (trimmedOrderedIdx.size() == 0) {
                    ub = function.eval(assign) + partialUtil;
                    return ub;
                }
                ub = partialUtil + funEst.eval(assign) + msgEst.get(curId);
            }
            return ub;
        }


        boolean stepExplore() {
            if (trimmedOrderedIdx.size() == 0) {
                sharedLB = Long.max(sharedLB, partialUtil + function.eval(assign));
                return false;
            }
            if (!terminate()) {
                // 1 level
                long tmpPartialUtil = partialUtil;
                if (trimmedOrderedIdx.size() == 1) {
                    int curIdx = trimmedOrderedIdx.get(depth);
                    int curId = function.dimOrdering[curIdx];
                    for (int i = 0; i < function.dimDomains.get(curId); i++) {
                        assign[curIdx] = i;
                        long util = partialUtil + incomeMsg.get(curId)[i] + function.eval(assign);
                        CONC.this.currentBasicOp++;
                        sharedLB = Long.max(sharedLB, util);
                    }
                    return false;
                }
                // multiple levels
                while (depth < trimmedOrderedIdx.size()) {
                    int curIdx = trimmedOrderedIdx.get(depth);
                    int curId = function.dimOrdering[curIdx];
                    boolean forward = false;
                    boolean updated = false;
                    // last level
                    if (depth == trimmedOrderedIdx.size() - 1) {
                        for (int val = 0; val < function.dimDomains.get(curId); val++) {
                            assign[curIdx] = val;
                            long util = tmpPartialUtil + incomeMsg.get(curId)[val] + function.eval(assign);
                            if (util > sharedLB) {
                                sharedLB = util;
                                updated = true;
                            }
                        }
                        if (updated) {
                            break;
                        } else {
                            depth -= 1;
                            assign[curIdx] = -1;
                        }
                    } else {
                        for (int val = assign[curIdx]; val < function.dimDomains.get(curId); val++) {
                            if (val == -1) {
                                val += 1;
                                tmpPartialUtil += incomeMsg.get(curId)[val];    // first time
                            }
                            assign[curIdx] = val;
                            CONC.this.currentBasicOp++;
                            Constraint funEst = (informedEst.containsKey(curId) &&
                                    informedEst.get(curId).containsKey(target)) ?
                                    informedEst.get(curId).get(target)[targetVal] : uninformedEst.get(curId);
                            long ub = funEst.eval(assign) + partialUtil + msgEst.get(curId);
                            if (ub > sharedLB) {
                                forward = true;
                                break;
                            }
                        }
                        if (forward) {
                            depth += 1;
                        } else {
                            return false;
                        }
                    }
                }
                depth = 0;
                tmpPartialUtil = partialUtil;
                int curIdx = trimmedOrderedIdx.get(depth);
                int curId = function.dimOrdering[curIdx];
                boolean found = false;
                boolean forward = false;
                while (depth < trimmedOrderedIdx.size() - 1) {
                    curIdx = trimmedOrderedIdx.get(depth);
                    curId = function.dimOrdering[curIdx];
                    tmpPartialUtil += incomeMsg.get(curId)[assign[curIdx]];
                    if (assign[curIdx] < function.dimDomains.get(curId)) {
                        found = true;
                        break;
                    } else {
                        depth += 1;
                    }
                }
                if (!found) {
                    return false;
                }
                for (int i = assign[curIdx] + 1; i < function.dimDomains.get(curId); i++) {
                    if (assign[curIdx] == -1) {
                        tmpPartialUtil = incomeMsg.get(curId)[assign[curIdx] + 1];
                    } else {
                        tmpPartialUtil -= incomeMsg.get(curId)[assign[curIdx]] + incomeMsg.get(curId)[i];
                    }
                    assign[curIdx] += i;
                    Constraint funEst = (informedEst.containsKey(curId) &&
                            informedEst.get(curId).containsKey(target)) ?
                            informedEst.get(curId).get(target)[targetVal] : uninformedEst.get(curId);
                    long ub = funEst.eval(assign) + msgEst.get(curId) + tmpPartialUtil;
                    if (ub > sharedLB) {
                        depth += 1;
                        forward = true;
                        this.ub = ub;
                        break;
                    }
                }
                return forward;
            } else {
                return false;// finished
            }
        }


        boolean terminate() {
            int firstVar = trimmedOrderedIdx.get(0);
            return assign[firstVar] == function.dimDomains.get(function.dimOrdering[firstVar]);
        }

        boolean updateExplore() {
            if (trimmedOrderedIdx.size() == 0) {
                sharedLB = Long.max(sharedLB, partialUtil + function.eval(assign));
                return false;
            }
            if (depth < 0) {
                return false;
            }
            while (true) {
                int curIdx = trimmedOrderedIdx.get(depth);
                int curId = function.dimOrdering[curIdx];
                int val = assign[curIdx];
                if (val != -1) {
                    partialUtil -= incomeMsg.get(curId)[val];
                }
                val++;
                boolean forward = false;
                for (int i = val; i < function.dimDomains.get(curId); i++) {
                    assign[curIdx] = i;
                    ub = partialUtil + incomeMsg.get(curId)[i];
                    CONC.this.currentBasicOp++;
                    if (depth < trimmedOrderedIdx.size() - 1) {
                        ub += msgEst.get(curId);
                        Constraint funEst = (informedEst.containsKey(curId) &&
                                informedEst.get(curId).containsKey(target)) ?
                                informedEst.get(curId).get(target)[targetVal] : uninformedEst.get(curId);
                        ub += funEst.eval(assign);

                    } else {
                        ub += function.eval(assign);
                        CONC.this.currentCC++;
                    }

                    if (ub > sharedLB) {
                        if (depth == trimmedOrderedIdx.size() - 1) {
                            sharedLB = ub;
                        }
                        forward = true;
                        break;
                    }
                }
                if (!forward) {
                    depth--;
                    assign[curIdx] = -1;
                    if (depth < 0) {
                        return false;
                    }
                } else {
                    partialUtil += incomeMsg.get(curId)[assign[curIdx]];
                    if (depth < trimmedOrderedIdx.size() - 1) {
                        depth++;
                    }
                    return true;
                }
            }
        }
    }
}
