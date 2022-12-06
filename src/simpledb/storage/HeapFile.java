package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File file;
    private TupleDesc tupleDesc;

    private static final class HeapFileIterator implements DbFileIterator{
        private final HeapFile heapFile;
        private final TransactionId transactionId;
        private Iterator<Tuple> tupleIterator;
        private int index;

        public HeapFileIterator(HeapFile heapFile,TransactionId transactionId){
            this.heapFile = heapFile;
            this.transactionId = transactionId;
        }

        @Override
        public void open() throws DbException, TransactionAbortedException {
            index = 0;
            tupleIterator = getTupleIterator(index);
        }

        private Iterator<Tuple> getTupleIterator(int pageNumber) throws DbException, TransactionAbortedException {
            if(pageNumber >= 0 && pageNumber < heapFile.numPages()){
                HeapPageId heapPageId = new HeapPageId(heapFile.getId(), pageNumber);
                HeapPage heapPage = (HeapPage) Database.getBufferPool().getPage(transactionId,heapPageId,Permissions.READ_ONLY);
                return heapPage.iterator();
            }else{
                throw new DbException(String.format("heapFile %d  does not exist in page[%d]!", pageNumber,heapFile.getId()));
            }
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            if(tupleIterator == null){
                return false;
            }
            if(tupleIterator.hasNext()){
                return true;
            }else{
                if(index < heapFile.numPages() - 1){
                    index++;
                    tupleIterator = getTupleIterator(index);
                    return tupleIterator.hasNext();
                }else{
                    return false;
                }
            }
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if(tupleIterator == null || !tupleIterator.hasNext()){
                throw new NoSuchElementException();
            }
            return tupleIterator.next();
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            close();
            open();
        }

        @Override
        public void close() {
            tupleIterator = null;
        }
    }

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.file = f;
        this.tupleDesc = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        int tableId = pid.getTableId();
        int pgNo = pid.getPageNumber();
        int offset = pgNo * BufferPool.getPageSize();
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(file,"r");
            if((long) (pgNo + 1) *BufferPool.getPageSize() > randomAccessFile.length()){
                randomAccessFile.close();
                throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
            }
            byte[] bytes = new byte[BufferPool.getPageSize()];
            randomAccessFile.seek(offset);
            int read = randomAccessFile.read(bytes,0,BufferPool.getPageSize());
            if(read != BufferPool.getPageSize()){
                throw new IllegalArgumentException(String.format("table %d page %d read %d bytes not equal to BufferPool.getPageSize() ", tableId, pgNo, read));
            }
            HeapPageId id = new HeapPageId(pid.getTableId(),pid.getPageNumber());
            return new HeapPage(id,bytes);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(randomAccessFile != null){
                    randomAccessFile.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        throw new IllegalArgumentException(String.format("table %d page %d is invalid", tableId, pgNo));
    }


    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
        HeapPageId heapPageId = (HeapPageId) page.getId();
        int pgNo = heapPageId.getPageNumber();
        int offset = pgNo * BufferPool.getPageSize();
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
        randomAccessFile.seek(offset);
        randomAccessFile.write(page.getPageData());
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int) getFile().length() / BufferPool.getPageSize();
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        HeapPage page;
        for(int pgNo = 0;;pgNo++){
            HeapPageId pageId = new HeapPageId(getId(),pgNo);
            try{
                page = (HeapPage) Database.getBufferPool().getPage(tid,pageId,Permissions.READ_WRITE);
                if(page.getNumEmptySlots() == 0){
                    continue;
                }
            } catch (IllegalArgumentException e) {
                page = new HeapPage(pageId,HeapPage.createEmptyPageData());
                writePage(page);
                page = (HeapPage) Database.getBufferPool().getPage(tid,pageId,Permissions.READ_WRITE);
            }
            break;
        }
        page.insertTuple(t);
        return Collections.singletonList(page);
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        // not necessary for lab1
        HeapPage page;
        RecordId recordId = t.getRecordId();
        if(getId() != recordId.getPageId().getTableId()){
            throw new DbException(String.format("tableId not equals %d != %d", getId(), recordId.getPageId().getTableId()));
        }
        HeapPageId pageId = new HeapPageId(getId(),recordId.getPageId().getPageNumber());
        page = (HeapPage) Database.getBufferPool().getPage(tid,pageId,Permissions.READ_WRITE);
        page.deleteTuple(t);
        return Collections.singletonList(page);
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(this,tid);
    }

}

