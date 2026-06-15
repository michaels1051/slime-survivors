import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DisplayPanel extends JPanel implements MouseListener, KeyListener, ActionListener {
    private int playerX;
    private int playerY;
    private ArrayList<Double> enemyXs;
    private ArrayList<Double> enemyYs;
    private ArrayList<Long> enemyLastHit;
    private BufferedImage background;
    private BufferedImage player;
    private BufferedImage enemy;
    private BufferedImage coin;
    private boolean[] pressedKeys;
    private Timer timer;
    private Timer playerTimer;
    private boolean gameOver;
    private ArrayList<Point> coins;
    private ArrayList<BufferedImage> playerUp;
    private ArrayList<BufferedImage> playerDown;
    private ArrayList<BufferedImage> playerRight;
    private ArrayList<BufferedImage> playerLeft;
    private int playerUpIdx;
    private int playerDownIdx;
    private int playerRightIdx;
    private int playerLeftIdx;
    private JButton resetButton;
    private int playerHP;
    private int exp;
    private int level;
    private int expToNextLevel;
    private Timer coinTimer;
    private Timer enemyTimer;
    private ArrayList<Point> bullets;
    private ArrayList<Double> bulletDirX;
    private ArrayList<Double> bulletDirY;
    private Timer shootTimer;
    private ArrayList<Integer> enemyHP;
    private String[] currentChoices = new String[3];
    private JButton upgrade1;
    private JButton upgrade2;
    private JButton upgrade3;
    private double playerSpeed = 3.0;
    private int bulletDamage = 1;
    private double bulletSpeed = 6.0;
    private int maxHP = 50;
    private boolean hasShotgun = false;
    private ArrayList<Integer> bulletSize;
    private ArrayList<Integer> bulletLife;
    private int regenCooldown = 0;

    public DisplayPanel() {
        exp = 0;
        level = 1;
        expToNextLevel = 10;
        playerUpIdx = 0;
        playerDownIdx = 0;
        playerRightIdx = 0;
        playerLeftIdx = 0;
        coins = new ArrayList<>();
        loadPlayerImages();
        gameOver = false;
        playerX = 0;
        playerY = 0;
        enemyXs = new ArrayList<>();
        enemyYs = new ArrayList<>();
        enemyLastHit = new ArrayList<>();
        bullets = new ArrayList<>();
        bulletDirX = new ArrayList<>();
        bulletDirY = new ArrayList<>();
        playerHP = 50;
        enemyHP = new ArrayList<>();
        bulletSize = new ArrayList<>();
        bulletLife = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            spawnEnemy();
        }
        coinTimer = new Timer(1000, e -> spawnCoin());
        coinTimer.start();
        enemyTimer = new Timer(1000, e -> {
            if (enemyXs.size() < 250) {
                spawnEnemy();
            }
        });
        enemyTimer.start();
        shootTimer = new Timer(300, e -> shootBullet());
        shootTimer.start();
        timer = new Timer(10, this);
        playerTimer = new Timer(200, this);
        pressedKeys = new boolean[128];
        try {
            background = ImageIO.read(new File("src/background.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        player = playerUp.get(playerUpIdx);
        try {
            enemy = ImageIO.read(new File("src/creatures.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        try {
            coin = ImageIO.read(new File("src/coin.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        upgrade1 = new JButton();
        upgrade2 = new JButton();
        upgrade3 = new JButton();
        add(upgrade1);
        add(upgrade2);
        add(upgrade3);
        upgrade1.setVisible(false);
        upgrade2.setVisible(false);
        upgrade3.setVisible(false);
        upgrade1.addActionListener(this);
        upgrade2.addActionListener(this);
        upgrade3.addActionListener(this);
        resetButton = new JButton("Reset");
        add(resetButton);
        resetButton.addActionListener(this);
        resetButton.setVisible(false);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
        timer.start();
        playerTimer.start();
    }

    private void loadPlayerImages() {
        playerRight = new ArrayList<>();
        playerLeft = new ArrayList<>();
        playerUp = new ArrayList<>();
        playerDown = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("src/slime_up" + i + ".png"));
                playerUp.add(img);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int i = 1; i <= 3; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("src/slime_down" + i + ".png"));
                playerDown.add(img);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int i = 1; i <= 3; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("src/slime_right" + i + ".png"));
                playerRight.add(img);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int i = 1; i <= 3; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("src/slime_left" + i + ".png"));
                playerLeft.add(img);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("HP: " + playerHP + "/" + maxHP, 50, 80);
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 56));
            g.drawString("GAME OVER", getWidth() / 2 - 190, 180);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("You reached Level " + level, getWidth() / 2 - 90, 240);
            g.drawString("Final EXP: " + exp, getWidth() / 2 - 75, 275);
            resetButton.setVisible(true);
            resetButton.setBounds(getWidth() / 2 - 75, 330, 150, 45);
        } else {
            g.drawImage(player, playerX, playerY, null);
            for (int i = 0; i < enemyXs.size(); i++) {
                g.drawImage(enemy, enemyXs.get(i).intValue(), enemyYs.get(i).intValue(), null);
            }
            for (Point p : coins) {
                g.drawImage(coin, p.x, p.y, null);
            }
        }
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(Color.GRAY);
        g.fillRect(50, 30, 200, 20);
        int barWidth = (int)((exp / (double)expToNextLevel) * 200);
        g.setColor(Color.GREEN);
        g.fillRect(50, 30, barWidth, 20);
        g.setColor(Color.BLACK);
        g.drawRect(50, 30, 200, 20);
        g.setColor(Color.WHITE);
        g.drawString("Level: " + level, 50, 25);
        g.setColor(Color.RED);
        for (int i = 0; i < bullets.size(); i++) {
            Point p = bullets.get(i);
            int size = bulletSize.get(i);
            g.fillOval(p.x, p.y, size, size);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        pressedKeys[keyCode] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys[key] = false;
    }

    private void movePlayer() {
        double speed = playerSpeed;

        if (pressedKeys[KeyEvent.VK_A]) {
            playerX -= speed;
        }
        if (pressedKeys[KeyEvent.VK_D]) {
            playerX += speed;
        }

        if (pressedKeys[KeyEvent.VK_W]) {
            playerY -= speed;
        }

        if (pressedKeys[KeyEvent.VK_S]) {
            playerY += speed;
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int playerWidth = player.getWidth();
        int playerHeight = player.getHeight();

        if (playerX < 0) {
            playerX = 0;
        }
        if (playerY < 0) {
            playerY = 0;
        }

        if (playerX + playerWidth > panelWidth) {
            playerX = panelWidth - playerWidth;
        }

        if (playerY + playerHeight > panelHeight) {
            playerY = panelHeight - playerHeight;
        }
    }

    private void moveEnemies() {

        for (int i = 0; i < enemyXs.size(); i++) {

            double enemyX = enemyXs.get(i);
            double enemyY = enemyYs.get(i);

            double dx = playerX - enemyX;
            double dy = playerY - enemyY;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {

                double speed = 2.0 + (double) level / 8;

                enemyX += dx / distance * speed;
                enemyY += dy / distance * speed;
            }

            enemyXs.set(i, enemyX);
            enemyYs.set(i, enemyY);
        }
    }

    private void animatePlayerWalking() {
        if (pressedKeys[KeyEvent.VK_W]) { // if pressing W key, UP
            playerUpIdx++;
            if (playerUpIdx >= playerUp.size()) {
                playerUpIdx = 0;
            }
            player = playerUp.get(playerUpIdx);
        }

        if (pressedKeys[KeyEvent.VK_S]) { // if pressing S key, DOWN
            playerDownIdx++;
            if (playerDownIdx >= playerDown.size()) {
                playerDownIdx = 0;
            }
            player = playerDown.get(playerDownIdx);
        }

        if (pressedKeys[KeyEvent.VK_A]) { // if pressing A key, LEFT
            playerLeftIdx++;
            if (playerLeftIdx >= playerLeft.size()) {
                playerLeftIdx = 0;
            }
            player = playerLeft.get(playerLeftIdx);
        }
        if (pressedKeys[KeyEvent.VK_D]) { // if pressing A key, LEFT
            playerRightIdx++;
            if (playerRightIdx >= playerRight.size()) {
                playerRightIdx = 0;
            }
            player = playerRight.get(playerRightIdx);
        }
    }

    private Rectangle playerRectangle() {
        int imageHeight = player.getHeight();
        int imageWidth = player.getWidth();
        Rectangle rect = new Rectangle(playerX, playerY, imageWidth, imageHeight);
        return rect;
    }

    private Rectangle coinRectangle(Point point) {
        int imageHeight = coin.getHeight();
        int imageWidth = coin.getWidth();
        Rectangle rect = new Rectangle(point.x, point.y, imageWidth, imageHeight);
        return rect;
    }

    private void checkForPlayerCoinCollision() {
        Rectangle playerRect = playerRectangle();
        for (int i = 0; i < coins.size(); i++) {
            Rectangle coinRect = coinRectangle(coins.get(i));
            if (playerRect.intersects(coinRect)) {
                exp += 1;
                if (exp >= expToNextLevel) {
                    level++;
                    exp -= expToNextLevel;
                    expToNextLevel += 5 * level / 2;
                    int delay = Math.max(200, 1000 - level * 40);
                    enemyTimer.setDelay(delay);
                    timer.stop();
                    enemyTimer.stop();
                    shootTimer.stop();
                    coinTimer.stop();
                    showUpgradeChoices();
                }
                coins.remove(i);
                i--;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            movePlayer();
            moveEnemies();
            checkForPlayerCoinCollision();
            if (!gameOver && playerHP < maxHP) {
                regenCooldown++;

                if (regenCooldown >= 500) {
                    playerHP = Math.min(playerHP + 5, maxHP);
                    regenCooldown = 0;
                }
            } else {
                regenCooldown = 0;
            }
            for (int i = 0; i < bullets.size(); i++) {
                Point p = bullets.get(i);
                double x = p.x + bulletDirX.get(i) * bulletSpeed;
                double y = p.y + bulletDirY.get(i) * bulletSpeed;
                bullets.set(i, new Point((int)x, (int)y));

                if (bulletLife.get(i) > 0) {
                    bulletLife.set(i, bulletLife.get(i) - 1);

                    if (bulletLife.get(i) <= 0) {
                        bullets.remove(i);
                        bulletDirX.remove(i);
                        bulletDirY.remove(i);
                        bulletSize.remove(i);
                        bulletLife.remove(i);
                        i--;
                        continue;
                    }
                }

                if (x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
                    bullets.remove(i);
                    bulletDirX.remove(i);
                    bulletDirY.remove(i);
                    bulletSize.remove(i);
                    bulletLife.remove(i);
                    i--;
                }
            }

            for (int i = 0; i < bullets.size(); i++) {
                Point b = bullets.get(i);
                int size = bulletSize.get(i);
                Rectangle bulletRect = new Rectangle(b.x, b.y, size, size);

                for (int j = 0; j < enemyXs.size(); j++) {

                    Rectangle enemyRect = new Rectangle(
                            enemyXs.get(j).intValue(),
                            enemyYs.get(j).intValue(),
                            enemy.getWidth(),
                            enemy.getHeight()
                    );

                    if (bulletRect.intersects(enemyRect)) {
                        int hp = enemyHP.get(j);
                        hp -= bulletDamage;
                        enemyHP.set(j, hp);
                        bullets.remove(i);
                        bulletDirX.remove(i);
                        bulletDirY.remove(i);
                        i--;
                        if (hp <= 0) {
                            coins.add(new Point(enemyXs.get(j).intValue(), enemyYs.get(j).intValue()));
                            enemyXs.remove(j);
                            enemyYs.remove(j);
                            enemyLastHit.remove(j);
                            enemyHP.remove(j);
                        }
                        break;
                    }
                }
            }

            if (playerHP <= 0) {
                gameOver = true;
                timer.stop();
                shootTimer.stop();
            }
            long now = System.currentTimeMillis();
            for (int i = 0; i < enemyXs.size(); i++) {
                Rectangle enemyRect = new Rectangle(enemyXs.get(i).intValue(), enemyYs.get(i).intValue(), enemy.getWidth(), enemy.getHeight());
                if (playerRectangle().intersects(enemyRect)) {
                    long lastHit = enemyLastHit.get(i);
                    if (now - lastHit > 500) {
                        playerHP--;
                        enemyLastHit.set(i, now);
                        if (playerHP <= 0) {
                            gameOver = true;
                            timer.stop();
                            shootTimer.stop();
                        }
                    }
                }
            }
            repaint();
        }
        if (e.getSource() == upgrade1) {
            handleUpgrade(currentChoices[0]);
        }
        if (e.getSource() == upgrade2) {
            handleUpgrade(currentChoices[1]);
        }
        if (e.getSource() == upgrade3) {
            handleUpgrade(currentChoices[2]);
        }
        if (e.getSource() == playerTimer) {
            animatePlayerWalking();
            repaint();
        }
        if (e.getSource() == resetButton) {
            reset();
        }
    }



    private void reset() {
        gameOver = false;
        exp = 0;
        level = 1;
        enemyTimer.setDelay(1000);
        expToNextLevel = 10;
        maxHP = 50;
        playerHP = maxHP;
        playerX = 50;
        playerY = 435;
        enemyXs.clear();
        enemyYs.clear();
        enemyLastHit.clear();
        enemyHP.clear();
        coins.clear();
        bullets.clear();
        bulletDirX.clear();
        bulletDirY.clear();
        bulletSize.clear();
        bulletLife.clear();
        for (int i = 0; i < 10; i++) {
            spawnEnemy();
        }
        playerSpeed = 3.0;
        bulletDamage = 1;
        bulletSpeed = 6.0;
        hasShotgun = false;
        regenCooldown = 0;
        upgrade1.setVisible(false);
        upgrade2.setVisible(false);
        upgrade3.setVisible(false);
        playerUpIdx = 0;
        playerDownIdx = 0;
        playerLeftIdx = 0;
        playerRightIdx = 0;
        player = playerDown.get(0);
        resetButton.setVisible(false);
        requestFocusInWindow();
        shootTimer.start();
        timer.start();
    }

    private void spawnEnemy() {
        double angle = Math.random() * Math.PI * 2;
        double distance = 700 + Math.random() * 300;
        double x = playerX + Math.cos(angle) * distance;
        double y = playerY + Math.sin(angle) * distance;
        enemyXs.add(x);
        enemyYs.add(y);
        enemyLastHit.add(0L);
        int baseHp = 5 + (int)(level * 1.75);
        double multiplier = 1.0;
        if (level > 5) {
            multiplier += (level - 5) * 0.35;
        }
        int hp = (int)(baseHp * multiplier);
        enemyHP.add(hp);
    }

    private void spawnCoin() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        int x = (int)(Math.random() * (w - coin.getWidth()));
        int y = (int)(Math.random() * (h - coin.getHeight()));
        coins.add(new Point(x, y));
    }

    private void shootBullet() {
        if (enemyXs.isEmpty()) return;
        double px = playerX + player.getWidth() / 2.0;
        double py = playerY + player.getHeight() / 2.0;
        int closestIndex = 0;
        double closestDist = Double.MAX_VALUE;
        for (int i = 0; i < enemyXs.size(); i++) {
            double dx = enemyXs.get(i) - px;
            double dy = enemyYs.get(i) - py;
            double dist = dx * dx + dy * dy;
            if (dist < closestDist) {
                closestDist = dist;
                closestIndex = i;
            }
        }
        double dx = enemyXs.get(closestIndex) - px;
        double dy = enemyYs.get(closestIndex) - py;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            return;
        }

        dx /= length;
        dy /= length;
        addBullet(px, py, dx, dy, 15, 0);

        if (hasShotgun) {
            double spread = 0.5;

            addBullet(px, py, dx + spread, dy + spread, 4, 0);
            addBullet(px, py, dx - spread, dy - spread, 4, 0);
        }
    }

    private void showUpgradeChoices() {
        ArrayList<String> pool = new ArrayList<>();
        pool.add("Speed");
        pool.add("Bullet Damage");
        pool.add("Max HP");
        pool.add("Bullet Speed");

        if (level >= 5 && !hasShotgun) {
            pool.add("Shotgun");
        }

        for (int i = 0; i < 3; i++) {
            int r = (int)(Math.random() * pool.size());
            currentChoices[i] = pool.remove(r);
        }
        upgrade1.setText(currentChoices[0]);
        upgrade2.setText(currentChoices[1]);
        upgrade3.setText(currentChoices[2]);
        upgrade1.setVisible(true);
        upgrade2.setVisible(true);
        upgrade3.setVisible(true);
        upgrade1.setBounds(350, 300, 200, 40);
        upgrade2.setBounds(350, 350, 200, 40);
        upgrade3.setBounds(350, 400, 200, 40);
    }

    private void handleUpgrade(String type) {
        switch (type) {
            case "Shotgun":
                hasShotgun = true;
                break;
            case "Speed":
                playerSpeed += 0.7;
                break;
            case "Bullet Damage":
                bulletDamage += 1;
                break;
            case "Max HP":
                maxHP += 5;
                playerHP = maxHP;
                break;
            case "Bullet Speed":
                bulletSpeed += 2.0;
                break;
        }
        finishUpgrade();
    }

    private void finishUpgrade() {
        upgrade1.setVisible(false);
        upgrade2.setVisible(false);
        upgrade3.setVisible(false);
        timer.start();
        enemyTimer.start();
        shootTimer.start();
        coinTimer.start();
    }

    private void addBullet(double px, double py, double dx, double dy, int size, int life) {
        bullets.add(new Point((int) px, (int) py));
        bulletDirX.add(dx);
        bulletDirY.add(dy);
        bulletSize.add(size);
        bulletLife.add(life);
    }
}
