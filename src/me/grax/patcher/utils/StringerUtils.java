package me.grax.patcher.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class StringerUtils implements Opcodes {
  public static byte[] getExtra(File file) throws IOException {
    ArrayList<byte[]> allArrays = new ArrayList<byte[]>();
    HashMap<Integer, Integer> amounts = new HashMap<>();
    ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
    int classCount = 0;
    while (true) {
      ZipEntry e = zis.getNextEntry();
      if (e == null) {
        break;
      }
      byte[] extra = e.getExtra();
      if (extra != null) {
        boolean found = false;
        int i = 0;
        for (byte[] array : allArrays) {
          if (array[0] == extra[0] && array[1] == extra[1] && array[2] == extra[2] && array[3] == extra[3]) {
            found = true;
            break;
          }
          i++;
        }
        if (found) {
          amounts.put(i, amounts.getOrDefault(i, 1) + 1);
        } else {
          allArrays.add(extra);
        }
      }
      zis.closeEntry();
      if (e.getName().endsWith(".class")) {
        classCount++;
      }
    }
    zis.close();
    int i = 0;
    int max = 0;
    byte[] hightestChance = null;
    int extras = 0;
    for (byte[] array : allArrays) {
      int files = amounts.getOrDefault(i, 1);
      if (files > max) {
        hightestChance = array;
        max = files;
      }
      extras += files;
      System.out.println("Possible key #" + i + ": " + Arrays.toString(array) + " (" + files + " files)");
      i++;
    }
    System.out.println(Math.round((extras / (double) classCount) * 100d) + "% of class files have an extra-value");
    if (extras < classCount / 3d) {
      JOptionPane.showMessageDialog(null,
          "File is probably not obfuscated with Stringer 3.x (" + Math.round((extras / (double) classCount) * 100d) + "%)", "Warning",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    return hightestChance;
  }

  public static long getStringerKey(byte[] extraValues, File file) throws IOException {
    ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    while (true) {
      ZipEntry e = zis.getNextEntry();
      if (e == null) {
        break;
      }
      String name = e.getName();
      byte[] extra = e.getExtra();
      if (extra != null) {
        if (extra[0] == extraValues[0] && extra[1] == extraValues[1]) {
          if (name.contains(".")) {
            if (name.substring(name.lastIndexOf(".")).equals(".class")) {
              bytes.write(name.getBytes("UTF-8"));
              bytes.write(extra);
            }
          }
        }
      }
      zis.closeEntry();
    }
    bytes.close();
    zis.close();
    return getBytes(0L, 0L, bytes.toByteArray());
  }

  private static long getBytes(long var0, long var2, byte[] var4) {
    long var5 = 8317987319222330741L ^ var0;
    long var7 = 7237128888997146477L ^ var2;
    long var9 = 7816392313619706465L ^ var0;
    long var11 = 8387220255154660723L ^ var2;
    int var15 = var4.length / 8 * 8;

    int var16;
    long var13;
    for (var16 = 0; var16 < var15; var5 ^= var13) {
      var13 = (long) var4[var16++] & 255L | ((long) var4[var16++] & 255L) << 8 | ((long) var4[var16++] & 255L) << 16
          | ((long) var4[var16++] & 255L) << 24 | ((long) var4[var16++] & 255L) << 32 | ((long) var4[var16++] & 255L) << 40
          | ((long) var4[var16++] & 255L) << 48 | ((long) var4[var16++] & 255L) << 56;
      var11 ^= var13;
      var5 += var7;
      var9 += var11;
      var7 = var7 << 13 | var7 >>> 51;
      var11 = var11 << 16 | var11 >>> 48;
      var7 ^= var5;
      var11 ^= var9;
      var5 = var5 << 32 | var5 >>> 32;
      var9 += var7;
      var5 += var11;
      var7 = var7 << 17 | var7 >>> 47;
      var11 = var11 << 21 | var11 >>> 43;
      var7 ^= var9;
      var11 ^= var5;
      var9 = var9 << 32 | var9 >>> 32;
      var5 += var7;
      var9 += var11;
      var7 = var7 << 13 | var7 >>> 51;
      var11 = var11 << 16 | var11 >>> 48;
      var7 ^= var5;
      var11 ^= var9;
      var5 = var5 << 32 | var5 >>> 32;
      var9 += var7;
      var5 += var11;
      var7 = var7 << 17 | var7 >>> 47;
      var11 = var11 << 21 | var11 >>> 43;
      var7 ^= var9;
      var11 ^= var5;
      var9 = var9 << 32 | var9 >>> 32;
    }

    var13 = 0L;

    for (var16 = var4.length - 1; var16 >= var15; --var16) {
      var13 <<= 8;
      var13 |= (long) var4[var16] & 255L;
    }

    var13 |= (long) var4.length << 56;
    var11 ^= var13;
    var5 += var7;
    var9 += var11;
    var7 = var7 << 13 | var7 >>> 51;
    var11 = var11 << 16 | var11 >>> 48;
    var7 ^= var5;
    var11 ^= var9;
    var5 = var5 << 32 | var5 >>> 32;
    var9 += var7;
    var5 += var11;
    var7 = var7 << 17 | var7 >>> 47;
    var11 = var11 << 21 | var11 >>> 43;
    var7 ^= var9;
    var11 ^= var5;
    var9 = var9 << 32 | var9 >>> 32;
    var5 += var7;
    var9 += var11;
    var7 = var7 << 13 | var7 >>> 51;
    var11 = var11 << 16 | var11 >>> 48;
    var7 ^= var5;
    var11 ^= var9;
    var5 = var5 << 32 | var5 >>> 32;
    var9 += var7;
    var5 += var11;
    var7 = var7 << 17 | var7 >>> 47;
    var11 = var11 << 21 | var11 >>> 43;
    var7 ^= var9;
    var11 ^= var5;
    var9 = var9 << 32 | var9 >>> 32;
    var5 ^= var13;
    var9 ^= 255L;
    var5 += var7;
    var9 += var11;
    var7 = var7 << 13 | var7 >>> 51;
    var11 = var11 << 16 | var11 >>> 48;
    var7 ^= var5;
    var11 ^= var9;
    var5 = var5 << 32 | var5 >>> 32;
    var9 += var7;
    var5 += var11;
    var7 = var7 << 17 | var7 >>> 47;
    var11 = var11 << 21 | var11 >>> 43;
    var7 ^= var9;
    var11 ^= var5;
    var9 = var9 << 32 | var9 >>> 32;
    var5 += var7;
    var9 += var11;
    var7 = var7 << 13 | var7 >>> 51;
    var11 = var11 << 16 | var11 >>> 48;
    var7 ^= var5;
    var11 ^= var9;
    var5 = var5 << 32 | var5 >>> 32;
    var9 += var7;
    var5 += var11;
    var7 = var7 << 17 | var7 >>> 47;
    var11 = var11 << 21 | var11 >>> 43;
    var7 ^= var9;
    var11 ^= var5;
    var9 = var9 << 32 | var9 >>> 32;
    var5 += var7;
    var9 += var11;
    var7 = var7 << 13 | var7 >>> 51;
    var11 = var11 << 16 | var11 >>> 48;
    var7 ^= var5;
    var11 ^= var9;
    var5 = var5 << 32 | var5 >>> 32;
    var9 += var7;
    var5 += var11;
    var7 = var7 << 17 | var7 >>> 47;
    var11 = var11 << 21 | var11 >>> 43;
    var7 ^= var9;
    var11 ^= var5;
    var9 = var9 << 32 | var9 >>> 32;
    var5 += var7;
    var9 += var11;
    var7 = var7 << 13 | var7 >>> 51;
    var11 = var11 << 16 | var11 >>> 48;
    var7 ^= var5;
    var11 ^= var9;
    var5 = var5 << 32 | var5 >>> 32;
    var9 += var7;
    var5 += var11;
    var7 = var7 << 17 | var7 >>> 47;
    var11 = var11 << 21 | var11 >>> 43;
    var7 ^= var9;
    var11 ^= var5;
    var9 = var9 << 32 | var9 >>> 32;
    return var5 ^ var7 ^ var9 ^ var11;
  }

  public static ArrayList<MethodNode> getHashMethods(Collection<ClassNode> nodes) {
    ArrayList<MethodNode> methods = new ArrayList<>();
    for (ClassNode cn : nodes) {
      for (MethodNode mn : cn.methods) {
        if (mn.desc.equals("()J") && mn.instructions.size() > 2 && mn.instructions.getFirst().getOpcode() == LCONST_0
            && mn.instructions.getFirst().getNext().getOpcode() == LSTORE && mn.instructions.getLast().getOpcode() == GOTO) {
          methods.add(mn);
        }
      }
    }
    return methods;

  }

  public static void patch(ArrayList<MethodNode> nodes, long key) {
    for (MethodNode mn : nodes) {
      mn.instructions.clear();
      mn.instructions.add(new LdcInsnNode(key));
      mn.instructions.add(new InsnNode(LRETURN));
      mn.tryCatchBlocks.clear();
      mn.localVariables.clear();
    }
  }

}
