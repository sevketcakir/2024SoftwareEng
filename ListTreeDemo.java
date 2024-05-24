import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Stack;

/**
 * GUI sınıfı
 */
public class ListTreeDemo extends JFrame {
    // GUI liste nesnesi
    private JList<String> list;
    // Liste modeli(ekleme, silme vb. işlemleri için)
    private DefaultListModel<String> listModel;
    // GUI ağaç nesnesi
    private JTree tree;
    // Ağaç modeli(ekleme, silme vb. işlemleri için)
    private DefaultTreeModel treeModel;
    // Ağacın kök düğümü
    private DefaultMutableTreeNode rootNode;
    // Undo yığıtı(eski adı commandStack)
    private Stack<CommandAction> undoStack;
    // Redo yığıtı
    private Stack<CommandAction> redoStack;

    /**
     * Pencereyi oluştur ve bileşenleri ekle
     */
    public ListTreeDemo() {
        setTitle("List and Tree Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Yığıtları oluştur
        undoStack = new Stack<>();
        redoStack = new Stack<>();

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

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redoLastAction();
            }
        });

        // Buttonlar tek bir panelde
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(redoButton);

        // Layout the components
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), new JScrollPane(tree));
        splitPane.setDividerLocation(150);

        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Command nesnesi ile listeden silip ağaca ekler
     */
    private void createTreeFromSelection() {
        // Command nesnesi oluştur
        CreateAction action = new CreateAction(list, tree);
        // Eylemi gerçekleştir
        action.execute();
        // Nesneyi undoStack'e ekle
        undoStack.push(action);
        // redoStack'i temizle
        redoStack.clear();
    }

    /**
     * Son işlemi geri alır
     */
    public void undoLastAction() {
        if (!undoStack.isEmpty()) {
            //Son eylemi yığıttan çek
            CommandAction action = undoStack.pop();
            // Eylemi geri al
            action.undo();
            // Eylemi redoStack'e ekle
            redoStack.push(action);
        }
    }
    /**
     * Geri alınan işlemi yeniden yapar
     */
    public void redoLastAction() {
        if (!redoStack.isEmpty()) {
            // Son geri alınan işlemi yığıttan çek
            CommandAction action = redoStack.pop();
            // Eylemi yeniden yap
            action.redo();
            // Eylemi yapılanlar yığıtına ekle
            undoStack.push(action);
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

/**
 * Command arayüzü
 */
interface CommandAction {
    // Eylemi gerçekleştir
    public void execute();
    // Geri al
    public void undo();
    // Yeniden yap
    public void redo();
}


/**
 * Listeden silip ağaca ekleyen Command katı(concrete, soyut değil) sınıfı
 */
class CreateAction implements CommandAction {
    // GUI Liste nesnesi
    private JList<String> list;
    // GUI Ağaç nesnesi
    private JTree tree;
    // Listeden(GUI) silinen elemanların listesi
    private List<String> removedElements;
    // Silinen elemanların listedeki başlangıç indisi
    private int removedIndex;
    // Ağaca eklenen düğüm nesnesi
    private DefaultMutableTreeNode newNode;

    /**
     * Constructor
     * @param list GUI Liste nesnesi
     * @param tree GUI Ağaç nesnesi
     */
    public CreateAction(JList<String> list, JTree tree) {
        this.list = list;
        this.tree = tree;
    }

    /**
     * Seçili elemanları listeden silip ağaca ekleyen metot
     * Seçili elemanlar removedElements listesine yazılır
     */
    public void transfer() {
        // İlk elemanı kök, diğerlerini çocuk olarak belirler
        DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(removedElements.get(0));
        for (int i = 1; i < removedElements.size(); i++) {
            newRootNode.add(new DefaultMutableTreeNode(removedElements.get(i)));
        }

        newNode = newRootNode;
        // Ağaç modelini al ve düğümü ekle
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.add(newRootNode);
        treeModel.reload();
        // Liste modelini al
        DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
        // Seçili elemanları listeden sil
        for (String value : removedElements) {
            listModel.removeElement(value);
        }
    }

    /**
     * Transfer işlemi için ilgili değişkenleri hazırlar ve transfer
     * metodunu çağırır
     */
    @Override
    public void execute() {
        List<String> selectedValuesList = list.getSelectedValuesList();

        removedElements = selectedValuesList;
        int[] indices = list.getSelectedIndices();
        removedIndex = indices[0];

        transfer();
    }

    /**
     * Geri alma işini yapar
     */
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

    /**
     * Eylemi yeniden yapar
     */
    @Override
    public void redo() {
        transfer();
    }
}
