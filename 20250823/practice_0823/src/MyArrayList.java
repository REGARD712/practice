import java.util.Arrays;

public class MyArrayList implements IList {
    public int[] arr;
    public int usedSize;
    public static int Default_num = 10;

    public MyArrayList() {
        this.arr = new int[Default_num];
    }

    private boolean isFull() {
        return this.arr.length == usedSize;
    }

    private void expand() {
        this.arr = Arrays.copyOf(this.arr,2*this.arr.length);
    }

    @Override
    public void add(int data) {
        if(isFull()) {
            expand();
        }
        this.arr[usedSize] = data;
        usedSize++;
    }

    private void addPosCheck(int pos) {
        if(pos<0 || pos>usedSize) {
            throw new PosIllegalException("Illegal position！");
        }
    }

    @Override
    public void add(int pos, int data) {
        try {
            addPosCheck(pos);
            if(isFull()) {
                expand();
            }
            for (int i = usedSize; i > pos; i--) {
                this.arr[i] = this.arr[i-1];
            }
            this.arr[pos] = data;
            usedSize++;
        } catch(PosIllegalException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean contains(int toFind) {
        for (int i = 0; i < usedSize; i++) {
            if(this.arr[i] == toFind) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int indexOf(int toFind) {
        for (int i = 0; i < usedSize; i++) {
            if(this.arr[i] == toFind) {
                return i;
            }
        }
        return -1;
    }

    private void isEmpty() {
        if(usedSize == 0) {
            throw new ArrayEmptyException("The array is empty！");
        }
    }

    private void PosCheck(int pos) {
        if(pos<0 || pos>=usedSize) {
            throw new PosIllegalException("Illegal position！");
        }
    }

    @Override
    public int get(int pos) {
        try {
            isEmpty();
            PosCheck(pos);
            return this.arr[pos];
        } catch(ArrayEmptyException e) {
            e.printStackTrace();
        } catch(PosIllegalException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void set(int pos, int data) {
        try {
            isEmpty();
            PosCheck(pos);
            this.arr[pos] = data;
        } catch(ArrayEmptyException e) {
            e.printStackTrace();
        } catch(PosIllegalException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(int toRemove) {
        try {
            int pos = indexOf(toRemove);
            isEmpty();
            PosCheck(pos);
            for(int i = pos; i < usedSize-1; i++) {
                this.arr[i] = this.arr[i+1];
            }
            usedSize--;
        } catch(ArrayEmptyException e) {
            e.printStackTrace();
        } catch(PosIllegalException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        usedSize = 0;
    }

    @Override
    public int size() {
        return usedSize;
    }

    @Override
    public void display() {
        for (int i = 0; i < usedSize; i++) {
            System.out.print(this.arr[i] + " ");
        }
        System.out.println();
    }
}
