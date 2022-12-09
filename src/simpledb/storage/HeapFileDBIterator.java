package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Permissions;
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
        if(pageCur >= heapFile.numPages()){
            return;
        }
        PageId pageId = new HeapPageId(tableId, pageCur);
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pageId, Permissions.READ_ONLY);
        tupleIterator = page.iterator();
    }

    @Override
    public boolean hasNext() throws DbException, TransactionAbortedException {
        if(tupleIterator == null){
            return false;
        }
        while (true){
            if(tupleIterator.hasNext()){
                return true;
            }
            pageCur++;
            if(pageCur >= heapFile.numPages()){
                tupleIterator = null;
                return false;
            }
            PageId pageId = new HeapPageId(tableId,pageCur);
            HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid,pageId,Permissions.READ_ONLY);
            tupleIterator = page.iterator();
        }
    }

    @Override
    public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
        if(tupleIterator == null){
            return null;
        }
        while (true){
            if(tupleIterator.hasNext()){
                return tupleIterator.next();
            }
            pageCur++;
            if(pageCur >= heapFile.numPages()){
                tupleIterator = null;
                throw new NoSuchElementException();
            }
            PageId pageId = new HeapPageId(tableId,pageCur);
            HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid,pageId,Permissions.READ_ONLY);
            tupleIterator = page.iterator();
        }
    }

    @Override
    public void rewind() throws DbException, TransactionAbortedException {
        pageCur = 0;
        PageId pageId = new HeapPageId(tableId,pageCur);
        HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid,pageId,Permissions.READ_ONLY);
        tupleIterator = page.iterator();
    }

    @Override
    public void close() {
        tupleIterator = null;
    }
}
