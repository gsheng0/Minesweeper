import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.*;

public class MineSweeper extends JPanel implements ActionListener, MinefieldListener {
    private JFrame frame;
    private JMenu menu, iconMenu;
    private JMenuBar menuBar;
    private JMenuItem beginner, intermediate, expert;
    private JMenuItem original, roman, dice;
    private Minefield field;
    private JToggleButton smileyButton;
    private LogManager logManager = LogManager.getLogManager();
    private Logger logger = Logger.getLogger(MineSweeper.class.getSimpleName());
    public static final MinefieldFormat BEGINNER = new MinefieldFormat(9, 9, 10);
    public static final MinefieldFormat INTERMEDIATE = new MinefieldFormat(16, 16, 40);
    public static final MinefieldFormat EXPERT = new MinefieldFormat(16, 30, 99);
    public static Font font;
    private JLabel timerLabel, marksLeftLabel;
    private Thread timer;
    public static boolean clicked = false;
    private int time = 0;
    private Dimension norm = new Dimension(30, 1);
    private Dimension button = new Dimension(16, 16);
    static {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("C:\\myapp\\MineSweeper\\src\\main\\mine-sweeper.ttf")).deriveFont(5f);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setFirstClick()
    {
        clicked = true;
    }

    public MineSweeper() {
        ConsoleHandler consoleHandler;
        FileHandler fileHandler;
        Formatter formatter;
        try
        {
            consoleHandler = new ConsoleHandler();
            fileHandler = new FileHandler("./minesweeper.log");
            formatter = new SimpleFormatter();

            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            consoleHandler.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            logger.setLevel(Level.ALL);

            logger.setFilter(record -> record.getLevel().intValue() > Level.FINE.intValue());
            logManager.readConfiguration(new FileInputStream("/minesweeper.properties"));

            fileHandler.setFormatter(formatter);
            logger.config("Configuration Complete");
            logger.removeHandler(consoleHandler);

        }
        catch(Exception e)
        {
            logger.log(Level.INFO, "Error with handler: " + e);
        }

        Tile.loadRomanIconSet();
        Tile.loadDiceIconSet();
        Tile.loadDefaultIconSet();
        beginner = new JMenuItem("Beginner");
        intermediate = new JMenuItem("Intermediate");
        expert = new JMenuItem("Expert");
        beginner.addActionListener(this);
        intermediate.addActionListener(this);
        expert.addActionListener(this);
        beginner.setFont(font);
        intermediate.setFont(font);
        expert.setFont(font);

        original = new JMenuItem("Default");
        roman = new JMenuItem("Roman");
        dice = new JMenuItem("Dice");
        original.addActionListener(this);
        roman.addActionListener(this);
        dice.addActionListener(this);
        original.setFont(font);
        roman.setFont(font);
        dice.setFont(font);


        timerLabel = new JLabel("" + 0);
        marksLeftLabel = new JLabel();
        marksLeftLabel.setFont(font);
        marksLeftLabel.setAlignmentX(JLabel.CENTER);
        timerLabel.setFont(font);
        timerLabel.setAlignmentX(JLabel.CENTER);

        smileyButton = new JToggleButton();
        smileyButton.setSize(32, 32);
        smileyButton.setIcon(Tile.SMILE);
        smileyButton.addActionListener(e -> {
            field.reset();
            setUpBoard(field.clone());
            System.out.println("CLICKED");
        });
        logger.log(Level.INFO, "" + smileyButton.getHeight());
        smileyButton.setPreferredSize(button);
        smileyButton.setMinimumSize(button);
        smileyButton.setMaximumSize(button);

        timer = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e);
                }
                if(!field.gameOver() && clicked) {
                    time++;
                    logger.log(Level.FINEST, "Time has increased: {0}", new Object[]{time});
                    timerLabel.setText("" + time);
                }
            }
        });

        menu = new JMenu("Beginner");
        iconMenu = new JMenu("Default");
        menuBar = new JMenuBar();
        menu.add(beginner);
        menu.add(intermediate);
        menu.add(expert);
        menu.setFont(font);
        iconMenu.add(original);
        iconMenu.add(dice);
        iconMenu.add(roman);
        iconMenu.setFont(font);
        menuBar.add(menu);
        menuBar.add(getSpacer());
        menuBar.add(timerLabel);
        menuBar.add(getSpacer());
        menuBar.add(smileyButton);
        menuBar.add(getSpacer());
        menuBar.add(marksLeftLabel);
        menuBar.add(getSpacer());
        menuBar.add(iconMenu);
        frame = new JFrame("Minesweeper");
        frame.setResizable(false);
        frame.setJMenuBar(menuBar);
        setUpBoard(BEGINNER);
        timer.start();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
    public void setUpBoard(Minefield field)
    {
        logger.log(Level.INFO, "Size of field: {0}, {1}", new Object[] {field.getX(), field.getY()});
        try{
            frame.getContentPane().removeAll();
        }
        catch(Exception e) {}

        JPanel fieldPanel = new JPanel();
        this.field = field;
        fieldPanel.setLayout(new GridLayout(field.getX(), field.getX()));
        for(int x = 0; x < field.getX(); x++) {
            for (int y = 0; y < field.getY(); y++) {
                fieldPanel.add(field.get(x, y).getButton());
            }
        }
        marksLeftLabel.setText("" + field.mineCount());
        field.addMinefieldListener(this);
        frame.setSize(field.getY() * 32, field.getX() * 32);
        frame.add(fieldPanel);
        frame.repaint();
        frame.revalidate();
        clicked = false;
        time = 0;
    }
    public void setUpBoard(MinefieldFormat format)
    {
        setUpBoard(new Minefield(format));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
            if(e.getSource() == original) {
                Tile.loadDefaultIconSet();
                iconMenu.setText("Original");
                field.updateIcons();
            }
            else if(e.getSource() == roman) {
                Tile.loadRomanIconSet();
                iconMenu.setText("Roman");
                field.updateIcons();
            }
            else if(e.getSource() == dice) {
                Tile.loadDiceIconSet();
                iconMenu.setText("Dice");
                field.updateIcons();
            }

            else if (e.getSource() == beginner) {
                setUpBoard(BEGINNER);
                menu.setText("Beginner");
                timerLabel.setText("0");
            }
            else if (e.getSource() == intermediate) {
                setUpBoard(INTERMEDIATE);
                menu.setText("Intermediate");
                timerLabel.setText("0");
            }
            else if (e.getSource() == expert) {
                setUpBoard(EXPERT);
                menu.setText("Expert");
                timerLabel.setText("0");
            }

            logger.log(Level.FINE, "Field has been restarted");

    }
    public static void main(String args[]) {
        MineSweeper game = new MineSweeper();
    }
    public static JMenu getSpacer() { return getSpacer(30, 1); }
    public static JMenu getSpacer(int x, int y) {
        JMenu output = new JMenu();
        output.setEnabled(false);
        Dimension dim = new Dimension(x, y);
        output.setMinimumSize(dim);
        output.setPreferredSize(dim);
        output.setMaximumSize(dim);
        return output;
    }
    @Override
    public void onMinePressed() {
        marksLeftLabel.setText("" + (-1 * field.getMarked() + field.mineCount()));
    }
}
