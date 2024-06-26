package mips.main;

import mips.exceptions.BranchNotFoundException;
import mips.exceptions.CodeExceptions;
import mips.exceptions.UnknownInstructionException;
import mips.graphics.Fonts;
import mips.graphics.Window;
import mips.instructions.IFormat;
import mips.instructions.Instruction;
import mips.instructions.JFormat;
import mips.instructions.RFormat;
import mips.units.Memory;
import mips.units.RegistersFile;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class Simulator {

    private boolean saved;
    private String fileName;
    private final Window window;
    private final RegistersFile registersFile;
    private final Memory memory;
    private static Simulator simulator;

    private static File mainFolder;
    private static File savesFolder;

    public Simulator() throws IOException, FontFormatException {
        simulator = this;
        registersFile = new RegistersFile();
        memory = new Memory();

        new Fonts().initialize();
        window = new Window();
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        String docs = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();

        mainFolder = new File(docs + "\\MipsSimulator");
        if (!mainFolder.exists())
            mainFolder.mkdirs();
        savesFolder = new File(mainFolder.getPath() + "\\saves");
        if (!savesFolder.exists())
            savesFolder.mkdirs();

        new Simulator();
    }

    public void runNoPipeline() throws CodeExceptions {
        registersFile.resetAll();
        System.out.println(" == Running Program == ");
        long startTime = System.currentTimeMillis();

        String[] code = window.getCodeBox().getCode().replaceAll("\n+", "\n").split("\n");
        for (int i = 0; i < code.length; i++)
            code[i] = code[i].replaceFirst("^\\s*", "");
        int i, executed = 0;

        for (i = 0; i < code.length; i++) {
            String line = code[i];

            if (line.contains("#"))
                line = line.substring(0, line.indexOf("#"));

            if (line.contains(":") || line.isEmpty()) continue;

            Instruction instruction = new Instruction(line);
            char t = instruction.getType();

            if (t == 'r') {
                instruction = new RFormat(line);
                instruction.perform();

                executed++;
                System.out.println("  [" + executed + "]:  PC:" + i + "  " + line);
            } else if (t == 'i') {
                instruction = new IFormat(line);
                String branch = ((IFormat) instruction).performInstruction();

                if (branch != null) {
                    i = safeFindPC(code, branch);

                    if (i == -1)
                        throw new BranchNotFoundException("Branch \"" + branch + "\" is not found in the mips instructions");
                }

                executed++;
                System.out.println("  [" + executed + "]:  PC:" + i + "  " + line);
            } else if (t == 'j') {
                instruction = new JFormat(line);
                String branch = ((JFormat) instruction).performInstruction();

                if (branch != null) {
                    i = safeFindPC(code, branch);
                    if (i == -1)
                        throw new BranchNotFoundException("Branch \"" + branch + "\" is not found in the mips instructions");
                }

                executed++;
                System.out.println("  [" + executed + "]:  PC:" + i + "  " + line);
            } else
                throw new UnknownInstructionException("Unknown instruction at PC " + i);
        }

        long endTime = System.currentTimeMillis();

        System.out.println(" == Program Ended == ");
        System.out.println("Total Instructions: " + code.length + "\n" +
                "Executed Instructions: " + executed + "\n" +
                "Runtime: " + (endTime - startTime) + "ms");
        window.updateRegisters();

    }

    public boolean unsavedPopUp(String second) {
        Object[] options = {"Save", second};
        int selection = JOptionPane.showOptionDialog(window, "Would you like to save current file?", "Unsaved Changes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

        return selection == 0;
    }

    public void saveFile() throws IOException {
        File file;

        if (fileName == null) {
            file = new File(savesFolder.getPath() + "\\mipsProgram#" + (Objects.requireNonNull(savesFolder.list()).length + 1) + ".mips");
            file.createNewFile();
            fileName = file.getName();
            window.updateTitle();
        } else
            file = new File(savesFolder.getPath() + "\\" + fileName + ".mips");

        PrintWriter writer = new PrintWriter(file.getPath(), StandardCharsets.UTF_8);
        String[] code = window.getCodeBox().getCode().split("\n");
        for (String s : code)
            writer.println(s);
        writer.close();

        setSaved(true);
    }

    public void newFile() {
        fileName = null;
        window.getCodeBox().reset();
        window.updateTitle();
        forceUnsaved();
    }

    public void openFile(File file) throws IOException {
        window.getCodeBox().reset();
        fileName = file.getName();
        window.updateTitle();
        String fileContent = Files.readString(Path.of(file.getPath()));
        window.getCodeBox().setText(fileContent);
        setSaved(true);
    }

    public void setSaved(boolean value) {
        if (saved == value) return;

        saved = value;
        window.updateSaved(saved);
    }

    public void forceUnsaved() {
        window.updateSaved("Unsaved");
    }

    public boolean isSaved() {
        return saved;
    }

    public static Simulator getSimulator() {
        return simulator;
    }

    public Memory getMemory() {
        return memory;
    }

    public RegistersFile getRegistersFile() {
        return registersFile;
    }

    private int safeFindPC(String[] code, String branch) {
        if (Arrays.asList(code).contains(branch + ":"))
            return Arrays.asList(code).indexOf(branch + ":");

        for (int i = 0; i < code.length; i++) {
            if (code[i].toLowerCase(Locale.ROOT).matches("[ ]*" + branch.toLowerCase() + "[ ]*:")) {
                System.out.println(i);
                return i;
            }
        }
        return -1;
    }

    public String getFileName() {
        return fileName;
    }

    public static File getMainFolder() {
        return mainFolder;
    }
}