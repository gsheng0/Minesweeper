public class MinefieldFormat {

    private int x, y, mines;
    public MinefieldFormat(int x, int y, int mines)
    {
        this.x = x;
        this.y = y;
        this.mines = mines;
    }
    public int getX() { return x;}
    public int getY() { return y;}
    public int getMineCount() { return mines; }
}
