// DemoRunner.java
package vcs.core;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new VCSDashboard().setVisible(true);
        });
    }
}