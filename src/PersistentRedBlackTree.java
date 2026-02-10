// PersistentRedBlackTree.java
package vcs.core;

import java.io.*;
import java.util.*;

public class PersistentRedBlackTree<K extends Comparable<K>, V extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected enum Color { RED, BLACK }
    
    protected static class Node<K, V extends Serializable> implements Serializable {
        K key;
        V value;
        Color color;
        Node<K, V> left, right;
        int size;
        String versionId;
        
        Node(K key, V value, Color color, int size, String versionId) {
            this.key = key;
            this.value = value;
            this.color = color;
            this.size = size;
            this.versionId = versionId;
        }
        
        // Copy constructor
        Node(Node<K, V> other, String newVersionId) {
            this.key = other.key;
            this.value = other.value;
            this.color = other.color;
            this.size = other.size;
            this.versionId = newVersionId;
        }
    }
    
    private Node<K, V> root;
    private final Map<String, Node<K, V>> versionHistory;
    private String currentVersion;
    
    public PersistentRedBlackTree() {
        this.root = null;
        this.versionHistory = new HashMap<>();
        this.currentVersion = "initial";
        saveVersion(currentVersion);
    }
    
    // Persistent put operation - returns new tree version
    public String put(K key, V value, String commitMessage) {
        String newVersion = generateVersionId(commitMessage);
        this.root = put(root, key, value, newVersion);
        if (this.root != null) {
            this.root.color = Color.BLACK;
        }
        this.currentVersion = newVersion;
        saveVersion(newVersion);
        return newVersion;
    }
    
    private Node<K, V> put(Node<K, V> h, K key, V value, String versionId) {
        if (h == null) {
            return new Node<>(key, value, Color.RED, 1, versionId);
        }
        
        int cmp = key.compareTo(h.key);
        
        // Create new nodes along the path (path copying for persistence)
        if (cmp < 0) {
            Node<K, V> newNode = new Node<>(h, versionId);
            newNode.left = put(h.left, key, value, versionId);
            newNode.right = h.right;
            return balance(newNode);
        } else if (cmp > 0) {
            Node<K, V> newNode = new Node<>(h, versionId);
            newNode.right = put(h.right, key, value, versionId);
            newNode.left = h.left;
            return balance(newNode);
        } else {
            // Update existing key - create new node with new value
            Node<K, V> newNode = new Node<>(key, value, h.color, h.size, versionId);
            newNode.left = h.left;
            newNode.right = h.right;
            return newNode;
        }
    }
    
    // Balance the tree after operations
    private Node<K, V> balance(Node<K, V> h) {
        if (h == null) return null;
        
        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);
        
        h.size = size(h.left) + size(h.right) + 1;
        return h;
    }
    
    // Get value from specific version
    public V get(K key, String versionId) {
        Node<K, V> versionRoot = versionHistory.get(versionId);
        return get(versionRoot, key);
    }
    
    // Get value from current version
    public V get(K key) {
        return get(root, key);
    }
    
    private V get(Node<K, V> x, K key) {
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if (cmp < 0) x = x.left;
            else if (cmp > 0) x = x.right;
            else return x.value;
        }
        return null;
    }
    
    // Tree operations
    private Node<K, V> rotateLeft(Node<K, V> h) {
        if (h == null || h.right == null) return h;
        
        Node<K, V> x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = x.left.color;
        x.left.color = Color.RED;
        x.size = h.size;
        h.size = size(h.left) + size(h.right) + 1;
        return x;
    }
    
    private Node<K, V> rotateRight(Node<K, V> h) {
        if (h == null || h.left == null) return h;
        
        Node<K, V> x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = x.right.color;
        x.right.color = Color.RED;
        x.size = h.size;
        h.size = size(h.left) + size(h.right) + 1;
        return x;
    }
    
    private void flipColors(Node<K, V> h) {
        if (h == null) return;
        
        h.color = flipColor(h.color);
        if (h.left != null) h.left.color = flipColor(h.left.color);
        if (h.right != null) h.right.color = flipColor(h.right.color);
    }
    
    private Color flipColor(Color c) {
        return c == Color.RED ? Color.BLACK : Color.RED;
    }
    
    private boolean isRed(Node<K, V> x) {
        return x != null && x.color == Color.RED;
    }
    
    private int size(Node<K, V> x) {
        return x == null ? 0 : x.size;
    }
    
    // Version management
    private void saveVersion(String versionId) {
        versionHistory.put(versionId, deepCopyNode(root));
    }
    
    public boolean checkoutVersion(String versionId) {
        if (versionHistory.containsKey(versionId)) {
            this.root = deepCopyNode(versionHistory.get(versionId));
            this.currentVersion = versionId;
            return true;
        }
        return false;
    }
    
    public List<String> getVersionHistory() {
        List<String> versions = new ArrayList<>(versionHistory.keySet());
        Collections.sort(versions);
        return versions;
    }
    
    public String getCurrentVersion() {
        return currentVersion;
    }
    
    // Deep copy implementation
    private Node<K, V> deepCopyNode(Node<K, V> node) {
        if (node == null) return null;
        
        Node<K, V> newNode = new Node<>(node.key, node.value, node.color, node.size, node.versionId);
        newNode.left = deepCopyNode(node.left);
        newNode.right = deepCopyNode(node.right);
        return newNode;
    }
    
    public List<K> keys() {
        return keys(root);
    }
    
    public List<K> keys(String versionId) {
        Node<K, V> versionRoot = versionHistory.get(versionId);
        return keys(versionRoot);
    }
    
    private List<K> keys(Node<K, V> x) {
        List<K> keys = new ArrayList<>();
        inorderTraversal(x, keys);
        return keys;
    }
    
    private void inorderTraversal(Node<K, V> x, List<K> keys) {
        if (x == null) return;
        inorderTraversal(x.left, keys);
        keys.add(x.key);
        inorderTraversal(x.right, keys);
    }
    
    public int size() {
        return size(root);
    }
    
    public int size(String versionId) {
        Node<K, V> versionRoot = versionHistory.get(versionId);
        return size(versionRoot);
    }
    
    private String generateVersionId(String commitMessage) {
        String safeMessage = commitMessage != null ? commitMessage : "unknown";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "v" + timestamp + "_" + 
               safeMessage.replaceAll("[^a-zA-Z0-9]", "_").substring(0, Math.min(10, safeMessage.length()));
    }
    
    // NEW: Check if version exists
    public boolean versionExists(String versionId) {
        return versionHistory.containsKey(versionId);
    }
    
    // Method to get all keys with their values for a specific version
    public Map<K, V> getAllEntries(String versionId) {
        Map<K, V> entries = new HashMap<>();
        Node<K, V> versionRoot = versionHistory.get(versionId);
        if (versionRoot != null) {
            collectEntries(versionRoot, entries);
        }
        return entries;
    }
    
    private void collectEntries(Node<K, V> x, Map<K, V> entries) {
        if (x == null) return;
        collectEntries(x.left, entries);
        entries.put(x.key, x.value);
        collectEntries(x.right, entries);
    }
    
    // Method to visualize the tree (for debugging)
    public void printTree() {
        printTree(root, 0);
    }
    
    private void printTree(Node<K, V> node, int level) {
        if (node == null) return;
        
        printTree(node.right, level + 1);
        
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(node.key + " (" + node.color + ")");
        
        printTree(node.left, level + 1);
    }
}