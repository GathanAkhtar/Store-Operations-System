package Project;

import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame {
    
    private static final Color GOLD = new Color(244, 196, 48);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color DARK_GRAY = new Color(168, 168, 168);
    
    private LoginSystem loginSystem;
    private Runnable onLoginSuccess;
    private JTextField userField;
    private JPasswordField passField;
    
    public LoginGUI(LoginSystem loginSys, Runnable onSuccess) {
        this.loginSystem = loginSys;
        this.onLoginSuccess = onSuccess;
        
        setTitle("GoldenHour Login");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel main = new JPanel(new GridLayout(1, 2));
        main.add(createLeftPanel());
        main.add(createRightPanel());
        
        add(main);
        setVisible(true);
    }
    
    private JPanel createLeftPanel() {
        JPanel p = new JPanel();
        p.setBackground(GOLD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createVerticalGlue());
        p.add(makeLabel("GOLDEN HOUR", 36, Color.WHITE, true));
        p.add(makeLabel("STORE MANAGEMENT", 28, Color.WHITE, true));
        p.add(makeLabel("SYSTEM", 32, Color.WHITE, true));
        p.add(Box.createVerticalGlue());
        return p;
    }
    
    private JPanel createRightPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(LIGHT_GRAY);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        
        // Welcome Title
        c.gridy = 0; 
        c.insets = new Insets(20, 40, 5, 40);
        p.add(makeLabel("Welcome!", 28, Color.BLACK, true), c);
        
        c.gridy = 1; 
        c.insets = new Insets(5, 40, 3, 40);
        p.add(makeLabel("Enter Your ID and Password", 16, Color.BLACK, true), c);
        
        c.gridy = 2; 
        c.insets = new Insets(3, 40, 15, 40);
        p.add(makeLabel("Fill in your credentials below", 12, DARK_GRAY, true), c);
        
        // User ID
        c.gridy = 3; 
        c.insets = new Insets(10, 40, 5, 40);
        p.add(makeLabel("Enter User ID", 13, Color.BLACK, false), c);
        
        userField = new JTextField();
        styleField(userField);
        c.gridy = 4; 
        c.insets = new Insets(5, 40, 10, 40);
        p.add(userField, c);
        
        // Password
        c.gridy = 5; 
        c.insets = new Insets(10, 40, 5, 40);
        p.add(makeLabel("Password", 13, Color.BLACK, false), c);
        
        passField = new JPasswordField();
        styleField(passField);
        passField.addActionListener(e -> login());
        c.gridy = 6; 
        c.insets = new Insets(5, 40, 15, 40);
        p.add(passField, c);
        
        // Login Button
        JButton loginBtn = makeButton("Login", GOLD, Color.WHITE, true);
        loginBtn.addActionListener(e -> login());
        c.gridy = 7; 
        c.insets = new Insets(15, 40, 8, 40);
        p.add(loginBtn, c);
        
        // Exit Button
        JButton exitBtn = makeButton("Exit", LIGHT_GRAY, GOLD, false);
        exitBtn.addActionListener(e -> System.exit(0));
        c.gridy = 8; 
        c.insets = new Insets(8, 40, 20, 40);
        p.add(exitBtn, c);
        
        return p;
    }
    
    private JLabel makeLabel(String text, int size, Color color, boolean center) {
        JLabel l = new JLabel(text, center ? SwingConstants.CENTER : SwingConstants.LEFT);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(color);
        if (center) l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
    
    private void styleField(JTextField f) {
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(221, 221, 221)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        f.setPreferredSize(new Dimension(350, 45));
    }
    
    private JButton makeButton(String txt, Color bg, Color fg, boolean fill) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (fill) {
                    g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                } else {
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(255, 248, 231));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    }
                    g2.setColor(fg);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 25, 25);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(350, 45));
        return b;
    }
    
    private void login() {
        String id = userField.getText().trim();
        String pass = new String(passField.getPassword());
        
        if (id.isEmpty() || pass.isEmpty()) {
            showDialog("Login Failed", "Please fill in all fields!", false);
            return;
        }
        
        if (loginSystem.validateLogin(id, pass)) {
            showDialog("Login Success", "Welcome " + loginSystem.getCurrentUser().getName() + "!", true);
        } else {
            showDialog("Login Failed", "Please Try Again", false);
        }
    }
    
    private void showDialog(String title, String msg, boolean success) {
        JPanel glassPane = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glassPane.setOpaque(false);
        
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        card.setBounds((1000-400)/2, (600-250)/2, 400, 250);
        
        JLabel titleLbl = makeLabel(title, 22, Color.BLACK, true);
        titleLbl.setOpaque(false);
        JLabel msgLbl = makeLabel(msg, 14, DARK_GRAY, true);
        msgLbl.setOpaque(false);
        
        JButton btn = makeButton("Continue", GOLD, Color.WHITE, true);
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            setGlassPane(new JPanel());
            getGlassPane().setVisible(false);
            if (success) {
                this.dispose();
                onLoginSuccess.run();
            }
        });
        
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLbl);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(msgLbl);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(btn);
        
        glassPane.add(card);
        setGlassPane(glassPane);
        getGlassPane().setVisible(true);
    }
}