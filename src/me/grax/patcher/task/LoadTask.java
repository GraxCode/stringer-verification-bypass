package me.grax.patcher.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.grax.patcher.gui.PatcherGui;
import me.grax.patcher.utils.ASMUtils;
import me.grax.patcher.utils.StringerUtils;

public class LoadTask extends SwingWorker<Void, Integer> {

  private JarFile inputJar;
  private File input;

  private JProgressBar progressBar;
  private JTextField extraValue;
  private JTextField key;
  private JLabel label;
  
  private int jarSize;
  private int loaded;

  private PatcherGui gui;

  public LoadTask(PatcherGui gui, File input, JProgressBar progressBar, JTextField extraValue, JTextField key, JLabel label) throws IOException {
    this.gui = gui;
    this.inputJar = new JarFile(input);
    this.input = input;
    this.jarSize = countFiles(this.inputJar);
    this.progressBar = progressBar;
    this.extraValue = extraValue;
    this.key = key;
    this.label = label;
  }

  @Override
  protected Void doInBackground() throws Exception {
    publish(0);
    loadFiles(inputJar);
    publish(67);
    gui.extras = StringerUtils.getExtra(input);
    if (gui.extras == null) {
      publish(100);
      return null;
    }
    this.extraValue.setText(gui.extras[0] + ", " + gui.extras[1] + ", " + gui.extras[2] + ", " + gui.extras[3]);
    publish(80);
    gui.longKey = StringerUtils.getStringerKey(gui.extras, input);
    this.key.setText(String.valueOf(gui.longKey));
    gui.searchMethods.setEnabled(true);
    gui.patchMeta.setEnabled(true);
    publish(100);
    return null;
  }

  @Override
  protected void process(List<Integer> chunks) {
    int i = chunks.get(chunks.size() - 1);
    progressBar.setValue(i);
    super.process(chunks);
  }

  @Override
  protected void done() {
    progressBar.setValue(0);
    super.done();
  }

  public int countFiles(final JarFile zipFile) {
    final Enumeration<? extends JarEntry> entries = zipFile.entries();
    int c = 0;
    while (entries.hasMoreElements()) {
      entries.nextElement();
      ++c;
    }
    return c;
  }

  /**
   * loads both classes and other files at the same time
   */
  public void loadFiles(JarFile jar) throws IOException {
    Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
    Map<String, byte[]> otherFiles = new HashMap<String, byte[]>();

    Stream<JarEntry> str = jar.stream();
    str.forEach(z -> readJar(jar, z, classes, otherFiles));
    jar.close();
    gui.classes = classes;
    gui.output = otherFiles;
    setLabel(jar);
    return;
  }

  private void setLabel(JarFile jar) {
    try {
      String protectedBy = jar.getManifest().getMainAttributes().getValue("Protected-By");
      if(protectedBy != null) {
        label.setText(protectedBy);
      } else {
        label.setText("Signed: true");
      }
    } catch (IOException e) {
      e.printStackTrace();
      label.setText("Manifest couldn't be loaded");
    }
  }

  private void readJar(JarFile jar, JarEntry en, Map<String, ClassNode> classes, Map<String, byte[]> otherFiles) {
    publish((int) ((((float) loaded++ / (float) jarSize) * 100f) * (2 / 3d)));
    String name = en.getName();
    try (InputStream jis = jar.getInputStream(en)) {
      if (name.endsWith(".class")) {
        byte[] bytes = IOUtils.toByteArray(jis);
        String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
        if (cafebabe.toLowerCase().equals("cafebabe")) {
          try {
            final ClassNode cn = ASMUtils.getNode(bytes);
            if (cn != null) {
              for (MethodNode mn : cn.methods) {
                mn.owner = cn.name;
              }
              classes.put(cn.name, cn);
            }
          } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed loading class file " + name);
          }
        }
      } else if (!en.isDirectory()) {
        byte[] bytes = IOUtils.toByteArray(jis);
        otherFiles.put(name, bytes);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Failed loading file");
    }
    return;
  }
}