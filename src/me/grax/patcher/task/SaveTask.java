package me.grax.patcher.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class SaveTask extends SwingWorker<Void, Integer> {

  private JProgressBar progressBar;
  private File outFile;
  private Collection<ClassNode> nodes;
  private Map<String, byte[]> output;

  public SaveTask(JProgressBar progressBar, File outFile, Collection<ClassNode> nodes, Map<String, byte[]> output) throws IOException {
    this.progressBar = progressBar;
    this.outFile = outFile;
    this.nodes = nodes;
    this.output = output;
  }

  @Override
  protected Void doInBackground() throws Exception {
    publish(0);
    double size = nodes.size();
    double i = 0;
    for (ClassNode cn : nodes) {
      publish((int)((i / size) * 50d));
      ClassWriter cw = new ClassWriter(0);
      cn.accept(cw);
      output.put(cn.name, cw.toByteArray());
      i++;
    }
    publish(50);
    size = nodes.size();
    i = 0;
    JarOutputStream out = new JarOutputStream(new FileOutputStream(outFile));
    for (String entry : output.keySet()) {
      publish(50 + (int)((i / size) * 50d));
      String ext = entry.contains(".") ? "" : ".class";
      out.putNextEntry(new ZipEntry(entry + ext));
      out.write(output.get(entry));
      out.closeEntry();
      i++;
    }
    out.close();
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
}