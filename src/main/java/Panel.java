import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class Panel extends JPanel implements ActionListener {

    private Dimension dimension;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean dying = false;

    public final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = BLOCK_SIZE * N_BLOCKS;
    private final int MAX_GHOSTS = 12;
    private final int PAC_SPEED = 6;

    private int N_GHOSTS =  6;
    private int  lives, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int pacman_x, pacman_y, pacman_dx, pacman_dy;
    private int req_dx, req_dy;

    private final int[] validSpeeds = {1,2,3,4,6,8};
    private final int maxSpeed = 6;
    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    private final short[] levelData = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16,  16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18,  18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16,  16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,  0, 17, 16, 16,  16, 16, 16, 20,
            17, 18, 18, 22,  0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20,  0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20,  0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };


    Panel() {
        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }

    private void loadImages() {
        ghost = new ImageIcon("src/images/ghost.gif").getImage();
        heart = new ImageIcon("src/images/heart.png").getImage();
        down = new ImageIcon("src/images/down.gif").getImage();
        up = new ImageIcon("src/images/up.gif").getImage();
        left = new ImageIcon("src/images/left.gif").getImage();
        right = new ImageIcon("src/images/right.gif").getImage();

    }

    private void initVariables() {
        screenData = new short[N_BLOCKS * N_BLOCKS];
        dimension = new Dimension(400,400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);
        timer.start();
    }

    private void initGame() {
        lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel() {
        for(int i = 0; i < N_BLOCKS * N_BLOCKS; ++i)
            screenData[i] = levelData[i];
    }

    private void showIntroScreen(Graphics2D graphics2D) {
        String start = "Press SPACE to start";
        graphics2D.setColor(Color.YELLOW);
        graphics2D.drawString(start, SCREEN_SIZE / 4, 150);
    }

    private  void drawScore(Graphics2D graphics2D) {
        graphics2D.setColor(Color.YELLOW);
        graphics2D.setFont(smallFont);
        String str = "Score: " + score;
        graphics2D.drawString(str, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for(int i = 0; i < lives; i++)
            graphics2D.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
    }

    private void playGame(Graphics2D graphics2D) {
        if(dying)
            death();
        else {
            movePacman();
            drawPacman(graphics2D);
            moveGhosts(graphics2D);
            checkMaze();
        }
    }

    private void movePacman() {
        int pos;
        short ch;

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacman_dx = req_dx;
                    pacman_dy = req_dy;
                }
            }

            // Check for standstill
            if ((pacman_dx == -1 && pacman_dy == 0 && (ch & 1) != 0)
                    || (pacman_dx == 1 && pacman_dy == 0 && (ch & 4) != 0)
                    || (pacman_dx == 0 && pacman_dy == -1 && (ch & 2) != 0)
                    || (pacman_dx == 0 && pacman_dy == 1 && (ch & 8) != 0)) {
                pacman_dx = 0;
                pacman_dy = 0;
            }
        }
        pacman_x = pacman_x + PAC_SPEED * pacman_dx;
        pacman_y = pacman_y + PAC_SPEED * pacman_dy;
    }

    private void drawPacman(Graphics2D graphics2D) {
        if (req_dx == -1) {
            graphics2D.drawImage(left, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dx == 1) {
            graphics2D.drawImage(right, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dy == -1) {
            graphics2D.drawImage(up, pacman_x + 1, pacman_y + 1, this);
        } else {
            graphics2D.drawImage(down, pacman_x + 1, pacman_y + 1, this);
        }
    }

    private void moveGhosts(Graphics2D graphics2D) {
        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) {
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);
                count = 0;

                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }
                if (count == 0) {
                    if ((screenData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }
                } else {
                    count = (int) (Math.random() * count);
                    if (count > 3)
                        count = 3;

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }
            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(graphics2D, ghost_x[i] + 1, ghost_y[i] + 1);

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame)
                dying = true;
        }
    }

    public void drawGhost(Graphics2D graphics2D, int x, int y) {
        graphics2D.drawImage(ghost, x, y, this);
    }

    private void checkMaze() {
        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {
            if((screenData[i]) != 0)
                finished = false;

            i++;
        }
        if(finished) {
            score += 50;

            if (N_GHOSTS < MAX_GHOSTS)
                N_GHOSTS++;

            if (currentSpeed < maxSpeed)
                currentSpeed++;

            initLevel();
        }
    }

    private void death() {
        lives--;
        if(lives == 0)
            inGame = false;
        continueLevel();
    }

    private void continueLevel() {
        int dx = 1;
        int random;

        for(int i = 0; i < N_GHOSTS; ++i) {
            ghost_y[i] = 4 * BLOCK_SIZE;
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if(random > currentSpeed)
                random = currentSpeed;

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7 * BLOCK_SIZE;
        pacman_y = 11 * BLOCK_SIZE;
        pacman_dx = 0;
        pacman_dy = 0;
        req_dx = 0;
        req_dy = 0;
        dying = false;
    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0,0, dimension.width, dimension.height);

        drawMaze(graphics2D);
        drawScore(graphics2D);

        if(inGame)
            playGame(graphics2D);
        else
            showIntroScreen(graphics2D);

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawMaze(Graphics2D graphics2D) {
           short i = 0;
           int x,y;

           for(y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
               for(x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {
                   graphics2D.setColor(Color.BLUE);
                   graphics2D.setStroke(new BasicStroke(5));

                   if((levelData[i]) == 0)
                       graphics2D.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                   if((screenData[i] & 1) != 0)
                       graphics2D.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                   if((screenData[i] & 2) != 0)
                       graphics2D.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                   if((screenData[i] & 4) != 0)
                       graphics2D.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                   if((screenData[i] & 8) != 0)
                       graphics2D.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                   if((screenData[i] & 16) != 0) {
                       graphics2D.setColor(Color.WHITE);
                       graphics2D.fillOval(x + 10, y + 10, 6, 6);
                   }
                   i++;
               }
           }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    class  TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if(inGame) {
                if(key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if(key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if(key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if(key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if(key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if(key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }
}
