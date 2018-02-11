package me.grax.patcher.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.patcher.task.LoadTask;
import me.grax.patcher.task.SaveTask;
import me.grax.patcher.utils.StringerUtils;

public class PatcherGui extends JFrame {
  
  private static final long serialVersionUID = 1L;
  
  private JPanel contentPane;
  private JProgressBar progressBar;
  private MethodTable table;
  private JTextField extraValue;
  private JTextField key;
  private JMenuItem save;
  private ArrayList<MethodNode> nodes;
  private JLabel label;
  
  public Map<String, ClassNode> classes;
  public Map<String, byte[]> output;
  public byte[] extras;
  public long longKey;
  public JButton searchMethods;
  public JButton patchMeta;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
          } catch (Exception e) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          }
          PatcherGui frame = new PatcherGui();
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public PatcherGui() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 500, 550);
    setResizable(false);
    setTitle("Stringer 3.0 Hash-Patcher");
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(15, 15));
    setContentPane(contentPane);
    setJMenuBar(getMyMenuBar());
    JPanel top = new JPanel();
    top.setLayout(new BorderLayout(15, 15));

    JPanel extraBytes = new JPanel();
    final JPanel input = new JPanel(new GridLayout(0, 1, 5, 5));
    final JPanel fields = new JPanel(new GridLayout(0, 1, 5, 5));
    extraBytes.setLayout(new BorderLayout(15, 15));
    input.add(new JLabel("Extra Value:"));
    fields.add(extraValue = new JTextField());
    extraValue.setEditable(false);
    input.add(new JLabel("Calculated Key:"));
    fields.add(key = new JTextField());
    key.setEditable(false);
    extraBytes.add(input, BorderLayout.WEST);
    extraBytes.add(fields, BorderLayout.CENTER);
    top.add(extraBytes, BorderLayout.CENTER);
    contentPane.add(top, BorderLayout.NORTH);

    JPanel center = new JPanel();
    center.setLayout(new BorderLayout(15, 15));
    center.setBorder(BorderFactory.createTitledBorder("Stringer hash methods"));
    table = new MethodTable();
    center.add(new JScrollPane(table), BorderLayout.CENTER);
    JPanel tableActions = new JPanel();
    tableActions.setLayout(new GridLayout(1, 3, 15, 15));
    patchMeta = new JButton("Patch Manifest");
    patchMeta.setEnabled(false);
    patchMeta.addActionListener(e -> {
      byte[] manifest = output.get("META-INF/MANIFEST.MF");
      if (manifest != null) {
        try {
          String mf = new String(manifest, "UTF-8");
          String newMf = "";
          int i = 0;
          for (String line : mf.split("\n")) {
            if (line.length() > 1 && !line.startsWith("JAR-Signature:") && !line.startsWith("Name:") && !line.startsWith("SHA-256-Digest:")) {
              newMf += line;
              newMf += '\n';
            } else {
              i++;
            }
          }
          newMf += "\n"; //need a new line because without it won't recognize it
          output.remove("META-INF/CERT.RSA");
          output.remove("META-INF/CERT.SF");
          output.put("META-INF/MANIFEST.MF", newMf.getBytes("UTF-8"));
          JOptionPane.showMessageDialog(null, "Patch successful, removed " + i + " lines!", "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e1) {
          JOptionPane.showMessageDialog(null, e1.toString(), "Error", JOptionPane.ERROR_MESSAGE);
          e1.printStackTrace();
        }
      } else {
        JOptionPane.showMessageDialog(null, "Archive does not contain META-INF/MANIFEST.MF", "Information", JOptionPane.INFORMATION_MESSAGE);
      }
      patchMeta.setEnabled(false);
      save.setEnabled(true);
    });
    JButton patch = new JButton("Patch");
    patch.setEnabled(false);
    patch.addActionListener(e -> {
      StringerUtils.patch(nodes, longKey);
      nodes = StringerUtils.getHashMethods(classes.values());
      table.addList(nodes);
      if (nodes.isEmpty()) {
        JOptionPane.showMessageDialog(null, "Patch successful!", "Information", JOptionPane.INFORMATION_MESSAGE);
        patch.setEnabled(false);
        save.setEnabled(true);
      } else {
        JOptionPane.showMessageDialog(null, "Failed to patch method(s) using key " + longKey, "Warning", JOptionPane.WARNING_MESSAGE);
      }
    });
    searchMethods = new JButton("Search");
    searchMethods.setEnabled(false);
    searchMethods.addActionListener(e -> {
      nodes = StringerUtils.getHashMethods(classes.values());
      if (!nodes.isEmpty()) {
        table.addList(nodes);
        patch.setEnabled(true);
      } else {
        JOptionPane.showMessageDialog(null, "No hashing methods found!", "Warning", JOptionPane.WARNING_MESSAGE);
      }
    });
    tableActions.add(searchMethods);
    tableActions.add(new JPanel());
    tableActions.add(patch);
    center.add(tableActions, BorderLayout.PAGE_END);
    contentPane.add(center, BorderLayout.CENTER);

    progressBar = new JProgressBar();
    JPanel south = new JPanel();
    south.setLayout(new BorderLayout(15, 15));
    south.add(progressBar, BorderLayout.SOUTH);
    label = new JLabel("");
    label.setHorizontalAlignment(JLabel.CENTER);
    JPanel southCenter = new JPanel();
    southCenter.setLayout(new GridLayout(1, 5, 15, 15));
    southCenter.setBorder(BorderFactory.createTitledBorder("Jar signature"));
    southCenter.add(label);
    southCenter.add(new JPanel());
    southCenter.add(patchMeta);
    south.add(southCenter, BorderLayout.CENTER);
    contentPane.add(south, BorderLayout.SOUTH);
  }

  private JMenuBar getMyMenuBar() {
    JMenuBar jmb = new JMenuBar();
    JMenu file = new JMenu("File");
    JMenu help = new JMenu("Help");
    JMenuItem load = new JMenuItem("Load");
    load.addActionListener(e -> {
      openLoadDialogue();
    });
    save = new JMenuItem("Save as..");
    save.setEnabled(false);
    save.addActionListener(e -> {
      openSaveDialogue();
    });
    JMenuItem about = new JMenuItem("About");
    about.addActionListener(e -> {
      JOptionPane.showMessageDialog(null,
          "This tool was made by GraxCode\nhttps://github.com/GraxCode/Stringer-Hash-Patcher\n\nStringer 3.0 Hash-Patcher is licensed under the GNU General Public License 3\nCopyright 2018 GraxCode",
          "Information", JOptionPane.INFORMATION_MESSAGE);
    });
    help.add(about);
    file.add(load);
    file.add(save);
    jmb.add(file);
    jmb.add(help);
    return jmb;
  }

  protected void openSaveDialogue() {
    JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package (*.jar)", "jar"));
    int result = jfc.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File output = jfc.getSelectedFile();
      saveFile(output);
      
      //too lazy to reset everything manually
      this.dispose();
      PatcherGui frame = new PatcherGui();
      frame.setVisible(true);
    }
  }

  private void saveFile(File out) {
    try {
      new SaveTask(progressBar, out, classes.values(), output).execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void openLoadDialogue() {
    JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.home") + File.separator + "Desktop"));
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.setFileFilter(new FileNameExtensionFilter("Java Package (*.jar)", "jar"));
    int result = jfc.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File input = jfc.getSelectedFile();
      loadFile(input);
    }
  }

  private void loadFile(File input) {
    try {
      new LoadTask(this, input, progressBar, extraValue, key, label).execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
