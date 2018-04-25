package com.primeledger.higgs.pbft.common.network.api;


/**
 * @author hanson
 * @Date 2018/4/25
 * @Description:
 */

public interface ICommitConsensus<T> {

    boolean commit(T t);
}
