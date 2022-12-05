package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    private int gbfield;
    private Type gbfieldtype;
    private int afield;
    private Op what;
    private TupleDesc tupleDesc;
    private Map<Field, StringAggregator.AggResult> results = new LinkedHashMap<>();

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;
        this.tupleDesc = this.gbfield != Aggregator.NO_GROUPING ?
                new TupleDesc(new Type[]{gbfieldtype,Type.INT_TYPE}) :
                new TupleDesc(new Type[]{Type.INT_TYPE});
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Field field;
        if(gbfield != Aggregator.NO_GROUPING){
            field = tup.getField(gbfield);
        }else{
            field = Aggregator.EMPTY_FIELD;
        }
        results.computeIfAbsent(field,k -> new AggResult()).merge();
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public OpIterator iterator() {
        // some code goes here
        List<Tuple> tuples = new ArrayList<>();
        for(Map.Entry<Field,AggResult>entry:results.entrySet()){
            Tuple tuple = new Tuple(tupleDesc);
            if(gbfield != Aggregator.NO_GROUPING){
                tuple.setField(0,entry.getKey());
                tuple.setField(1,new IntField(entry.getValue().stringResult()));
            }else{
                tuple.setField(0,new IntField(entry.getValue().stringResult()));
            }
            tuples.add(tuple);
        }
        return new TupleIterator(tupleDesc,tuples);
    }

    private static class AggResult{
        private int count;

        public void merge(){
            count++;
        }

        public int stringResult(){
            return count;
        }
    }

}
