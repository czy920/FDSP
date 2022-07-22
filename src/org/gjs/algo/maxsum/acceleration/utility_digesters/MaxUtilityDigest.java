package org.gjs.algo.maxsum.acceleration.utility_digesters;

import org.infrastructure.core.Constraint;

import java.util.Set;

public class MaxUtilityDigest extends AbstractUtilityDigest{
    public MaxUtilityDigest(Constraint data, Set<Integer> fixedIndexes, int[] curAssign) {
        super(data, fixedIndexes, curAssign);
    }

    @Override
    protected void end() {
        this.weight = this.maxUtil;
    }

    @Override
    protected void process(int[] curAssign, long util, int idx) {
        this.maxUtil = Long.max(this.maxUtil, util);
    }
}
