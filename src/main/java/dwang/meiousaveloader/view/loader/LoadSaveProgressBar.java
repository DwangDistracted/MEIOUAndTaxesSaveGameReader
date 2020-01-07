package dwang.meiousaveloader.view.loader;

import dwang.meiousaveloader.loader.SaveGameLoader;
import dwang.meiousaveloader.view.SwingUtils;
import dwang.meiousaveloader.view.browser.SaveGameSelector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadSaveProgressBar extends JFrame implements ActionListener {
    private SaveGameLoader saveGameLoader;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    Thread loadingThread;

    public LoadSaveProgressBar(SaveGameLoader saveGameLoader, String saveGameName) {
        this.saveGameLoader = saveGameLoader;
        loadingThread = new Thread(saveGameLoader);
        loadingThread.start();

        JLabel saveGameLabel = new JLabel("Loading " + saveGameName + "...");
        saveGameLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JPanel progressPanel = new JPanel(new BorderLayout());

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
        progressBar.setPreferredSize(new Dimension(SwingUtils.getWidthRelativeToScreen(6), SwingUtils.getHeightRelativeToScreen(20)));
        progressBar.setStringPainted(true);
        progressBar.setString("0%");

        statusLabel = new JLabel("blah blah blah...");
        statusLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);

        progressPanel.add(progressBar, BorderLayout.NORTH);
        progressPanel.add(statusLabel, BorderLayout.LINE_END);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        BorderLayout layout = new BorderLayout();
        this.setLayout(layout);
        this.add(saveGameLabel, BorderLayout.NORTH);
        this.add(progressPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        SwingUtils.setLocationToScreenCenter(this);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel loading?", "", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.NO_OPTION) {
            return;
        }

        saveGameLoader.setStatus(SaveGameLoader.LoadStatus.ABORTED);
        new SaveGameSelector();
        this.dispose();
    }
}
