package org.example.gui;

import org.example.dao.DatabaseManager;
import org.example.model.*;
import org.example.service.CrewAssignmentService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MainWindow extends JFrame {
    private JComboBox<Mission> missionCombo;

    private JList<CrewAssignment> currentCrewList;
    private DefaultListModel<CrewAssignment> currentCrewModel;

    private JList<CrewMember> allCrewList;
    private DefaultListModel<CrewMember> allCrewModel;

    private JButton btnAdd, btnRemove;
    private final CrewAssignmentService assignments = new CrewAssignmentService();
    private boolean busy;
    private boolean populatingMissions;

    public MainWindow() {
        setTitle("Mission Control Center - System Zarządzania Agencją Kosmiczną");

        var logo = getClass().getResource("/logo.jpg");
        if (logo != null) setIconImage(new ImageIcon(logo).getImage());

        setSize(900, 550);
        setMinimumSize(new Dimension(750, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(245, 247, 250));

        JLabel lblMission = new JLabel("Aktualnie zarządzana misja: ");
        lblMission.setFont(new Font("Segoe UI", Font.BOLD, 13));

        missionCombo = new JComboBox<>();
        missionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        missionCombo.setPreferredSize(new Dimension(250, 28));
        missionCombo.addActionListener(e -> {
            if (!populatingMissions && !busy) refreshCurrentCrewList();
        });

        topPanel.add(lblMission);
        topPanel.add(missionCombo);
        add(topPanel, BorderLayout.NORTH);

        currentCrewModel = new DefaultListModel<>();
        currentCrewList = new JList<>(currentCrewModel);
        currentCrewList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        currentCrewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane currentCrewScroll = new JScrollPane(currentCrewList);
        currentCrewScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Załoga przypisana do misji",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));

        allCrewModel = new DefaultListModel<>();
        allCrewList = new JList<>(allCrewModel);
        allCrewList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        allCrewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane allCrewScroll = new JScrollPane(allCrewList);
        allCrewScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Rejestr wszystkich astronautów",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        btnAdd = new JButton("<<< Dodaj");
        btnRemove = new JButton("Usuń >>>");

        prepareButton(btnAdd, new Color(46, 204, 113));
        prepareButton(btnRemove, new Color(231, 76, 60));

        btnAdd.addActionListener(e -> addCrewToMission());
        btnRemove.addActionListener(e -> removeCrewFromMission());

        controlPanel.add(Box.createVerticalGlue());
        controlPanel.add(btnAdd);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        controlPanel.add(btnRemove);
        controlPanel.add(Box.createVerticalGlue());

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.add(currentCrewScroll, BorderLayout.CENTER);

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.add(allCrewScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftContainer, rightContainer);
        splitPane.setDividerLocation(420);
        splitPane.setResizeWeight(0.5);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(splitPane, BorderLayout.CENTER);
        centerPanel.add(controlPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        JLabel statusLabel = new JLabel(" Wybierz misję i astronautę, aby zarządzać przydziałami.");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void prepareButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(110, 35));
        button.setPreferredSize(new Dimension(110, 35));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private record InitialData(List<Mission> missions, List<CrewMember> crew) {}

    private void loadInitialData() {
        runTask(() -> new InitialData(DatabaseManager.getAllMissions(),
                DatabaseManager.getAllAvailableCrew()), data -> {
            populatingMissions = true;
            try {
                missionCombo.removeAllItems();
                data.missions().forEach(missionCombo::addItem);
                allCrewModel.clear();
                data.crew().forEach(allCrewModel::addElement);
            } finally {
                populatingMissions = false;
            }
            refreshCurrentCrewList();
        });
    }

    private void refreshCurrentCrewList() {
        currentCrewModel.clear();
        Mission selected = (Mission) missionCombo.getSelectedItem();
        if (selected == null) return;
        runTask(() -> DatabaseManager.getMissionWithAssignments(selected.getId()), mission -> {
            if (mission != null) mission.getAssignments().forEach(currentCrewModel::addElement);
        });
    }

    private void addCrewToMission() {
        Mission mission = (Mission) missionCombo.getSelectedItem();
        CrewMember member = allCrewList.getSelectedValue();
        if (mission == null || member == null) {
            JOptionPane.showMessageDialog(this, "Wybierz misję i astronautę.");
            return;
        }
        runTask(() -> {
            assignments.add(mission.getId(), member.getId());
            return Boolean.TRUE;
        }, ignored -> refreshCurrentCrewList());
    }

    private void removeCrewFromMission() {
        CrewAssignment selected = currentCrewList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Wybierz przydział do usunięcia.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Usunąć przydział astronauty " + selected.getCrewMember().getName() + "?",
                "Usunięcie przydziału", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        runTask(() -> {
            assignments.remove(selected.getId());
            return Boolean.TRUE;
        }, ignored -> refreshCurrentCrewList());
    }

    private void setBusy(boolean value) {
        busy = value;
        btnAdd.setEnabled(!value);
        btnRemove.setEnabled(!value);
        missionCombo.setEnabled(!value);
        allCrewList.setEnabled(!value);
        currentCrewList.setEnabled(!value);
        setCursor(Cursor.getPredefinedCursor(value ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private <T> void runTask(Supplier<T> operation, Consumer<T> onSuccess) {
        if (busy) return;
        setBusy(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() { return operation.get(); }

            @Override protected void done() {
                setBusy(false);
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void showError(Throwable error) {
        boolean validation = error instanceof IllegalArgumentException
                || error instanceof IllegalStateException;
        if (!validation) error.printStackTrace();
        JOptionPane.showMessageDialog(this,
                validation ? error.getMessage() : "Operacja bazy danych nie powiodła się. Szczegóły zapisano w konsoli.",
                validation ? "Nie można wykonać operacji" : "Błąd bazy danych",
                validation ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }
}
