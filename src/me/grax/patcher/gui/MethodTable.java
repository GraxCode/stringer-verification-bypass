package me.grax.patcher.gui;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.objectweb.asm.tree.MethodNode;

public class MethodTable extends JTable {
  private static final long serialVersionUID = 1L;

  public MethodTable() {
    setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
    getTableHeader().setReorderingAllowed(false);
    DefaultTableModel lm = new DefaultTableModel();
    lm.addColumn("#");
    lm.addColumn("Item");
    setModel(lm);
  }

  @Override
  public boolean editCellAt(int row, int column) {
    return false;
  }

  public void addList(ArrayList<MethodNode> mns) {
    DefaultTableModel lm = new DefaultTableModel();
    lm.addColumn("#");
    lm.addColumn("Method");
    int i = 0;
    for (MethodNode item : mns) {
      lm.addRow(new Object[] { String.valueOf(i), item.owner + "." + item.name + item.desc });
      i++;
    }
    setModel(lm);
  }
}
