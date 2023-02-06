package simpledb.lock;

import simpledb.storage.Page;
import simpledb.storage.PageId;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LockManager {

    public ConcurrentHashMap<PageId,ConcurrentHashMap<TransactionId,Lock>>pageLocks;
    public LockManager(){
        pageLocks = new ConcurrentHashMap<>();
    }

    public synchronized boolean requestLock(PageId pid, TransactionId tid,Lock.TYPE requestType) throws InterruptedException, TransactionAbortedException {
        ConcurrentHashMap<TransactionId,Lock> pageLock = pageLocks.get(pid);
        final String thread = Thread.currentThread().getName();
        if(pageLock == null || pageLock.size() == 0){ // not lock
            pageLock = new ConcurrentHashMap<>();
            Lock lock = new Lock(tid,requestType);
            pageLock.put(tid,lock);
            pageLocks.put(pid,pageLock);
            System.out.println(thread + ": the " + pid.hashCode() + " have no lock, transaction" + tid + " require " + requestType + ", accept");
            return true;
        }
        Lock lock = pageLock.get(tid);
        if(lock == null){
            lock = new Lock(tid,requestType);
            if(requestType == Lock.TYPE.SHARE){
                if(pageLock.size() > 1){
                    System.out.println(thread + ": the " + pid.hashCode() + " have many read locks, transaction" + tid + " require " + requestType + ", accept and add a new read lock");
                    lock.setType(Lock.TYPE.SHARE);
                    pageLock.put(tid,lock);
                    pageLocks.put(pid,pageLock);
                    return true;
                }
                if(pageLock.size() == 1){
                    Lock oldLock = null;
                    for(Map.Entry<TransactionId,Lock> entry:pageLock.entrySet()){
                        oldLock = entry.getValue();
                    }
                    if(oldLock.getType() == Lock.TYPE.SHARE){
                        lock.setType(Lock.TYPE.SHARE);
                        pageLock.put(tid,lock);
                        pageLocks.put(pid,pageLock);
                        return true;
                    }
                    if(oldLock.getType() == Lock.TYPE.EXCLUSIVE){
                        wait(50);
                        return false;
                    }
                }
            }else{
                wait(10);
                return false;
            }
        }else{
            System.out.println(thread + ": the " + pid + " have one lock with same txid, transaction" + tid + " require " + requestType + ", accept");
            if(requestType == Lock.TYPE.SHARE){
                return true;
            }else{
                if(pageLock.size() > 1){
                    throw new TransactionAbortedException();
                }
                if(pageLock.size() == 1 && lock.getType() == Lock.TYPE.SHARE){
                    lock.setType(Lock.TYPE.EXCLUSIVE);
                    pageLock.put(tid,lock);
                    pageLocks.put(pid,pageLock);
                    return true;
                }
                return pageLock.size() == 1 && lock.getType() == Lock.TYPE.EXCLUSIVE;
            }
        }
        return false;
    }

    public synchronized void releaseLock(PageId pid,TransactionId tid){
        ConcurrentHashMap<TransactionId,Lock> pageLock = pageLocks.get(pid);
        if(pageLock == null){
            return;
        }
        Lock lock = pageLock.get(tid);
        if(lock == null){
            return;
        }
        pageLock.remove(tid);
        if(pageLock.size() == 0){
            pageLocks.remove(pid);
        }
        this.notifyAll();
    }

    public synchronized boolean isHoldingLock(PageId pid,TransactionId tid){
        ConcurrentHashMap<TransactionId,Lock> pageLock = pageLocks.get(pid);
        if(pageLock == null){
            return false;
        }
        return pageLock.get(tid) != null;
    }

    public synchronized void transactionComplete(TransactionId tid){
        Set<PageId> pageIdSet = pageLocks.keySet();
        for(PageId pageId:pageIdSet){
            releaseLock(pageId,tid);
        }
    }

    public void dumpLock(){
        System.out.println("------------------lock dump begin---------------------");
        for(Map.Entry<PageId,ConcurrentHashMap<TransactionId,Lock>> entry:pageLocks.entrySet()){
            System.out.println("pageId:" + entry.getKey().hashCode());
            ConcurrentHashMap<TransactionId,Lock> map = entry.getValue();
            for(Map.Entry<TransactionId,Lock>m:map.entrySet()){
                System.out.println("tid:" + m.getKey().hashCode());
                System.out.print("has lock:" + (m.getValue() != null));
                if((m.getValue() != null)){
                    System.out.println(" lock type:" + m.getValue().getType());
                }
            }
            System.out.println();
        }
        System.out.println("------------------lock dump finish---------------------");
    }
}
