// FileVersionManager.java
package vcs.core;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileVersionManager {
    public static class FileMetadata implements Serializable {
        private static final long serialVersionUID = 1L;
        public String fileName;
        public String content;
        public LocalDateTime lastModified;
        public String checksum;
        public String author;
        
        public FileMetadata(String fileName, String content, String author) {
            this.fileName = fileName;
            this.content = content;
            this.author = author;
            this.lastModified = LocalDateTime.now();
            this.checksum = calculateChecksum(content);
        }
        
        private String calculateChecksum(String content) {
            if (content == null) return "null";
            return Integer.toHexString(content.hashCode());
        }
        
        @Override
        public String toString() {
            return String.format("File: %s | Modified: %s | Author: %s", 
                fileName, 
                lastModified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                author
            );
        }
    }
    
    private PersistentRedBlackTree<String, FileMetadata> versionTree;
    private String currentAuthor;
    
    public FileVersionManager(String author) {
        this.versionTree = new PersistentRedBlackTree<>();
        this.currentAuthor = author;
    }
    
    // Core VCS operations
    public String addFile(String fileName, String content) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        FileMetadata fileMeta = new FileMetadata(fileName, content, currentAuthor);
        return versionTree.put(fileName, fileMeta, "Added: " + fileName);
    }
    
    public String modifyFile(String fileName, String newContent) {
        FileMetadata existing = versionTree.get(fileName);
        if (existing != null) {
            FileMetadata newFileMeta = new FileMetadata(fileName, newContent, currentAuthor);
            return versionTree.put(fileName, newFileMeta, "Modified: " + fileName);
        }
        return null;
    }
    
    public String deleteFile(String fileName) {
        FileMetadata existing = versionTree.get(fileName);
        if (existing != null) {
            // Create a tombstone instead of null to avoid issues
            FileMetadata tombstone = new FileMetadata(fileName, "DELETED", currentAuthor);
            return versionTree.put(fileName, tombstone, "Deleted: " + fileName);
        }
        return null;
    }
    
    public String commitChanges(String message) {
        if (message == null || message.trim().isEmpty()) {
            message = "Auto-commit";
        }
        // Create a new version with current state
        List<String> files = versionTree.keys();
        String commitMessage = "Commit: " + message + " | Files: " + files.size();
        
        // Use a special key for commit records
        FileMetadata commitRecord = new FileMetadata("COMMIT_RECORD", message, currentAuthor);
        return versionTree.put("COMMIT_RECORD", commitRecord, commitMessage);
    }
    
    // Version control operations
    public boolean checkoutVersion(String versionId) {
        return versionTree.checkoutVersion(versionId);
    }
    
    public FileMetadata getFile(String fileName, String versionId) {
        FileMetadata metadata = versionTree.get(fileName, versionId);
        // Check if file is deleted (tombstone)
        if (metadata != null && "DELETED".equals(metadata.content)) {
            return null;
        }
        return metadata;
    }
    
    public FileMetadata getFile(String fileName) {
        FileMetadata metadata = versionTree.get(fileName);
        // Check if file is deleted (tombstone)
        if (metadata != null && "DELETED".equals(metadata.content)) {
            return null;
        }
        return metadata;
    }
    
    public List<String> getFilesInVersion(String versionId) {
        List<String> files = versionTree.keys(versionId);
        List<String> validFiles = new ArrayList<>();
        
        for (String file : files) {
            if (!file.equals("COMMIT_RECORD")) {
                FileMetadata metadata = getFile(file, versionId);
                if (metadata != null && !"DELETED".equals(metadata.content)) {
                    validFiles.add(file);
                }
            }
        }
        return validFiles;
    }
    
    public List<String> getCurrentFiles() {
        List<String> files = versionTree.keys();
        List<String> validFiles = new ArrayList<>();
        
        for (String file : files) {
            if (!file.equals("COMMIT_RECORD")) {
                FileMetadata metadata = getFile(file);
                if (metadata != null && !"DELETED".equals(metadata.content)) {
                    validFiles.add(file);
                }
            }
        }
        return validFiles;
    }
    
    public Map<String, String> getVersionHistory() {
        Map<String, String> history = new LinkedHashMap<>();
        List<String> versions = versionTree.getVersionHistory();
        
        for (String version : versions) {
            FileMetadata commitRecord = versionTree.get("COMMIT_RECORD", version);
            if (commitRecord != null) {
                history.put(version, commitRecord.content + " | " + 
                    commitRecord.lastModified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else {
                history.put(version, "Initial version");
            }
        }
        
        return history;
    }
    
    public void setAuthor(String author) {
        this.currentAuthor = author;
    }
    
    public String getCurrentVersion() {
        return versionTree.getCurrentVersion();
    }
    
    public void printFileHistory(String fileName) {
        System.out.println("=== File History for: " + fileName + " ===");
        for (String version : versionTree.getVersionHistory()) {
            FileMetadata file = versionTree.get(fileName, version);
            if (file != null && !"DELETED".equals(file.content)) {
                System.out.println("Version: " + version + " | " + file);
            }
        }
    }
    
    // New method to get file content from specific version
    public String getFileContent(String fileName, String versionId) {
        FileMetadata file = getFile(fileName, versionId);
        return file != null ? file.content : null;
    }
    
    // New method to check if file exists in current version
    public boolean fileExists(String fileName) {
        return getFile(fileName) != null;
    }
    
    // New method to get all versions containing a specific file
    public List<String> getVersionsContainingFile(String fileName) {
        List<String> versionsWithFile = new ArrayList<>();
        for (String version : versionTree.getVersionHistory()) {
            FileMetadata file = getFile(fileName, version);
            if (file != null && !"DELETED".equals(file.content)) {
                versionsWithFile.add(version);
            }
        }
        return versionsWithFile;
    }
    
    // NEW: Method to check if version exists
    public boolean versionExists(String versionId) {
        return versionTree.versionExists(versionId);
    }
}