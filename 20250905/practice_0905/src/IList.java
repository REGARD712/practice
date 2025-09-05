public interface IList {
    public int size();
    public void display();
    public boolean contains(int data);
    public void addFirst(int data);
    public void addLast(int data);
    public void addIndex(int index,int data);
    public void remove(int data);
    public void removeAllData(int data);
    public void clear();
}
