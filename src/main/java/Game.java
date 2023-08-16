import javax.swing.*;

public class Game extends JFrame {

    Game() {
        add(new Panel());
    }
    public static void main(String[] args) {
        Game pacmanGame = new Game();
        pacmanGame.setVisible(true);
        pacmanGame.setTitle("Pacman");
        pacmanGame.setIconImage(new ImageIcon("src/images/pacman48x48.png").getImage());
        pacmanGame.setSize(380,420);
        pacmanGame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pacmanGame.setLocationRelativeTo(null);
    }
}
