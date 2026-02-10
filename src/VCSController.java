// VCSController.java
package vcs.core;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class VCSController {
    private FileVersionManager fileManager;
    private Map<String, String> branches;
    private String currentBranch;
    private String remoteUrl;
    private boolean hasRemote;
    
    // Staging area for git-like operations
    private Set<String> stagedFiles;
    
    public VCSController(String author) {
        this.fileManager = new FileVersionManager(author);
        this.branches = new HashMap<>();
        this.currentBranch = "main";
        this.branches.put("main", fileManager.getCurrentVersion());
        this.stagedFiles = new HashSet<>();
        this.remoteUrl = null;
        this.hasRemote = false;
    }
    
    // Git-like init operation
    public String initRepository(String repoName) {
        String initMessage = "Initialized empty VCS repository: " + repoName;
        return fileManager.commitChanges(initMessage);
    }
    
    // Git-like add operation (stage files)
    public void addFiles(List<String> fileNames) {
        for (String fileName : fileNames) {
            if (fileManager.fileExists(fileName)) {
                stagedFiles.add(fileName);
            } else {
                // If file doesn't exist but is in current files, stage it
                List<String> currentFiles = getCurrentFiles();
                if (currentFiles.contains(fileName)) {
                    stagedFiles.add(fileName);
                }
            }
        }
    }
    
    public void addAllFiles() {
        List<String> allFiles = getCurrentFiles();
        stagedFiles.addAll(allFiles);
    }
    
    // Git-like commit with staged files
    public String commitStaged(String message) {
        if (stagedFiles.isEmpty()) {
            return "No changes staged for commit";
        }
        
        StringBuilder commitMessage = new StringBuilder("Commit: " + message + " | Files: ");
        for (String file : stagedFiles) {
            commitMessage.append(file).append(", ");
        }
        
        String commitId = fileManager.commitChanges(commitMessage.toString());
        stagedFiles.clear(); // Clear staging area after commit
        branches.put(currentBranch, commitId);
        return commitId;
    }
    
    // Git-like status
    public Map<String, String> getStatus() {
        Map<String, String> status = new LinkedHashMap<>();
        List<String> allFiles = getCurrentFiles();
        
        // Show branch info
        status.put("On branch", currentBranch);
        
        // Show staged files
        if (!stagedFiles.isEmpty()) {
            status.put("Changes to be committed", String.join(", ", new ArrayList<>(stagedFiles)));
        }
        
        // Show modified but not staged files
        List<String> unstagedFiles = new ArrayList<>();
        for (String file : allFiles) {
            if (!stagedFiles.contains(file)) {
                unstagedFiles.add(file);
            }
        }
        
        if (!unstagedFiles.isEmpty()) {
            status.put("Changes not staged for commit", String.join(", ", unstagedFiles));
        }
        
        if (stagedFiles.isEmpty() && unstagedFiles.isEmpty()) {
            status.put("Working tree", "clean");
        }
        
        return status;
    }
    
    // Git-like log
    public List<String> getCommitLog() {
        List<String> logEntries = new ArrayList<>();
        Map<String, String> history = fileManager.getVersionHistory();
        
        for (Map.Entry<String, String> entry : history.entrySet()) {
            logEntries.add("commit " + entry.getKey() + "\n    " + entry.getValue());
        }
        
        Collections.reverse(logEntries); // Show latest first
        return logEntries;
    }
    
    // Enhanced branch operations
    public String createBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            branches.put(branchName, fileManager.getCurrentVersion());
            return "Created new branch: " + branchName;
        }
        return "Branch already exists: " + branchName;
    }
    
    public List<String> listBranches() {
        List<String> branchList = new ArrayList<>();
        for (String branch : branches.keySet()) {
            if (branch.equals(currentBranch)) {
                branchList.add("* " + branch);
            } else {
                branchList.add("  " + branch);
            }
        }
        return branchList;
    }
    
    public boolean deleteBranch(String branchName) {
        if (!branchName.equals(currentBranch) && branches.containsKey(branchName)) {
            branches.remove(branchName);
            return true;
        }
        return false;
    }
    
    // Enhanced checkout
    public String checkout(String target) {
        // Check if target is a branch
        if (branches.containsKey(target)) {
            String version = branches.get(target);
            boolean success = fileManager.checkoutVersion(version);
            if (success) {
                currentBranch = target;
                return "Switched to branch '" + target + "'";
            }
        }
        
        // Check if target is a version ID
        if (versionExists(target)) {
            boolean success = fileManager.checkoutVersion(target);
            if (success) {
                return "HEAD is now at " + target;
            }
        }
        
        return "error: pathspec '" + target + "' did not match any file(s) known to git";
    }
    
    // Check if version exists
    private boolean versionExists(String versionId) {
        Map<String, String> history = fileManager.getVersionHistory();
        return history.containsKey(versionId);
    }
    
    // Git-like merge
    public String merge(String sourceBranch) {
        if (!branches.containsKey(sourceBranch)) {
            return "Branch '" + sourceBranch + "' not found";
        }
        
        if (sourceBranch.equals(currentBranch)) {
            return "Cannot merge a branch with itself";
        }
        
        String sourceVersion = branches.get(sourceBranch);
        String currentVersion = branches.get(currentBranch);
        
        // Get files from source branch
        List<String> sourceFiles = fileManager.getFilesInVersion(sourceVersion);
        
        // Merge files from source branch - FIXED LOGIC
        int mergedFiles = 0;
        for (String file : sourceFiles) {
            FileVersionManager.FileMetadata sourceFile = fileManager.getFile(file, sourceVersion);
            if (sourceFile != null) {
                // Get current file content for comparison
                FileVersionManager.FileMetadata currentFile = fileManager.getFile(file);
                
                // FIX: Check if files are different or if current file doesn't exist
                boolean needsUpdate = currentFile == null || 
                                    !sourceFile.content.equals(currentFile.content);
                
                if (needsUpdate) {
                    // FIX: Use modifyFile for existing files, addFile for new files
                    if (currentFile == null) {
                        fileManager.addFile(file, sourceFile.content);
                    } else {
                        fileManager.modifyFile(file, sourceFile.content);
                    }
                    mergedFiles++;
                }
            }
        }
        
        String commitId = fileManager.commitChanges("Merge branch '" + sourceBranch + "' into " + currentBranch);
        branches.put(currentBranch, commitId);
        
        return "Merge made by recursive. " + mergedFiles + " files updated.";
    }
    
    // Remote operations (simulated)
    public String setRemote(String url) {
        this.remoteUrl = url;
        this.hasRemote = true;
        return "Set remote repository to: " + url;
    }
    
    public String fetch() {
        if (!hasRemote) {
            return "No remote repository configured";
        }
        return "Fetching from " + remoteUrl + " (simulated)";
    }
    
    public String pull() {
        if (!hasRemote) {
            return "No remote repository configured";
        }
        String fetchResult = fetch();
        // Simulate merge with remote branch
        String remoteBranch = "remote/" + currentBranch;
        if (!branches.containsKey(remoteBranch)) {
            branches.put(remoteBranch, fileManager.getCurrentVersion());
        }
        String mergeResult = merge(remoteBranch);
        return fetchResult + "\n" + mergeResult;
    }
    
    public String push() {
        if (!hasRemote) {
            return "No remote repository configured";
        }
        return "Pushing to " + remoteUrl + " (simulated) - " + getCurrentFiles().size() + " files";
    }
    
    // Getters
    public FileVersionManager getFileManager() { return fileManager; }
    public Map<String, String> getBranches() { return new HashMap<>(branches); }
    public String getCurrentBranch() { return currentBranch; }
    public Set<String> getStagedFiles() { return new HashSet<>(stagedFiles); }
    public boolean hasRemote() { return hasRemote; }
    public String getRemoteUrl() { return remoteUrl; }
    
    // Clear staging area
    public void clearStaged() {
        stagedFiles.clear();
    }
    
    // ADD THIS MISSING METHOD:
    public List<String> getCurrentFiles() {
        return fileManager.getCurrentFiles();
    }
}