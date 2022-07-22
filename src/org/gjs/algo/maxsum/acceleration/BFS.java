package org.gjs.algo.maxsum.acceleration;

import org.infrastructure.core.Constraint;
import org.gjs.algo.maxsum.AbstractFunctionNode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class BFS extends AbstractFunctionNode {
    private final Map<Integer, Constraint> uninformedEst;
    private final Map<Integer, Map<Integer, Constraint[]>> informedEst;
    private long maxUtil;
    private final Map<Integer, Long> msgEst = new HashMap<>();
    private final Map<Integer, Map<Integer, int[]>> MaxEntryView;
    PriorityQueue<SearchNode> openList;
    private int[] domainLength;
    private final Map<Integer, Map<Integer, Long>> StoredMaxUtil;


    public BFS(Constraint function) {
        super(function);
        this.uninformedEst = new HashMap<>();
        this.informedEst = new HashMap<>();
        this.MaxEntryView = new HashMap<>();
        this.domainLength = new int[this.function.dimDomains.size()];
        for (int i = 0; i < this.function.dimDomains.size(); i++) {
            int curId = this.function.dimOrdering[i];
            domainLength[i] = this.function.dimDomains.get(curId);
        }
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
        openList = new PriorityQueue<>(100, new Comparator<SearchNode>() {
            @Override
            public int compare(SearchNode o1, SearchNode o2) {
                return Long.compare(o2.getValue(), o1.getValue());
            }
        });
    }

    @Override
    public long[] max(int id) {
        super.max(id);
        msgEst.clear();
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
        long norm = 0;

        for (int targetVal = 0; targetVal < result.length; targetVal++) {
            this.maxUtil = this.StoredMaxUtil.get(id).get(targetVal);
            int[] maxAssign = this.MaxEntryView.get(id).get(targetVal);
            this.currentCC++;
            for (int i = 0; i < this.function.dimOrdering.length; i++) {
                int curId = this.function.dimOrdering[i];
                if (curId != id) {
                    this.currentBasicOp++;
                    this.maxUtil += this.incomeMsg.get(curId)[maxAssign[i]];
                }
            }
            openList.clear();
            bfsExpand(null, id, targetVal);
            while (!openList.isEmpty()) {
                SearchNode bestNode = openList.poll();
                if (bestNode.expandable()) {
                    bfsExpand(bestNode, id, targetVal);
                } else {
                    maxUtil = this.function.eval(bestNode.assign) + bestNode.qSum;
                    break;
                }
            }

            result[targetVal] = this.maxUtil;
            norm += this.maxUtil;
        }
        norm = norm / result.length;
        for (int i = 0; i < result.length; i++) {
            result[i] -= norm;
        }
        return result;
    }

    private void bfsExpand(SearchNode parent, int target, int targetVal) {
        int depth = parent == null ? 0 : parent.getDepth() + 1;
        int curId = this.function.dimOrdering[depth];
        if (parent == null) {
            if (curId == target) {
                SearchNode searchNode = new SearchNode(new int[]{targetVal}, 0, 0);
                openList.add(searchNode);
                bfsExpand(searchNode, target, targetVal);
            } else {
                for (int i = 0; i < domainLength[depth]; i++) {
                    this.currentBasicOp++;
                    int[] assign = new int[]{i};
                    long qSum = this.incomeMsg.get(curId)[i];
                    long est = this.msgEst.get(curId) + informedEst.get(curId).get(target)[targetVal].eval(assign);
                    if (est + qSum > this.maxUtil) {
                        openList.add(new SearchNode(assign, qSum, est));
                    }
                }
            }
        } else {
            if (curId == target) {
                int[] assign = new int[depth + 1];
                System.arraycopy(parent.assign, 0, assign, 0, parent.assign.length);
                assign[assign.length - 1] = targetVal;
                SearchNode searchNode = new SearchNode(assign, parent.qSum, parent.est);
                if (searchNode.expandable()) {
                    bfsExpand(searchNode, target, targetVal);
                } else {
                    openList.add(searchNode);
                }
            } else {
                for (int i = 0; i < domainLength[depth]; i++) {
                    this.currentCC++;
                    this.currentBasicOp++;
                    int[] assign = new int[depth + 1];
                    System.arraycopy(parent.assign, 0, assign, 0, parent.assign.length);
                    assign[depth] = i;
                    long qSum = parent.qSum + this.incomeMsg.get(curId)[i];
                    Constraint funEst = (informedEst.containsKey(curId) && informedEst.get(curId).containsKey(target))
                            ? informedEst.get(curId).get(target)[targetVal]
                            : uninformedEst.get(curId);
                    // 新节点的est
                    long est;
                    if (depth == domainLength.length - 1) {
                        est = this.function.eval(assign);
                    } else {
                        est = msgEst.get(curId) + funEst.eval(assign);
                    }

                    if (est + qSum > this.maxUtil) {
                        openList.add(new SearchNode(assign, qSum, est));
                    }
                }
            }
        }
    }

    private class SearchNode {
        int[] assign;
        long qSum;
        long est;

        public SearchNode(int[] assign, long qSum, long est) {
            this.assign = assign;
            this.qSum = qSum;
            this.est = est;
        }

        public long getValue() {
            return qSum + est;
        }

        public int getDepth() {
            return assign.length - 1;
        }

        public boolean expandable() {
            return assign.length < function.dimDomains.size();
        }
    }
}
