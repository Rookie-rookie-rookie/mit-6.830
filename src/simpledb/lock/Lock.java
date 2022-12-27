package simpledb.lock;

import simpledb.transaction.TransactionId;

public class Lock {

    public enum TYPE{
        SHARE,
        EXCLUSIVE
    }

    private TYPE type;
    private TransactionId tid;
    public Lock(TransactionId tid,TYPE type){
        this.tid = tid;
        this.type = type;
    }

    public TransactionId getTid(){
        return this.tid;
    }

    public TYPE getType(){
        return this.type;
    }

    public void setType(TYPE type){
        this.type = type;
    }
}
