package main.mips;

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

    /**
     * The Simulator class represents a simulator object that initializes the registersFile, memory, fonts, and window objects.
     * 
     * Example Usage:
     * 
     *     Simulator simulator = new Simulator();
     * 
     * Code Analysis:
     * 
     * Inputs:
     *     None
     * 
     * Flow:
     *     1. The constructor is called.
     *     2. The simulator variable is assigned to the current instance of the Simulator class.
     *     3. The registersFile object is created and initialized using the RegistersFile constructor.
     *     4. The memory object is created and initialized using the Memory constructor.
     *     5. The fonts object is created and initialized using the Fonts constructor.
     *     6. The initialize method of the fonts object is called to load the fonts.
     *     7. The window object is created and initialized using the Window constructor.
     * 
     * Outputs:
     *     None
     */
    public Simulator() throws IOException, FontFormatException {
        simulator = this;
        registersFile = new RegistersFile();
        memory = new Memory();

        new Fonts().initialize();
        window = new Window();
    }

    //! starting point
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
    
    /**
     * Executes a MIPS program without using pipeline processing.
     * It takes the code from the window, parses it, and executes each instruction sequentially.
     *
     * Example Usage:
     *     Simulator simulator = new Simulator();
     *     simulator.runNoPipeline();
     *
     * Inputs: None
     *
     * Flow:
     * 1. Reset all registers in the registersFile.
     * 2. Print a message indicating that the program is running.
     * 3. Get the code from the window, remove extra line breaks, and split it into an array of lines.
     * 4. Iterate over each line of code.
     * 5. If the line contains a comment or is empty, skip it.
     * 6. Determine the type of instruction (R, I, or J) and create an instance of the corresponding instruction class.
     * 7. Perform the instruction.
     * 8. Increment the executed instruction count and print the instruction details.
     * 9. If the instruction is a branch, find the target line number and update the loop counter accordingly.
     * 10. If the instruction type is unknown, throw an exception.
     * 11. Calculate the runtime of the program.
     * 12. Print a message indicating that the program has ended, along with the total and executed instruction counts and the runtime.
     * 13. Update the registers in the window.
     *
     * Outputs: None
     *
     * @throws CodeExceptions if there is an error in the code or an unknown instruction is encountered.
     */
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

    /**
     * Displays a pop-up dialog box asking the user if they want to save the current file.
     * 
     * @param second The text to be displayed as the second option in the pop-up dialog box.
     * @return true if the user chose to save the file, false otherwise.
     */
    public boolean unsavedPopUp(String second) {
        Object[] options = {"Save", second};
        int selection = JOptionPane.showOptionDialog(window, "Would you like to save current file?", "Unsaved Changes",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

        return selection == 0;
    }

    /**
     * Saves the contents of the code window to a file.
     * If the file name is not specified, it creates a new file with a unique name in the saves folder.
     * If the file name is already set, it overwrites the existing file.
     *
     * @throws IOException if an I/O error occurs while creating or writing to the file
     */
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

    /**
     * Resets the file name, clears the code box, updates the window title, and marks the file as unsaved.
     * 
     * Example Usage:
     * 
     * Simulator simulator = new Simulator();
     * simulator.newFile();
     * 
     * Inputs: None
     * 
     * Flow:
     * 1. Sets the fileName variable to null.
     * 2. Calls the reset method of the codeBox object in the window to clear the code box.
     * 3. Calls the updateTitle method of the window to update the window title.
     * 4. Calls the forceUnsaved method to mark the file as unsaved.
     * 
     * Outputs: None
     */
    public void newFile() {
        fileName = null;
        window.getCodeBox().reset();
        window.updateTitle();
        forceUnsaved();
    }

    /**
     * Opens a file and displays its contents in the code box of the simulator's window.
     *
     * @param file The file to be opened.
     * @throws IOException If an I/O error occurs.
    */
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