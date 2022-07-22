package org.gjs.algo.maxsum.acceleration.utility_digesters;

import org.infrastructure.core.Constraint;

import java.util.Set;

public class MeanUtilityDigest extends AbstractUtilityDigest{
    private long totalUtil;

    public MeanUtilityDigest(Constraint data, Set<Integer> fixedIndexes, int[] curAssign) {
        super(data, fixedIndexes, curAssign);
    }

    @Override
    protected void end() {
        this.weight = this.totalUtil / this.combinationNum;
    }

    @Override
    protected void process(int[] curAssign, long util, int idx) {
        this.maxUtil = Long.max(this.maxUtil, util);
        this.totalUtil += util;
    }
}
