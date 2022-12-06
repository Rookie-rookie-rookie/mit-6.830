package simpledb.storage;

import simpledb.common.DbException;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HeapFileDBIterator implements DbFileIterator{

    private final HeapFile heapFile;
    private final TransactionId tid;
    private final int tableId;
    private Iterator<Tuple> tupleIterator;
    private int pageCur = 0;

    public HeapFileDBIterator(HeapFile heapFile,TransactionId tid){
        this.heapFile = heapFile;
        this.tid = tid;
        this.tableId = heapFile.getId();
    }
    @Override
    public void open() throws DbException, TransactionAbortedException {

    }

    @Override
    public boolean hasNext() throws DbException, TransactionAbortedException {
        return false;
    }

    @Override
    public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
        return null;
    }

    @Override
    public void rewind() throws DbException, TransactionAbortedException {

    }

    @Override
    public void close() {
        tupleIterator = null;
    }
}
