import java.util.logging.Level;
import java.util.logging.Logger;

public class Minefield {
    private Tile[][] field;
    private Boolean revealed = false;
    private int mines;
    private int marked;
    private MinefieldListener listener;
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private MinefieldFormat format;
    public Minefield(MinefieldFormat format)
    {
        this(format.getX(), format.getY(), format.getMineCount());
    }
    public Minefield(int x, int y, int mines)
    {
        createField(x, y);
        placeMines(x, y, mines);
        this.mines = mines;
        this.format = new MinefieldFormat(x, y, mines);
    }

    public int mineCount() { return mines; }
    public void scramble()
    {
        createField(field.length, field[0].length);
        placeMines(field.length, field[0].length, mines);
    }
    public void incrementMarked() { marked++; }
    public void decrementMarked() { marked--; }
    public int getMarked() { return marked; }
    private void placeMines(int x, int y, int mines)
    {
        int counter = 0;
        while (counter < mines) {
            int xCor = (int)(Math.random() * x);
            int yCor = (int)(Math.random() * y);
            if(!field[xCor][yCor].hasMine())
            {
                field[xCor][yCor].setMine();
                counter++;
            }
        }
    }
    public MinefieldFormat getFormat() { return format; }
    public void addMine()
    {
        int xCor = (int)(Math.random() * field.length);
        int yCor = (int)(Math.random() * field[0].length);
        while(field[xCor][yCor].hasMine())
        {
            xCor = (int)(Math.random() * field.length);
            yCor = (int)(Math.random() * field[0].length);
        }
        field[xCor][yCor].setMine();

        logger.log(Level.FINE, "A mine has been added to the Tile at [{0}, {1}]", new Object[]{xCor, yCor});
    }
    private void createField(int x, int y)
    {
        field = new Tile[x][y];
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++) {
                field[i][j] = new Tile(new Location(i, j));
                field[i][j].setMinefield(this);
            }
        }
    }
    public Tile[][] getField() { return field; }
    public int getX() { return field.length;}
    public int getY() { return field[0].length; }
    public Tile get(int x, int y) { return field[x][y];}
    public boolean gameOver() { return revealed; }
    public void revealMines()
    {
        logger.log(Level.INFO, "Mines have been revealed");
        if(revealed)
           return;
        revealed = true;
        for(int x = 0; x < field.length; x++) {
            for(int y = 0; y < field[0].length; y++) {
                if(field[x][y].hasMine())
                    field[x][y].revealMine();
            }
        }
    }
    public void addMinefieldListener(MinefieldListener listener)
    {
        this.listener = listener;
    }
    public MinefieldListener getListener() { return listener; }
    public void reset()
    {
        for(int x = 0; x < field.length; x++)
        {
            for(int y = 0; y < field[0].length; y++)
                field[x][y].reset();
        }
        marked = 0;
    }
    public void updateIcons() {
        for(int x = 0; x < field.length; x++)
        {
            for(int y = 0; y < field[0].length; y++)
                field[x][y].updateIcon();
        }
    }
    public Minefield clone() {
        Minefield output = new Minefield(field);
        for(int x = 0; x < output.getX(); x++)
        {
            for(int y = 0; y < output.getY(); y++)
                output.get(x, y).setMinefield(output);
        }
        return output;
    }

    private Minefield(Tile[][] field)
    {
        int mines = 0;
        Tile[][] other = new Tile[field.length][field[0].length];
        for(int x = 0; x < field.length; x++)
        {
            for(int y = 0; y < field[0].length; y++) {
                other[x][y] = new Tile(new Location(x, y));
                if (field[x][y].hasMine()) {
                    mines++;
                    other[x][y].setMine();
                }
            }
        }
        this.mines = mines;
        this.format = new MinefieldFormat(field.length, field[0].length, mines);
        this.field = other;
    }
}
