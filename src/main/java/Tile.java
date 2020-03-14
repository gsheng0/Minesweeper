import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tile {
    private Location location;
    private boolean mine = false;
    private boolean marked = false;
    private boolean visible = false;
    private JToggleButton button;
    static ImageIcon MINE, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, MARKED, UNCLICKED, EMPTY;
    static ImageIcon SMILE;
    private static ImageIcon[] numIcons;
    private Minefield field;
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private EventListener listener;
    public Tile(Location location)
    {
        this.location = location;
        listener = new buttonClickListener();
        button = new JToggleButton();
        button.setSize(32, 32);
        button.setIcon(UNCLICKED);
        button.addMouseListener((MouseListener)listener);
        button.addActionListener((ActionListener) listener);

    }
    public void setMinefield(Minefield field) { this.field = field; }
    public JToggleButton getButton() { return button; }
    public Location gridLocation() { return location; }
    public boolean hasMine() { return mine; }
    public void setMine() { mine = true;}
    public boolean isMarked() { return marked; }
    public void updateIcon() {
        if(marked)
            button.setIcon(MARKED);

        else if(!visible)
            button.setIcon(UNCLICKED);

        else if(visible && mine)
            button.setIcon(MINE);

        else
            button.setIcon(numIcons[adjacentMineTiles().size()]);
    }
    public void toggleMark()
    {
        logger.log(Level.FINER, "Attempting to toggle mark of Tile at [{0}, {1}]", location.asArray());
        if(visible) {
            logger.log(Level.FINER, "Notice: Tile at [{0}, {1}] is currently visible", location.asArray());
            return;
        }
        marked = !marked;
        if(marked) {
            button.setIcon(MARKED);
            field.incrementMarked();
        }
        else {
            button.setIcon(UNCLICKED);
            field.decrementMarked();
        }
        logger.log(Level.FINER, "Tile at [{0}, {1}] has been marked/unmarked", location.asArray());
    }
    public boolean isVisible() { return visible; }
    public ArrayList<Tile> adjacentMineTiles() {
        Tile[][] minefield = field.getField();
        ArrayList<Tile> adjacent = new ArrayList<>();
        for(int x = -1; x < 2; x++) {
            for(int y = -1; y < 2; y++) {
                try {
                    Tile curr = minefield[this.location.getX() + x][this.location.getY() + y];
                    if(curr.hasMine())
                        adjacent.add(curr);
                }
                catch(Exception e) {
                }
            }
        }
        return adjacent;

    }
    public void revealMine(){
        if(!mine)
            return;
        button.setIcon(MINE);
        visible = true;
    }
    public void reveal() {
        logger.log(Level.FINER, "Attempting to reveal Tile at [{0}, {1}]", location.asArray());
        if(visible) {
            logger.log(Level.FINER, "Notice: Tile at [{0}, {1}] is currently visible", location.asArray());
            return;
        }

        if(mine) {
            logger.log(Level.FINER, "Player has clicked on mine at [{0}, {1}]", location.asArray());
            button.setIcon(MINE);
            visible = true;
            if(!field.gameOver())
                field.revealMines();
            return;
        }

        if(field.gameOver())
            return;

        if(marked) {
            toggleMark();
            return;
        }

        logger.log(Level.FINER, "Checking for mines adjacent to Tile at [{0}, {1}]", location.asArray());
        ArrayList<Tile> adjacent = adjacentMineTiles();
        button.setIcon(numIcons[adjacent.size()]);
        logger.log(Level.FINEST, "Tile at [{0}, {1}] has {2} mines adjacent to it", new Object[] {location.getX(), location.getY(), adjacent.size()});
        visible = true;
        if(adjacent.size() > 0)
            return;
        logger.log(Level.FINER, "Revealing mineless tiles adjacent to Tile at [{0}, {1}]", location.asArray());
        for(int x = -1; x < 2; x++) {
            for(int y = -1; y < 2; y++) {
                try {
                    Tile curr = field.get(location.getX() + x, location.getY() + y);
                    if (!curr.visible)
                        curr.reveal();
                }
                catch(Exception e) { }
            }
        }



        logger.log(Level.FINER, "Finished revealing mineless tiles adjacent to Tile at [{0}, {1}]", location.asArray());
    }
    public void removeMine() {
        mine = false;
        field.addMine();
    }
    public void reset()
    {
        this.visible = false;
        this.marked = false;
    }

    public class buttonClickListener implements MouseListener, ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.log(Level.INFO, "There are {0} mines next to this Tile at [{1}, {2}]", new Object[] {adjacentMineTiles().size(), location.getX(), location.getY()});
            logger.log(Level.FINE, "This {0} the first click", new Object[] {(MineSweeper.clicked) ? "is not" : "is"});
            field.getListener().onMinePressed();
            logger.log(Level.INFO, "Clicked is {0}", new Object[] {MineSweeper.clicked});
            if(!MineSweeper.clicked && adjacentMineTiles().size() > 0)
            {
                ArrayList<Tile> tiles = adjacentMineTiles();
                for(int i = 0; i < tiles.size(); i++) {
                    logger.log(Level.INFO, "Moving mine in Tile at [{0}, {1}]", location.asArray());
                    tiles.get(i).removeMine();
                }
                MineSweeper.setFirstClick();
            }
            reveal();
            button.removeActionListener(this);
            button.addActionListener(e1 -> reveal());

        }

        @Override
        public void mouseClicked(MouseEvent e) {
            logger.log(Level.INFO, "Clicked is {0}", new Object[] {MineSweeper.clicked});
            field.getListener().onMinePressed();
            if(SwingUtilities.isRightMouseButton(e) && !field.gameOver())
                toggleMark();
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }
        @Override
        public void mouseReleased(MouseEvent e) {

        }
        @Override
        public void mouseEntered(MouseEvent e) {

        }
        @Override
        public void mouseExited(MouseEvent e) {

        }
    }


    public static void loadDefaultIconSet() {
        MINE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\MINE.JPG");
        ONE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\ONE.JPG");
        TWO = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\TWO.JPG");
        THREE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\THREE.JPG");
        FOUR = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\FOUR.JPG");
        FIVE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\FIVE.JPG");
        SIX = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\SIX.JPG");
        SEVEN = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\SEVEN.JPG");
        EIGHT = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\EIGHT.JPG");
        MARKED = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\MARKED.JPG");
        UNCLICKED = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\UNCLICKED.JPG");
        EMPTY = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Default\\EMPTY.JPG");

        numIcons = new ImageIcon[] {EMPTY, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT};
    }

    public static void loadRomanIconSet()
    {
        MINE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\MINE.png");
        ONE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\ONE.png");
        TWO = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\TWO.png");
        THREE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\THREE.png");
        FOUR = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\FOUR.png");
        FIVE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\FIVE.png");
        SIX = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\SIX.png");
        SEVEN = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\SEVEN.png");
        EIGHT = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\EIGHT.png");
        MARKED = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\MARKED.png");
        UNCLICKED = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\UNCLICKED.png");
        EMPTY = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Roman\\EMPTY.png");

        numIcons = new ImageIcon[] {EMPTY, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT};
    }

    public static void loadDiceIconSet()
    {
        MINE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\MINE.png");
        ONE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\ONE.png");
        TWO = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\TWO.png");
        THREE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\THREE.png");
        FOUR = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\FOUR.png");
        FIVE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\FIVE.png");
        SIX = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\SIX.png");
        SEVEN = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\SEVEN.png");
        EIGHT = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\EIGHT.png");
        MARKED = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\MARKED.png");
        UNCLICKED = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\UNCLICKED.png");
        EMPTY = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\Dice\\EMPTY.png");

        numIcons = new ImageIcon[] {EMPTY, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT};
    }

    static {
        SMILE = getImageIcon("C:\\myapp\\MineSweeper\\src\\main\\resources\\SMILE.JPG");
    }

    public static ImageIcon getImageIcon(String path)
    {
        return getImageIcon(path, 32, 32);
    }
    public static ImageIcon getImageIcon(String path, int x, int y)
    {
        ImageIcon output = new ImageIcon(path);
        output = new ImageIcon(output.getImage().getScaledInstance(x, y, Image.SCALE_SMOOTH));
        return output;
    }
}

