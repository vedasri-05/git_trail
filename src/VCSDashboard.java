// VCSDashboard.java - FIXED LAYOUT TO SHOW BUTTONS
package vcs.core;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class VCSDashboard extends JFrame {
    private VCSController vcsController;
    private JTextArea fileContentArea;
    private JTextField fileNameField;
    private JTextArea commitMessageArea;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JTextArea statusArea;
    private JTextArea commandOutput;
    private JTextField commandInput;
    
    public VCSDashboard() {
        initializeVCS();
        initializeUI();
        refreshAllViews();
    }
    
    private void initializeVCS() {
        this.vcsController = new VCSController("Developer");
    }
    
    private void initializeUI() {
        setTitle("Mini Version Control System - Git Commands Supported");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 1000); // Increased size
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Force layout refresh
        SwingUtilities.invokeLater(() -> {
            validate();
            repaint();
        });
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel titleLabel = new JLabel("🚀 Mini Version Control System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(60, 60, 60));
        
        JLabel subtitleLabel = new JLabel("Edit files → Stage changes → Commit with messages → Use Git commands");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(new Color(240, 240, 240));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(subtitleLabel);
        
        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }
    
    private JSplitPane createContentPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createFilePanel());
        splitPane.setRightComponent(createEditorPanel());
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.25);
        return splitPane;
    }
    
    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(
            new TitledBorder("Version Controlled Files"),
            new EmptyBorder(8, 8, 8, 8)
        ));
        panel.setPreferredSize(new Dimension(300, Integer.MAX_VALUE));
        
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setFont(new Font("Consolas", Font.PLAIN, 12));
        fileList.setBackground(Color.WHITE);
        fileList.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewSelectedFile();
                }
            }
        });
        
        JScrollPane fileScrollPane = new JScrollPane(fileList);
        
        JPanel fileOpsPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        fileOpsPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        JButton addButton = createButton("Add File", new Color(70, 130, 180));
        JButton removeButton = createButton("Remove", new Color(220, 80, 60));
        JButton viewButton = createButton("View", new Color(65, 150, 65));
        JButton refreshFilesButton = createButton("Refresh", new Color(120, 120, 120));
        
        addButton.addActionListener(e -> browseAndAddFile());
        removeButton.addActionListener(e -> removeSelectedFile());
        viewButton.addActionListener(e -> viewSelectedFile());
        refreshFilesButton.addActionListener(e -> refreshAllViews());
        
        fileOpsPanel.add(addButton);
        fileOpsPanel.add(removeButton);
        fileOpsPanel.add(viewButton);
        fileOpsPanel.add(refreshFilesButton);
        
        panel.add(new JLabel("Files (double-click to view):"), BorderLayout.NORTH);
        panel.add(fileScrollPane, BorderLayout.CENTER);
        panel.add(fileOpsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(
            new TitledBorder("File Editor - Make changes and save to version control"),
            new EmptyBorder(8, 8, 8, 8)
        ));
        
        JPanel fileInfoPanel = new JPanel(new BorderLayout(5, 5));
        fileInfoPanel.add(new JLabel("Currently Editing:"), BorderLayout.WEST);
        fileNameField = new JTextField();
        fileNameField.setFont(new Font("Consolas", Font.PLAIN, 12));
        fileNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        fileNameField.setEditable(false);
        fileNameField.setBackground(new Color(250, 250, 250));
        fileInfoPanel.add(fileNameField, BorderLayout.CENTER);
        
        fileContentArea = new JTextArea();
        fileContentArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        fileContentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        fileContentArea.setLineWrap(true);
        fileContentArea.setWrapStyleWord(true);
        fileContentArea.setTabSize(4);
        
        JScrollPane editorScrollPane = new JScrollPane(fileContentArea);
        
        JPanel editorOpsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        editorOpsPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        JButton saveButton = createButton("Save Changes", new Color(65, 150, 65));
        JButton clearButton = createButton("Clear Editor", new Color(220, 80, 60));
        JButton newFileButton = createButton("New File", new Color(70, 130, 180));
        JButton stageButton = createButton("Stage File", new Color(150, 120, 200));
        
        saveButton.addActionListener(e -> saveEditsToVersionControl());
        clearButton.addActionListener(e -> clearEditor());
        newFileButton.addActionListener(e -> createNewFile());
        stageButton.addActionListener(e -> stageCurrentFile());
        
        editorOpsPanel.add(saveButton);
        editorOpsPanel.add(clearButton);
        editorOpsPanel.add(newFileButton);
        editorOpsPanel.add(stageButton);
        
        panel.add(fileInfoPanel, BorderLayout.NORTH);
        panel.add(editorScrollPane, BorderLayout.CENTER);
        panel.add(editorOpsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(300, 350)); // Ensure footer has enough height
        
        JSplitPane footerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        footerSplitPane.setTopComponent(createGitCommandPanel());
        footerSplitPane.setBottomComponent(createOperationsPanel());
        footerSplitPane.setDividerLocation(180); // Adjusted divider
        footerSplitPane.setResizeWeight(0.5);
        
        panel.add(footerSplitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createGitCommandPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(
            new TitledBorder("Git Commands Terminal"),
            new EmptyBorder(8, 8, 8, 8)
        ));
        panel.setPreferredSize(new Dimension(300, 180));
        
        JPanel commandPanel = new JPanel(new BorderLayout(5, 5));
        commandInput = new JTextField();
        commandInput.setFont(new Font("Consolas", Font.PLAIN, 12));
        commandInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        commandInput.setText("git ");
        
        JButton executeButton = createButton("Execute", new Color(70, 130, 180));
        JButton clearButton = createButton("Clear", new Color(120, 120, 120));
        
        commandPanel.add(new JLabel("Command:"), BorderLayout.WEST);
        commandPanel.add(commandInput, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(executeButton);
        buttonPanel.add(clearButton);
        commandPanel.add(buttonPanel, BorderLayout.EAST);
        
        commandOutput = new JTextArea(4, 60);
        commandOutput.setFont(new Font("Consolas", Font.PLAIN, 11));
        commandOutput.setBackground(new Color(30, 30, 30));
        commandOutput.setForeground(new Color(220, 220, 220));
        commandOutput.setEditable(false);
        commandOutput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JScrollPane outputScrollPane = new JScrollPane(commandOutput);
        
        executeButton.addActionListener(e -> executeGitCommand());
        clearButton.addActionListener(e -> commandOutput.setText(""));
        commandInput.addActionListener(e -> executeGitCommand());
        
        panel.add(commandPanel, BorderLayout.NORTH);
        panel.add(outputScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOperationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new CompoundBorder(
            new TitledBorder("Version Control Operations"),
            new EmptyBorder(8, 8, 8, 8)
        ));
        panel.setPreferredSize(new Dimension(300, 200)); // Ensure enough height

        // Commit section
        JPanel commitPanel = new JPanel(new BorderLayout(5, 5));
        commitPanel.add(new JLabel("Commit Message:"), BorderLayout.WEST);
        commitMessageArea = new JTextArea(2, 40);
        commitMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        commitMessageArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        commitMessageArea.setLineWrap(true);
        commitMessageArea.setWrapStyleWord(true);

        JPanel commitButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        JButton commitButton = createButton("Commit", new Color(70, 130, 180));
        JButton stageAllButton = createButton("Stage All", new Color(150, 120, 200));

        commitButton.addActionListener(e -> commitStagedChanges());
        stageAllButton.addActionListener(e -> stageAllFiles());

        commitButtonPanel.add(stageAllButton);
        commitButtonPanel.add(commitButton);

        commitPanel.add(new JScrollPane(commitMessageArea), BorderLayout.CENTER);
        commitPanel.add(commitButtonPanel, BorderLayout.EAST);

        // Operations panel - FIXED LAYOUT TO SHOW BUTTONS
        JPanel opsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        opsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        opsPanel.setPreferredSize(new Dimension(300, 80)); // Fixed height for buttons

        // Create the 5 requested buttons with proper sizing
        JButton statusButton = createButton("Status", new Color(65, 150, 65));
        JButton logButton = createButton("Log", new Color(150, 120, 200));
        JButton branchButton = createButton("Branch", new Color(220, 150, 60));
        JButton exportButton = createButton("Export", new Color(120, 120, 120));
        JButton refreshButton = createButton("Refresh", new Color(70, 130, 180));

        // Set consistent button sizes
        Dimension buttonSize = new Dimension(80, 30);
        statusButton.setPreferredSize(buttonSize);
        logButton.setPreferredSize(buttonSize);
        branchButton.setPreferredSize(buttonSize);
        exportButton.setPreferredSize(buttonSize);
        refreshButton.setPreferredSize(buttonSize);

        statusButton.addActionListener(e -> showStatus());
        logButton.addActionListener(e -> showLog());
        branchButton.addActionListener(e -> createBranch());
        exportButton.addActionListener(e -> exportFiles());
        refreshButton.addActionListener(e -> refreshAllViews());

        // Add buttons to panel
        opsPanel.add(statusButton);
        opsPanel.add(logButton);
        opsPanel.add(branchButton);
        opsPanel.add(exportButton);
        opsPanel.add(refreshButton);

        // Status area
        statusArea = new JTextArea(3, 60);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusArea.setBackground(new Color(250, 250, 250));
        statusArea.setEditable(false);
        statusArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        statusArea.setText("Ready! Use GUI buttons or Git commands above.");

        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setPreferredSize(new Dimension(300, 80));

        // Main layout
        JPanel mainContent = new JPanel(new BorderLayout(5, 5));
        mainContent.add(commitPanel, BorderLayout.NORTH);
        mainContent.add(opsPanel, BorderLayout.CENTER);
        mainContent.add(statusScrollPane, BorderLayout.SOUTH);

        panel.add(mainContent, BorderLayout.CENTER);

        return panel;
    }
    
    // NEW: Show log in terminal
    private void showLog() {
        List<String> log = vcsController.getCommitLog();
        if (log.isEmpty()) {
            commandOutput.append("$ git log\nNo commits yet\n\n");
        } else {
            commandOutput.append("$ git log\n" + String.join("\n\n", log) + "\n\n");
        }
        commandOutput.setCaretPosition(commandOutput.getDocument().getLength());
    }
    
    private void executeGitCommand() {
        String command = commandInput.getText().trim();
        if (!command.isEmpty()) {
            String result = processGitCommand(command);
            commandOutput.append("$ " + command + "\n");
            commandOutput.append(result + "\n\n");
            commandInput.setText("git ");
            refreshAllViews();
            commandOutput.setCaretPosition(commandOutput.getDocument().getLength());
        }
    }
    
    private String processGitCommand(String command) {
        if (command.startsWith("git ")) {
            command = command.substring(4);
        }
        
        String[] parts = command.split("\\s+");
        if (parts.length == 0) return "";
        
        String baseCommand = parts[0].toLowerCase();
        
        try {
            switch (baseCommand) {
                case "init":
                    if (parts.length > 1) {
                        return vcsController.initRepository(parts[1]);
                    }
                    return vcsController.initRepository("my-project");
                    
                case "add":
                    if (parts.length > 1 && parts[1].equals(".")) {
                        vcsController.addAllFiles();
                        return "Added all files to staging area";
                    } else if (parts.length > 1) {
                        List<String> files = Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
                        vcsController.addFiles(files);
                        return "Staged files: " + String.join(", ", files);
                    }
                    return "Usage: add <file>... or add .";
                    
                case "commit":
                    if (parts.length > 1 && parts[1].equals("-m") && parts.length > 2) {
                        String message = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length))
                            .replace("\"", "");
                        String result = vcsController.commitStaged(message);
                        processSmartCommit(message);
                        return result;
                    }
                    return "Usage: commit -m \"message\"";
                    
                case "status":
                    Map<String, String> status = vcsController.getStatus();
                    StringBuilder statusText = new StringBuilder();
                    for (Map.Entry<String, String> entry : status.entrySet()) {
                        statusText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    }
                    return statusText.toString();
                    
                case "log":
                    List<String> log = vcsController.getCommitLog();
                    if (log.isEmpty()) {
                        return "No commits yet";
                    }
                    return String.join("\n\n", log);
                    
                case "branch":
                    if (parts.length == 1) {
                        List<String> branches = vcsController.listBranches();
                        return String.join("\n", branches);
                    } else if (parts.length == 2) {
                        return vcsController.createBranch(parts[1]);
                    } else if (parts.length == 3 && parts[1].equals("-d")) {
                        boolean deleted = vcsController.deleteBranch(parts[2]);
                        return deleted ? "Deleted branch " + parts[2] : "Failed to delete branch " + parts[2];
                    }
                    return "Usage: branch [name] or branch -d [name]";
                    
                case "checkout":
                    if (parts.length == 2) {
                        return vcsController.checkout(parts[1]);
                    }
                    return "Usage: checkout [branch|commit]";
                    
                case "merge":
                    if (parts.length == 2) {
                        return vcsController.merge(parts[1]);
                    }
                    return "Usage: merge [branch]";
                    
                case "remote":
                    if (parts.length >= 3 && parts[1].equals("add") && parts[2].equals("origin")) {
                        String url = parts.length > 3 ? parts[3] : "https://github.com/user/repo.git";
                        return vcsController.setRemote(url);
                    }
                    return "Usage: remote add origin [url]";
                    
                case "fetch":
                    return vcsController.fetch();
                    
                case "pull":
                    return vcsController.pull();
                    
                case "push":
                    return vcsController.push();
                    
                case "help":
                    return getGitHelp();
                    
                default:
                    return "Unknown command: " + baseCommand + "\n" + getGitHelp();
            }
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
    
    private String getGitHelp() {
        return "Available Git Commands:\n" +
               "  init [name]          - Initialize new repository\n" +
               "  add <file>...        - Stage files for commit\n" +
               "  add .                - Stage all files\n" +
               "  commit -m \"message\"  - Commit staged changes\n" +
               "  status               - Show working tree status\n" +
               "  log                  - Show commit history\n" +
               "  branch               - List branches\n" +
               "  branch [name]        - Create new branch\n" +
               "  branch -d [name]     - Delete branch\n" +
               "  checkout [branch]    - Switch branches\n" +
               "  merge [branch]       - Merge branches\n" +
               "  remote add origin [url] - Set remote repository\n" +
               "  fetch                - Download from remote\n" +
               "  pull                 - Fetch and merge\n" +
               "  push                 - Update remote\n" +
               "  help                 - Show this help";
    }
    
    private void processSmartCommit(String message) {
        message = message.toLowerCase();
        if (message.contains("feat:") || message.contains("feature:")) {
            statusArea.setText("🚀 Feature commit - consider updating documentation");
        } else if (message.contains("fix:") || message.contains("bug:")) {
            statusArea.setText("🐛 Bug fix - remember to test the fix thoroughly");
        } else if (message.contains("docs:")) {
            statusArea.setText("📚 Documentation update - verify examples work");
        } else if (message.contains("style:")) {
            statusArea.setText("🎨 Code style - no functional changes");
        } else if (message.contains("refactor:")) {
            statusArea.setText("🔧 Refactoring - ensure all tests pass");
        } else if (message.contains("test:")) {
            statusArea.setText("✅ Test updates - verify coverage improved");
        } else if (message.contains("chore:")) {
            statusArea.setText("📦 Maintenance tasks completed");
        } else if (message.contains("initial") || message.contains("init")) {
            statusArea.setText("🎉 Initial commit - project started!");
        }
    }
    
    private void stageCurrentFile() {
        String currentFile = fileNameField.getText().trim();
        if (!currentFile.isEmpty()) {
            List<String> files = Arrays.asList(currentFile);
            vcsController.addFiles(files);
            statusArea.setText("Staged file: " + currentFile);
        } else {
            statusArea.setText("No file selected to stage");
        }
    }
    
    private void stageAllFiles() {
        vcsController.addAllFiles();
        statusArea.setText("Staged all files");
    }
    
    private void commitStagedChanges() {
        String message = commitMessageArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a commit message.", 
                "Missing Commit Message", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String result = vcsController.commitStaged(message);
        statusArea.setText(result);
        commitMessageArea.setText("");
        refreshAllViews();
    }
    
    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brighter(color));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private Color brighter(Color color) {
        return new Color(
            Math.min(color.getRed() + 20, 255),
            Math.min(color.getGreen() + 20, 255),
            Math.min(color.getBlue() + 20, 255)
        );
    }
    
    private void browseAndAddFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select files to add to version control");
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length > 0) {
                int addedCount = 0;
                for (File file : selectedFiles) {
                    if (file.isFile()) {
                        if (addFileToVCS(file)) {
                            addedCount++;
                        }
                    } else if (file.isDirectory()) {
                        addedCount += addDirectoryToVCS(file);
                    }
                }
                statusArea.setText("Added " + addedCount + " file(s) to version control");
                refreshAllViews();
            }
        }
    }
    
    private boolean addFileToVCS(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            String versionId = vcsController.getFileManager().addFile(file.getName(), content);
            return true;
        } catch (IOException e) {
            statusArea.setText("Error reading file: " + file.getName() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            statusArea.setText("Error adding file: " + file.getName() + " - " + e.getMessage());
            return false;
        }
    }
    
    private int addDirectoryToVCS(File directory) {
        int addedCount = 0;
        try {
            Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    File file = path.toFile();
                    if (addFileToVCS(file)) {
                        // Count will be updated in the calling method
                    }
                });
            addedCount = (int) Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .count();
        } catch (IOException e) {
            statusArea.setText("Error reading directory: " + directory.getName());
        }
        return addedCount;
    }
    
    private void viewSelectedFile() {
        String selectedFile = fileList.getSelectedValue();
        if (selectedFile != null) {
            FileVersionManager.FileMetadata file = vcsController.getFileManager().getFile(selectedFile);
            if (file != null) {
                fileNameField.setText(file.fileName);
                fileContentArea.setText(file.content);
                statusArea.setText("Viewing: " + selectedFile + " | Last modified: " + file.lastModified);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a file from the version control list to view.", 
                "No File Selected", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void removeSelectedFile() {
        String selectedFile = fileList.getSelectedValue();
        if (selectedFile != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Remove '" + selectedFile + "' from version control?\n\nThis will delete it from version history.", 
                "Confirm Removal", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                String versionId = vcsController.getFileManager().deleteFile(selectedFile);
                statusArea.setText("Removed '" + selectedFile + "' from version control");
                
                if (selectedFile.equals(fileNameField.getText())) {
                    fileNameField.setText("");
                    fileContentArea.setText("File was removed from version control. Select another file or add new files.");
                }
                
                refreshAllViews();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a file from the list to remove.", 
                "No File Selected", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void createNewFile() {
        String fileName = JOptionPane.showInputDialog(this,
            "Enter name for new file (e.g., MyClass.java, notes.txt):",
            "Create New File",
            JOptionPane.QUESTION_MESSAGE);
            
        if (fileName != null && !fileName.trim().isEmpty()) {
            fileNameField.setText(fileName.trim());
            fileContentArea.setText("// New file: " + fileName + "\n// Created: " + new Date() + "\n\n// Start editing here...");
            statusArea.setText("Ready to edit new file: " + fileName + " - Make changes and click 'Save Changes'");
        }
    }
    
    private void clearEditor() {
        fileNameField.setText("");
        fileContentArea.setText("");
        statusArea.setText("Editor cleared. Select a file to edit or create a new one.");
    }
    
    private void saveEditsToVersionControl() {
        String currentFileName = fileNameField.getText().trim();
        String newContent = fileContentArea.getText();
        
        if (currentFileName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No file selected!\n\nEither:\n• Select a file from the list\n• Or click 'Create New File'",
                "No File Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (newContent.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "File content is empty! Please add some content before saving.",
                "Empty File",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (vcsController.getFileManager().fileExists(currentFileName)) {
            String versionId = vcsController.getFileManager().modifyFile(currentFileName, newContent);
            statusArea.setText("Changes saved to version control: " + currentFileName + " | Version: " + versionId);
        } else {
            String versionId = vcsController.getFileManager().addFile(currentFileName, newContent);
            statusArea.setText("New file added to version control: " + currentFileName + " | Version: " + versionId);
        }
        
        refreshAllViews();
    }
    
    private void exportFiles() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setDialogTitle("Select folder to export version-controlled files");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        int result = folderChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File exportFolder = folderChooser.getSelectedFile();
            if (!exportFolder.exists()) {
                exportFolder.mkdirs();
            }
            
            int exportedCount = exportFilesToFolder(exportFolder);
            statusArea.setText("✅ Exported " + exportedCount + " files to: " + exportFolder.getAbsolutePath());
        }
    }
    
    private int exportFilesToFolder(File folder) {
        int count = 0;
        try {
            List<String> files = vcsController.getCurrentFiles();
            for (String fileName : files) {
                FileVersionManager.FileMetadata fileData = vcsController.getFileManager().getFile(fileName);
                if (fileData != null && fileData.content != null) {
                    File outputFile = new File(folder, fileName);
                    Files.write(outputFile.toPath(), fileData.content.getBytes());
                    count++;
                }
            }
        } catch (IOException e) {
            statusArea.setText("Export failed: " + e.getMessage());
        }
        return count;
    }
    
    private void showStatus() {
        Map<String, String> status = vcsController.getStatus();
        StringBuilder statusText = new StringBuilder("Repository Status:\n");
        
        for (Map.Entry<String, String> entry : status.entrySet()) {
            statusText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        statusArea.setText(statusText.toString());
    }
    
    private void createBranch() {
        String branchName = JOptionPane.showInputDialog(this, 
            "Enter new branch name:", "Create Branch", JOptionPane.QUESTION_MESSAGE);
        
        if (branchName != null && !branchName.trim().isEmpty()) {
            String result = vcsController.createBranch(branchName.trim());
            statusArea.setText(result);
            refreshAllViews();
        }
    }
    
    private void refreshAllViews() {
        fileListModel.clear();
        List<String> files = vcsController.getCurrentFiles();
        for (String file : files) {
            fileListModel.addElement(file);
        }
        
        statusArea.setText("🔄 Refreshed at: " + new java.util.Date() + " | Files: " + files.size());
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new VCSDashboard().setVisible(true);
        });
    }
}