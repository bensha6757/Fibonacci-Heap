import java.util.*;



/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap
{
    private HeapNode first;
    private HeapNode min;
    private int size;
    private int marked;
    private int trees;
    private static int links;
    private static int cuts;


    /**
     * public boolean isEmpty()
     *
     * precondition: none
     *
     * The method returns true if and only if the heap
     * is empty.
     *
     */
    public boolean isEmpty() // complexity O(1)
    {
        return first == null; // checks if the pointer to the first HeapNode is null
    }

    /**
     * public HeapNode insert(int key)
     *
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     */
    public HeapNode insert(int key)  // complexity O(1)
    {
        // creating a new heap with one HeapNode - a new node contains the given key
        FibonacciHeap keyHeap = new FibonacciHeap(); // creating the heap
        HeapNode keyNode = new HeapNode(key); // creating the node
        // updating the heap's fields
        keyHeap.size = 1;
        keyHeap.min = keyNode;
        keyHeap.first = keyNode;
        keyHeap.marked = 0;
        keyHeap.trees = 1;
        this.meld(keyHeap); // melding this heap with the new one
        this.first = this.first.getPrev();
        // updating the "first" pointer to point on the new node inserted, that way, it is inserted to the beginning of the heap
        return keyNode; // returning the new node inserted for future use
    }


    /**
     * public void deleteMin()
     *
     * Delete the node containing the minimum key.
     *
     */
    public void deleteMin(){  // complexity is O(log n) amortized

        // If the heap is empty, do nothing
        if (isEmpty()){
            return;
        }

        HeapNode prevChild = this.min;
        int childrenAmount = this.min.rank;
        HeapNode currChild = this.min.child;
        HeapNode nextChild;

        // Insert the min's children to the roots list
        for (int i = 0; i < childrenAmount; i++) {
            nextChild = currChild.next;

            // Delete currChild from children list
            currChild.prev.next = currChild.next;
            currChild.next.prev = currChild.prev;

            // Insert currChild to roots list
            currChild.prev = prevChild;
            currChild.next = prevChild.next;
            prevChild.next.prev = currChild;
            prevChild.next = currChild;

            // Unmark and unparent
            currChild.parent = null;
            currChild.mark = false;

            this.trees++;

            prevChild = currChild;
            currChild = nextChild;
        }

        // Delete the minimum node from root list

        // If the minimum is the only one in the heap return empty heap
        if (this.min.next.compareTo(this.min) == 0){
            this.first = null;
            this.min = null;
            this.trees--;
            this.size--;
            return;
        }

        // There are more nodes in the heap
        this.min.prev.next = this.min.next;
        this.min.next.prev = this.min.prev;
        if (this.first.compareTo(this.min) == 0){
            this.first = this.min.next;
        }
        this.min = this.min.next;
        this.size--;
        this.trees--;

        consolidating();
    }



    private void consolidating(){
        HeapNode[] buckets;
        buckets = toBuckets();
        fromBuckets(buckets);
    }


    private HeapNode[] toBuckets(){
        int n = this.size;
        int d = rankBarrier(n);
        HeapNode[] buckets = new HeapNode[d];
        HeapNode x = this.first;
        HeapNode nextRoot;

        // Run on the root list
        for (int j = 0; j < this.trees; j++){
            int r = x.rank;
            nextRoot = x.next;

            // While there is a tree in the right place in buckets
            while (buckets[r] != null){
                HeapNode y = buckets[r];

                // Check whos key value is greater
                if (x.compareTo(y) > 0){
                    HeapNode tmp = x;
                    x = y;
                    y = tmp;
                }

                //deleteFromRootList(y);

                // Make one tree which its root is x and one of its children is y
                link(x, y);
                buckets[r] = null;
                r++;
            }
            buckets[r] = x;
            x = nextRoot;
        }
        return buckets;

    }

    private void fromBuckets(HeapNode[] buckets){

        // Nullify the heap
        this.first = null;
        this.min = null;
        this.trees = 0;

        HeapNode x;
        for (int i = buckets.length - 1; i >= 0; i--){
            x = buckets[i];
            if (x != null) {
                if (this.first == null) {
                    this.first = x;
                    this.min = x;
                }
                else {

                    // Delete x from root list
                    x.prev.next = x.next;
                    x.next.prev = x.prev;

                    //Insert x to root list
                    x.next = this.first;
                    x.prev = this.first.prev;
                    this.first.prev.next = x;
                    this.first.prev = x;
                    this.first = x;

                    // Update min
                    if (x.compareTo(this.min) < 0){
                        this.min = x;
                    }

                }

                // Increase number of trees
                this.trees++;
            }
        }
    }


    private void link(HeapNode x, HeapNode y){

        // Delete y from root list
        y.prev.next = y.next;
        y.next.prev = y.prev;


        // Make y a child of x
        HeapNode xChild = x.child;
        if (xChild == null){
            x.child = y;
            y.next = y;
            y.prev = y;
            y.parent = x;
        }
        else {
            x.child = y;
            y.parent = x;
            y.next = xChild;
            xChild.prev.next = y;
            y.prev = xChild.prev;
            xChild.prev = y;
        }
        x.rank++;
        links++;
    }

    private static int rankBarrier(int n){
        return (int) Math.ceil(((1.4405 * Math.log(n)) + 40));
    }



    /**
     * public HeapNode findMin()
     *
     * Return the node of the heap whose key is minimal.
     *
     */
    public HeapNode findMin()
    {
        return min; // returns the field min - which carries a pointer to the HeapNode with the minimal key
    }

    /**
     * public void meld (FibonacciHeap heap2)
     *
     * Meld the heap with heap2
     *
     */
    public void meld (FibonacciHeap heap2) // complexity O(1)
    {
        if (!this.isEmpty() && !heap2.isEmpty()) { // checks if both of the heaps aren't empty
            // adding heap2's size, #marked, #trees to this heap, and updating min to be the minimal key between the two heaps
            this.size += heap2.size();
            this.min = (Math.min(this.findMin().getKey(), heap2.findMin().getKey()) == this.min.getKey()) ? this.min : heap2.findMin();
            this.marked += heap2.marked;
            this.trees += heap2.trees;
            // connecting the two heaps by updating pointers
            HeapNode heap2Last = heap2.first.getPrev();
            HeapNode heap1Last = this.first.getPrev();
            this.first.setPrev(heap2Last);
            heap1Last.setNext(heap2.first);
            heap2.first.setPrev(heap1Last);
            heap2Last.setNext(this.first);
        }
        if (this.isEmpty() && !heap2.isEmpty()){ // checks if this heap is empty and the other isn't
            // updating this heap's fields to be heap2's
            this.size = heap2.size();
            this.min = heap2.findMin();
            this.first = heap2.first;
            this.marked = heap2.marked;
            this.trees = heap2.trees;
        }
        // in case both are empty or in case this heap isn't empty and the other is - no need to update anything
    }

    /**
     * public int size()
     *
     * Return the number of elements in the heap
     *
     */
    public int size() // complexity O(1)
    {
        return size; // returns the field - size, which carries an integer counts number of elements in the heap.
    }

    /**
     * public int[] countersRep()
     *
     * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap.
     *
     */
    public int[] countersRep() // complexity O(#ofTrees)
    {
        int maxi = 0;
        if (this.isEmpty()){
            return new int [0];
        }
        HeapNode x = this.first;
        do{ // checks which root has the greatest rank
            if (x.getRank() > maxi){
                maxi = x.getRank();
            }
            x = x.getNext();
        } while (x.compareTo(first) != 0);
        int[] arr = new int[maxi + 1];
        x = this.first;
        do{ // counting how many roots there are of rank i and puts it in arr[i]
            arr[x.getRank()] ++;
            x = x.getNext();
        } while (x.compareTo(first) != 0);
        return arr;
    }

    /**
     * public void delete(HeapNode x)
     *
     * Deletes the node x from the heap.
     *
     */
    public void delete(HeapNode x)
    {
        decreaseKey(x,Math.abs(x.getKey() - min.getKey()) + 10); // decreasing x's key to be the new min
        deleteMin(); // deleting x
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     *
     * The function decreases the key of the node x by delta. The structure of the heap should be updated
     * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta)
    {
        x.key -= delta; // decreasing x's key
        if (x.compareTo(min) < 0){ // updating the min in case it was changed
            this.min = x;
        }
        if (isRoot(x) || x.compareTo(x.getParent()) > 0){ // in case x is a root, or x is bigger then it's parent - these situations are OK
            return;
        }
        else { // else - the heap is not ordered, we need to perform cascading cuts
            cascadingCut(x, x.getParent());
        }
    }

    private void cascadingCut(HeapNode x , HeapNode y){
        cut(x,y); // cut x from it's parent y
        if (!isRoot(y)){ //if y is a root, we are done, else, check the following:
            if (y.getMark() == false){ // if y isn't marked, mark it and finish
                y.setMark(true);
                marked++; // updating # of marked nodes, as we marked a new one
            }
            else {
                cascadingCut(y,y.getParent()); // else, do the cascadingCut again, on y and y's parent
            }
        }

    }

    private void cut(HeapNode x , HeapNode y){
        cuts++; // updating # of cuts after performing a cut
        x.setParent(null); // cutting x from it's parent
        if (x.getMark() == true){
            x.setMark(false); // unmarking x as it becomes a root
            marked--; // updating # of marked nodes in case x was marked
        }
        y.rank--; // updating y's rank after deleting it's child
        if (x.compareTo(x.next) == 0){ // if x is the only child of y, set y's child to be null
            y.setChild(null);
        }
        else {
            if (y.getChild().compareTo(x) == 0) { // if x is y's child - set it's child to be x's next. else - don't update y's child
                y.setChild(x.getNext());
            }
            //deleting x from a double linked list:
            x.getPrev().setNext(x.getNext());
            x.getNext().setPrev(x.getPrev());
        }
        trees++; // updating # of trees, as x will be inserted as a new root to the heap
        // insert x to the beginning of the heap by changing pointers
        HeapNode insertBefore = this.first;
        HeapNode last = this.first.getPrev();
        this.first = x;
        x.setNext(insertBefore);
        x.setPrev(last);
        insertBefore.setPrev(x);
        last.setNext(x);
    }

    private boolean isRoot(HeapNode x){
        return x.getParent() == null; // checks if x's parent is null
    }

    /**
     * public int potential()
     *
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap.
     */
    public int potential()  // complexity O(1)
    {
        return trees + (2 * marked);
    }

    /**
     * public static int totalLinks()
     *
     * This static function returns the total number of link operations made during the run-time of the program.
     * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of
     * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value
     * in its root.
     */
    public static int totalLinks() // complexity O(1)
    {
        return links;
    }

    /**
     * public static int totalCuts()
     *
     * This static function returns the total number of cut operations made during the run-time of the program.
     * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts() // complexity O(1)
    {
        return cuts;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     *
     * This static function returns the k minimal elements in a binomial tree H.
     * The function should run in O(k(logk + deg(H)).
     */
    public static int[] kMin(FibonacciHeap H, int k) {
        if (k > H.size) { // in case k is bigger then # of nodes we have, we will return all of the keys in the tree
            k = H.size;
        }
        int[] arr = new int[k];
        if (H.isEmpty() || k == 0){ // if our heap is empty or we were asked to provide 0 keys - return empty array
            return arr;
        }
        FibonacciHeap help = new FibonacciHeap();
        kMinRec(H, help, H.first, k,0); // updates the help heap to include k levels of H
        for (int i = 0 ; i < k ; i++) {
            arr[i] = help.findMin().getKey(); // appending to the array the k minimal items in help heap
            help.deleteMin();
        }
        return arr; // returning the k minimal elements in a binomial tree H.
    }
    public static void kMinRec(FibonacciHeap H, FibonacciHeap help, HeapNode x, int k, int level) {
        if (level >= k){ // stop - in case we have passed the k levels of the tree
            return;
        }
        HeapNode y = x;
        // inserting all of the keys from the level "level", if any of our nodes in this level has a child, insert all of its children recursively
        do {
            help.insert(x.getKey());
            if (x.getChild() != null){
                kMinRec(H, help, x.getChild(), k, ++level);
            }
            x = x.getNext();
        } while (x.compareTo(y) != 0);
        return;
    }



    /**
     * public class HeapNode
     *
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in
     * another file
     *
     */


    public class HeapNode implements Comparable<HeapNode>{

        public int key;
        private int rank;
        private boolean mark;
        private HeapNode child;
        private HeapNode next;
        private HeapNode prev;
        private HeapNode parent;

        @Override
        public int compareTo(HeapNode o) { // our HeapNode class is implementing comparable - a node is comparable by it's key
            return Integer.compare(this.key,o.key);
        }

        public int getRank(){
            return this.rank;
        }

        public void setRank(int rank){
            this.rank = rank;
        }

        public boolean getMark(){
            return this.mark;
        }

        public void setMark(boolean mark){
            this.mark = mark;
        }

        public HeapNode getChild(){
            return this.child;
        }

        public void setChild(HeapNode child){
            this.child = child;
        }

        public HeapNode getNext(){
            return this.next;
        }

        public void setNext(HeapNode next){
            this.next = next;
        }

        public HeapNode getPrev(){
            return this.prev;
        }

        public void setPrev(HeapNode prev){
            this.prev = prev;
        }

        public HeapNode getParent(){
            return this.parent;
        }
        public void setParent(HeapNode parent){
            this.parent = parent;
        }

        public HeapNode(int key) {
            this.key = key;
            this.rank = 0;
            this.mark = false;
            this.next = this;
            this.prev = this;
        }

        public int getKey() {
            return this.key;
        }

    }
}