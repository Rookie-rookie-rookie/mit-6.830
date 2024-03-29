package simpledb.storage;

import simpledb.common.Type;
import simpledb.common.Utility;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 * TupleDesc 描述了一个Tuple的结构，包含内部元素的类型和名称
 */
public class TupleDesc implements Serializable {

    private String tableAlias;
    private List<TDItem>tdItems;
    /**
     * A help class to facilitate organizing the information of each field
     * 储存每一个field的信息
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
        return tdItems.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
        this.tdItems = new ArrayList<>();
        for(int i=0;i<typeAr.length;i++){
            this.tdItems.add(new TDItem(typeAr[i],fieldAr[i]));
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        this(typeAr, Utility.getStrings(typeAr.length,"unnamed"));
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return tdItems.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if(this.tableAlias != null){
            return this.tableAlias + "." + tdItems.get(i).fieldName;
        }
        return tdItems.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        return tdItems.get(i).fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        if(name == null){
            throw new NoSuchElementException("name is null");
        }
        for(TDItem tdItem:tdItems){
            if(name.equals(tdItem.fieldName)){
                return tdItems.indexOf(tdItem);
            }
        }
        throw new NoSuchElementException("not such element!");
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int size = 0;
        for(TDItem tdItem:tdItems){
            size += tdItem.fieldType.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        List<TDItem> tdItems1 = td1.tdItems;
        List<TDItem> tdItems2 = td2.tdItems;
        Type[] types = new Type[tdItems1.size()+ tdItems2.size()];
        String[] strings = new String[tdItems1.size()+ tdItems2.size()];
        for(int i = 0;i < tdItems1.size();i++){
            types[i] = tdItems1.get(i).fieldType;
            strings[i] = tdItems1.get(i).fieldName;
        }
        for(int i = tdItems1.size();i < tdItems1.size() + tdItems2.size();i++){
            types[i] = tdItems2.get(i - tdItems1.size()).fieldType;
            strings[i] = tdItems2.get(i - tdItems1.size()).fieldName;
        }
        return new TupleDesc(types,strings);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        // some code goes here
        if(! (o instanceof TupleDesc tupleDesc)){
            return false;
        }
        if(tupleDesc.getSize() != this.getSize() || tupleDesc.numFields() != this.numFields()){
            return false;
        }
        for(int i = 0;i < tupleDesc.numFields();i++){
            if(!this.getFieldType(i).equals(tupleDesc.getFieldType(i))){
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        StringBuilder res = new StringBuilder();
        for(int i=0;i<tdItems.size();i++){
            TDItem tdItem = tdItems.get(i);
            res.append(tdItem.fieldType.toString()).append("[").append(i).append("](").append(tdItem.fieldName).append("[").append(i).append("])");
        }
        return res.toString();
    }

    public void setTableAlias(String tableAlias){
        this.tableAlias = tableAlias;
    }
}
