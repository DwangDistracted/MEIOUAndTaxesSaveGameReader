package dwang.meiousaveloader.view.browser;

import com.google.common.base.Strings;
import dwang.meiousaveloader.constants.DirectoryConstants;
import dwang.meiousaveloader.constants.ProgramConstants;
import dwang.meiousaveloader.loader.SaveGameLoader;
import dwang.meiousaveloader.view.SwingUtils;
import dwang.meiousaveloader.view.loader.LoadSaveProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SaveGameSelector extends JFrame implements ActionListener {
    private static final Logger logger = LogManager.getLogger(SaveGameSelector.class);
    private Map<String, File> saveGames;
    private SelectSaveList saveList;

    public SaveGameSelector() {
        saveGames = new HashMap<>();

        DirectoryConstants.getSaveGameDirectory().ifPresent(
                (saveGameDir) -> {
                    File[] dirFiles = Objects.requireNonNull(saveGameDir.listFiles());

                    for (File file : dirFiles) {
                        if (file.isFile() && file.getName().endsWith(ProgramConstants.EU4_EXT)) {
                            saveGames.put(file.getName(), file);
                        }
                    }
                }
        );

        this.setTitle("MEIOU And Taxes - Save Game Reader");

        BorderLayout layout = new BorderLayout();
        layout.setVgap(10);
        this.setLayout(layout);

        this.add(new SelectSaveLabel(), BorderLayout.NORTH);

        LoadSaveButton loadButton = new LoadSaveButton(this);
        QuitButton quitButton = new QuitButton(this);

        saveList = new SelectSaveList(loadButton);
        JScrollPane listPanel = new JScrollPane(saveList);
        listPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(listPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loadButton);
        buttonPanel.add(quitButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        SwingUtils.setMinimumSizeRelativeToScreen(this, 3);
        this.setResizable(false);

        SwingUtils.setLocationToScreenCenter(this);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String selectedSaveName = saveList.getSelectedValue();
        if (Strings.isNullOrEmpty(selectedSaveName)) {
            return;
        }

        File saveFile = saveGames.get(selectedSaveName);

        if (Objects.nonNull(saveFile)) {
            logger.info("Loading Save File '" + selectedSaveName + "'");
            try {
                new LoadSaveProgressBar(new SaveGameLoader(saveFile, false), selectedSaveName);
                this.dispose();
            } catch (IOException exception) {
                logger.error("Failed to Load " + selectedSaveName);
            }
        } else {
            logger.warn("Could not find a save file with the name '" + selectedSaveName + "'");
        }
    }

    private static class SelectSaveLabel extends JLabel {
        SelectSaveLabel() {
            super("Select a Save Game:");
        }
    }

    private class SelectSaveList extends JList<String> {
        SelectSaveList(LoadSaveButton loadButton) {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addAll(saveGames.keySet());
            this.setModel(model);

            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            this.setLayoutOrientation(JList.VERTICAL);

            this.addListSelectionListener(loadButton);
        }
    }

    private class LoadSaveButton extends JButton implements ListSelectionListener {
        LoadSaveButton (SaveGameSelector listener) {
            this.setText("Load Save");
            this.addActionListener(listener);
            this.setEnabled(false);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!Strings.isNullOrEmpty(saveList.getSelectedValue())) {
                this.setEnabled(true);
            }
        }
    }

    private class QuitButton extends JButton {
        QuitButton (SaveGameSelector frame) {
            this.setText("Quit");
            this.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(frame, "Are You Sure You Want to Quit?",
                        "MEIOU And Taxes - Save Game Reader", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    frame.dispose();
                }
            });
        }
    }
}
