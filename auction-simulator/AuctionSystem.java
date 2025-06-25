import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AuctionSystem {
    static class Product {
        String name, description;
        double basePrice, currentBid;
        String highestBidder;
        boolean bidPlaced;
        long lastBidTime;

        public Product(String name, String desc, double basePrice) {
            this.name = name;
            this.description = desc;
            this.basePrice = basePrice;
            this.currentBid = basePrice;
            this.highestBidder = "None";
            this.bidPlaced = false;
            this.lastBidTime = System.currentTimeMillis();
        }

        public void placeBid(double amount, String bidderName) {
            if (amount > currentBid && !bidderName.equals(highestBidder)) {
                currentBid = amount;
                highestBidder = bidderName;
                bidPlaced = true;
                lastBidTime = System.currentTimeMillis();
            }
        }

        public boolean hasAnyBid() {
            return !highestBidder.equals("None") && currentBid > basePrice;
        }
    }

    static class User {
        String name;
        double purse;
        boolean activeBidder;

        public User(String name, double purse) {
            this.name = name;
            this.purse = purse;
            this.activeBidder = true;
        }

        public boolean canBid(double amt) {
            return amt <= purse;
        }

        public void deduct(double amt) {
            purse -= amt;
        }
    }

    static class AuctionGUI extends JFrame {
        private JLabel productLabel, descLabel, basePriceLabel, bidLabel, highestBidderLabel, timerLabel, purseLabel, goneLabel;
        private JTextField bidField;
        private JButton bidButton;

        private ArrayList<Product> products;
        private ArrayList<User> dummyUsers;
        private User realUser;

        private int productIndex = 0;
        private Product currentProduct;
        private Timer countdownTimer;
        private int timeLeft = 60;
        private Random rand = new Random();

        public AuctionGUI() {
            setTitle("Auction System");
            setSize(500, 380);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(10, 1));

            productLabel = new JLabel();
            descLabel = new JLabel();
            basePriceLabel = new JLabel();
            bidLabel = new JLabel();
            highestBidderLabel = new JLabel();
            purseLabel = new JLabel();
            timerLabel = new JLabel();
            goneLabel = new JLabel();
            bidField = new JTextField();
            bidButton = new JButton("Place Bid");

            add(productLabel);
            add(descLabel);
            add(basePriceLabel);
            add(bidLabel);
            add(highestBidderLabel);
            add(timerLabel);
            add(purseLabel);
            add(goneLabel);
            add(bidField);
            add(bidButton);

            bidButton.addActionListener(e -> handleUserBid());

            initData();
            startAuction();
            setVisible(true);
        }

        private void initData() {
            realUser = new User("You", 10000);

            dummyUsers = new ArrayList<>();
            dummyUsers.add(new User("Dummy1", 8000));
            dummyUsers.add(new User("Dummy2", 6000));
            dummyUsers.add(new User("Dummy3", 5000));
            dummyUsers.add(new User("Dummy4", 7000));

            products = new ArrayList<>();
            products.add(new Product("Smartphone", "6GB RAM, 128GB", 4000));
            products.add(new Product("Laptop", "i5, 8GB RAM", 2000));
            products.add(new Product("Speaker", "Bluetooth 10W", 1500));
            products.add(new Product("Watch", "Analog, Waterproof", 1200));
            products.add(new Product("Headphones", "Over-ear", 2500));
            products.add(new Product("Keyboard", "Mechanical RGB", 2200));
            products.add(new Product("Mouse", "Gaming 7-button", 900));
            products.add(new Product("Hard Drive", "1TB USB 3.0", 3800));
            products.add(new Product("Backpack", "Laptop 32L", 1100));
            products.add(new Product("Kettle", "1.5L Electric", 1300));
            Collections.shuffle(products);
        }

        private void startAuction() {
            if (productIndex >= products.size()) {
                JOptionPane.showMessageDialog(this, "Auction complete!");
                System.exit(0);
            }

            currentProduct = products.get(productIndex);
            currentProduct.bidPlaced = false;
            currentProduct.lastBidTime = System.currentTimeMillis();
            timeLeft = 60;
            goneLabel.setText("");

            for (User d : dummyUsers) {
                d.activeBidder = rand.nextBoolean();
            }

            updateDisplay();
            startTimer();
            startDummyBidders();
        }

        private void updateDisplay() {
            productLabel.setText("Product: " + currentProduct.name);
            descLabel.setText("Description: " + currentProduct.description);
            basePriceLabel.setText("Base Price: ₹" + currentProduct.basePrice);
            bidLabel.setText("Current Bid: ₹" + currentProduct.currentBid);
            highestBidderLabel.setText("Highest Bidder: " + currentProduct.highestBidder);
            timerLabel.setText("Time left: " + timeLeft + "s");
            purseLabel.setText("Your Purse: ₹" + realUser.purse);
        }

        private void startTimer() {
            countdownTimer = new Timer(1000, e -> {
                timeLeft--;
                timerLabel.setText("Time left: " + timeLeft + "s");

                long idleTime = (System.currentTimeMillis() - currentProduct.lastBidTime) / 1000;
                if (currentProduct.hasAnyBid()) {
                    if (idleTime >= 10 && idleTime < 11) goneLabel.setText("Gone once!");
                    else if (idleTime >= 11 && idleTime < 13) goneLabel.setText("Gone twice!");
                    else if (idleTime >= 13) {
                        goneLabel.setText("Gone!");
                        countdownTimer.stop();
                        concludeAuction();
                        return;
                    } else {
                        goneLabel.setText("");
                    }
                } else {
                    goneLabel.setText("");
                }

                if (timeLeft <= 0) {
                    countdownTimer.stop();
                    concludeAuction();
                }
            });
            countdownTimer.start();
        }

        private void startDummyBidders() {
            for (User d : dummyUsers) {
                new Thread(() -> {
                    try {
                        Thread.sleep(rand.nextInt(15000) + 5000);
                    } catch (Exception ignored) {}

                    while (timeLeft > 0 && d.activeBidder) {
                        try {
                            Thread.sleep(rand.nextInt(3000) + 3000);
                        } catch (Exception ignored) {}

                        double maxBid = d.purse * 0.6;
                        double bidAttempt = currentProduct.currentBid + rand.nextInt(500) + 100;
                        if (bidAttempt > maxBid) continue;

                        synchronized (currentProduct) {
                            if (d.canBid(bidAttempt) && bidAttempt > currentProduct.currentBid && !d.name.equals(currentProduct.highestBidder)) {
                                currentProduct.placeBid(bidAttempt, d.name);
                                SwingUtilities.invokeLater(this::updateDisplay);
                            }
                        }
                    }
                }).start();
            }
        }

        private void handleUserBid() {
            try {
                double bid = Double.parseDouble(bidField.getText());
                if (realUser.name.equals(currentProduct.highestBidder)) {
                    JOptionPane.showMessageDialog(this, "You are already the highest bidder.");
                } else if (bid <= currentProduct.currentBid || !realUser.canBid(bid)) {
                    JOptionPane.showMessageDialog(this, "Invalid bid amount.");
                } else {
                    currentProduct.placeBid(bid, realUser.name);
                    updateDisplay();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter valid number.");
            } finally {
                bidField.setText("");
            }
        }

        private void concludeAuction() {
            String winner = currentProduct.highestBidder;
            double finalPrice = currentProduct.currentBid;

            if (!currentProduct.hasAnyBid()) {
                JOptionPane.showMessageDialog(this, currentProduct.name + " was UNSOLD.");
            } else {
                if (winner.equals(realUser.name)) {
                    realUser.deduct(finalPrice);
                } else {
                    for (User d : dummyUsers) {
                        if (d.name.equals(winner)) {
                            d.deduct(finalPrice);
                            break;
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Winner: " + winner + " for ₹" + finalPrice);
            }

            productIndex++;
            startAuction();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuctionGUI());
    }
}
