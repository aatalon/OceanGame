import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// only import the util classes we actually need (no Timer conflict)
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MatchingGame extends JFrame {

    // Board size
    private final int NUM_ROWS = 4;
    private final int NUM_COLS = 5;
    private final int NUM_CARDS = NUM_ROWS * NUM_COLS; // 20 cards → 10 pairs

    private JPanel boardPanel;
    private JLabel instructionsLabel;

    private JButton[] cardButtons = new JButton[NUM_CARDS];
    private String[] cardImageFiles = new String[NUM_CARDS];

    private Icon backIcon;
    private Map<String, Icon> frontIcons = new HashMap<>();

    private int firstCardIndex = -1;
    private boolean isBusy = false; // blocks clicks while cards are flipping back

    private int cardsLeft = NUM_CARDS; // used to check when game is finished

    public MatchingGame() {
        super("Ocean Animals Matching Game");

        // Prepare window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Instructions label at top
        instructionsLabel = new JLabel("Welcome! Click two cards to find a matching pair.", SwingConstants.CENTER);
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(instructionsLabel, BorderLayout.NORTH);

        // Board in center
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(NUM_ROWS, NUM_COLS, 10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(boardPanel, BorderLayout.CENTER);

        // Load icons and deck
        loadIcons();

        // Create buttons/cards
        setupCards();

        setVisible(true);
    }

    // Loads the back image and prepares the mapping for front images
    private void loadIcons() {
        // Card back image
        backIcon = makeIcon("card_back.png");

        // Ocean animal images (10 pairs)
        String[] animalFiles = {
            "dolphin.png",
            "turtle.png",
            "clownfish.png",
            "shark.png",
            "octopus.png",
            "jellyfish.png",
            "seahorse.png",
            "crab.png",
            "stingray.png",
            "starfish.png"
        };

        // Add each icon to the map for re-use
        for (String file : animalFiles) {
            frontIcons.put(file, makeIcon(file));
        }

        // Build deck: each image twice
        ArrayList<String> deckList = new ArrayList<>();
        for (String file : animalFiles) {
            deckList.add(file);
            deckList.add(file);
        }

        // Just in case someone changes NUM_CARDS
        if (deckList.size() != NUM_CARDS) {
            System.out.println("Warning: deck size does not match NUM_CARDS!");
        }

        // Shuffle the deck
        Collections.shuffle(deckList);

        // Copy into array
        for (int i = 0; i < NUM_CARDS; i++) {
            cardImageFiles[i] = deckList.get(i);
        }
    }

    // Helper to load and scale image to ~100x100 from /images/ folder
    private Icon makeIcon(String fileName) {
        java.net.URL imgURL = getClass().getResource("/images/" + fileName);
        if (imgURL == null) {
            System.err.println("Could not find image: " + fileName);
            return null;
        }
        ImageIcon icon = new ImageIcon(imgURL);
        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // Creates the JButtons and assigns behavior
    private void setupCards() {
        for (int i = 0; i < NUM_CARDS; i++) {
            JButton button = new JButton();
            button.setIcon(backIcon);
            button.setFocusPainted(false);
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            final int index = i; // for inner class

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleCardClick(index);
                }
            });

            cardButtons[i] = button;
            boardPanel.add(button);
        }
    }

    // Logic for when a card is clicked
    private void handleCardClick(int index) {
        if (isBusy) return;                        // ignore clicks while timer is running
        if (!cardButtons[index].isEnabled()) return;  // already matched
        if (index == firstCardIndex) return;       // same card

        // Flip this card face-up
        showCard(index);

        if (firstCardIndex == -1) {
            // First selection
            firstCardIndex = index;
            instructionsLabel.setText("Now select another card.");
        } else {
            // Second selection
            int secondCardIndex = index;
            isBusy = true; // temporarily block clicks

            if (cardImageFiles[firstCardIndex].equals(cardImageFiles[secondCardIndex])) {
                // MATCH
                handleMatch(firstCardIndex, secondCardIndex);
            } else {
                // NOT A MATCH
                handleNoMatch(firstCardIndex, secondCardIndex);
            }
        }
    }

    // Show the card's front image
    private void showCard(int index) {
        String fileName = cardImageFiles[index];
        Icon icon = frontIcons.get(fileName);
        cardButtons[index].setIcon(icon);
    }

    // Show the back image
    private void hideCard(int index) {
        cardButtons[index].setIcon(backIcon);
    }

    // Called when two cards match
    private void handleMatch(int i1, int i2) {
        // Disable the matched cards
        cardButtons[i1].setEnabled(false);
        cardButtons[i2].setEnabled(false);

        // Decrease cardsLeft by 2
        cardsLeft -= 2;

        if (cardsLeft == 0) {
            // REQUIRED: change Instructions to “Congratulations.”
            instructionsLabel.setText("Congratulations! You matched all the ocean animals! 🌊");
        } else {
            instructionsLabel.setText("Nice match! " + cardsLeft + " cards left.");
        }

        // Reset selection
        firstCardIndex = -1;
        isBusy = false;
    }

    // Called when two cards do NOT match
    private void handleNoMatch(final int i1, final int i2) {
        instructionsLabel.setText("Not a match. Cards will flip back.");

        // Wait a bit so player can see the second card, then flip both back
        javax.swing.Timer timer = new javax.swing.Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideCard(i1);
                hideCard(i2);

                firstCardIndex = -1;
                isBusy = false;
                instructionsLabel.setText("Try again! Find all the matching ocean animals.");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        // Make the UI look closer to the system style
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore if not available
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MatchingGame();
            }
        });
    }
}
