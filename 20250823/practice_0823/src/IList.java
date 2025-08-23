public interface IList {
    public void add(int data);
    public void add(int pos, int data);
    public boolean contains(int toFind);
    public int indexOf(int toFind);
    public int get(int pos);
    public void set(int pos, int data);
    public void remove(int toRemove);
    public void clear();
    public int size();
    public void display();
}
