# MIPS Simulator in Java

This team project introduces a primary emulator for the MIPS assembly language, implemented in Java, complemented by a user-friendly graphical user interface (GUI). The current iteration replicates the functionality of MIPS without register pipelining, a feature slated for future inclusion. 


## Table of Contents
1. [Description](#description)
2. [What is MIPS?](#what-is-mips)
3. [How Does MIPS Work?](#how-does-mips-work)
4. [Implemented Instruction Set](#implemented-instruction-set)
5. [Features](#features)
6. [Test File Explanation](#test-file-explanation)
7. [Version History](#version-history)
8. [To-do](#to-do)
9. [Concluding Remarks](#concluding-remarks)

## Description

This simulator serves as a fundamental editor and executor for MIPS code, offering a platform for understanding and experimenting with MIPS assembly language in a visually engaging manner. The GUI encompasses a code editor, and a register display, among other useful functionalities, enabling line-by-line code execution. The roadmap includes the addition of register pipelining while retaining the line-by-line execution feature for run-time comparison.

![Program GUI](https://i.ibb.co/BjCJ6M9/Screenshot-9.png)

## What is MIPS?

MIPS (Microprocessor without Interlocked Pipeline Stages) is a RISC (Reduced Instruction Set Computing) architecture known for its simplicity and efficiency. It's often utilized in academic settings to impart fundamental concepts of computer architecture and assembly language. The design of MIPS facilitates pipelining by minimizing instruction complexity and ensuring consistent instruction size.

## How Does MIPS Work?

MIPS architecture operates through a sequence of stages—Fetch, Decode, Execute, Memory Access, and Write Back, known as the instruction execution cycle. Each instruction in MIPS is exactly 32 bits long, simplifying the fetch and decode stages. The simplicity of the instruction set allows for a straightforward hardware implementation, enabling high performance and efficient use of the processor.

## Implemented Instruction Set

- Arithmetic Operations: `add`, `addi`, `sub`, `mult`, `div`
- Logic Operations: `and`, `andi`, `or`, `ori`, `nor`
- Shift Operations: `sll`, `srl`
- Word Operations: `lw`, `sw`
- Branching: `j`, `beq`, `bne`
- Comparison: `slt`, `slti`

## Features

- Display of primary registers ($s, $t, $v, and $a) values on the GUI.
- Initial main memory allocation of 256KB, adjustable via the memory constructor.
- Save, open, and execute MIPS assembly code files with the line-by-line execution feature.
- Saved files are stored in a dedicated directory `MipsSimulator/saved` within the documents folder.

## Test File Explanation

A test file named `test.mips` is provided to demonstrate a range of basic operations on the simulator. This file includes data declarations, arithmetic, logic, comparison, branching, and shift operations, alongside memory loading instructions. It serves as a simple test case to verify the simulator's functionality across a mix of MIPS instructions. For a detailed breakdown of the test file, refer to [this section](#test-file-breakdown).

## Version History

- Release 0.1
  - Added save file functionality
  - Added open file functionality
  - Added save-exit to unsaved files
  - Improved line-by-line execution
  - Enhanced GUI smoothness and overall look

- Pre-release 0.0
  - Introduced code editor and register data display
  - Implemented line-by-line execution
  - Added custom exceptions

## To-do

- [ ] Add register pipelining
- [ ] Update memory settings through the GUI
- [ ] Improve exceptions handling
- [ ] Naming files when saving

## Concluding Remarks

This project lays the groundwork for a comprehensive MIPS simulator, providing a platform for understanding and experimenting with MIPS assembly language in a visually engaging manner. The roadmap ahead includes notable enhancements that aim to bring this simulator closer to a real-world representation of the MIPS architecture.

## Test File Breakdown

The provided `file.mips` serves as a test case to demonstrate the simulator's functionality. Here’s a brief explanation of the sections within the file:

1. **Data Section:** Contains memory declarations for storing values.
2. **Text Section:** Contains the program code, starting from the `_start` label.
3. **Loading Values:** Instructions for loading values from memory into registers.
4. **Arithmetic Operations:** Demonstrates addition, subtraction, and addition with an immediate value.
5. **Logic Operations:** Demonstrates bitwise AND and OR operations.
6. **Comparison:** Demonstrates the set less than operation.
7. **Branching:** Demonstrates branch if equal operation.
8. **Shift Operation:** Demonstrates logical shift left operation.
9. **Exit Label:** Marks the end of the program.

The sections from Loading Values to Exit Label contain various MIPS instructions, showcasing the operational capabilities of the simulator.
