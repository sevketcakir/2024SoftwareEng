import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Stack;

public class ListTreeDemo extends JFrame {
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private Stack<CommandAction> commandStack;

    public ListTreeDemo() {
        setTitle("List and Tree Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        commandStack = new Stack<>();

        // Initialize list model and list
        listModel = new DefaultListModel<>();
        for (int i = 1; i <= 50; i++) {
            listModel.addElement("Item " + i);
        }
        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        // Initialize tree model and tree
        rootNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);

        // Initialize button and its action
        JButton createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTreeFromSelection();
            }
        });

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoLastAction();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(undoButton);

        // Layout the components
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), new JScrollPane(tree));
        splitPane.setDividerLocation(150);

        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createTreeFromSelection() {
        CreateAction action = new CreateAction(list, tree);
        action.execute();
        commandStack.push(action);
    }

    public void undoLastAction() {
        if (!commandStack.isEmpty()) {
            CommandAction action = commandStack.pop();
            action.undo();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ListTreeDemo().setVisible(true);
            }
        });
    }
}

interface CommandAction {
    public void execute();
    public void undo();
    public void redo();
}

class CreateAction implements CommandAction {
    private JList<String> list;
    private JTree tree;
    private List<String> removedElements;
    private int removedIndex;
    private DefaultMutableTreeNode newNode;

    public CreateAction(JList<String> list, JTree tree) {
        this.list = list;
        this.tree = tree;
    }

    @Override
    public void execute() {
        List<String> selectedValuesList = list.getSelectedValuesList();

        removedElements = selectedValuesList;
        int[] indices = list.getSelectedIndices();
        removedIndex = indices[0];

        DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(selectedValuesList.get(0));
        for (int i = 1; i < selectedValuesList.size(); i++) {
            newRootNode.add(new DefaultMutableTreeNode(selectedValuesList.get(i)));
        }

        newNode = newRootNode;

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.add(newRootNode);
        treeModel.reload();

        DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
        // Remove selected items from the list
        for (String value : selectedValuesList) {
            listModel.removeElement(value);
        }
    }

    @Override
    public void undo() {
        // Add to JList
        int removedCount = removedElements.size();
        DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
        for(int i=0;i<removedCount;i++)
            listModel.insertElementAt(removedElements.get(removedCount-i-1), removedIndex);

        // Remove from JTree
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.remove(newNode);
        treeModel.reload(rootNode);
    }

    @Override
    public void redo() {
    }

}
