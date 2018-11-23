package io.github.nqnstudios.minedraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class DraftCore {
    public static void main(String[] args) {
        // Create a DraftCore and debug it in a repl
        DraftCore core = new DraftCore();

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            core.process(line);
            core.dumpOutput();
        }
    }


    public void clear() {
        filename = "";
        lines.clear();
    }

    private String joinTokens(String[] tokens, int start, boolean toPath) {
        String joint = "";
        for (int i = start; i < tokens.length; ++i) {
            String token = tokens[i];
            if (toPath)
                token = token.replace("/", File.separator);
            joint += token;
            if (i < tokens.length - 1) {
                joint += " ";
            }
        }
        return joint;
    }

    public void process(String message) {
        String[] tokens = message.split(" ");
        
        switch (tokens[0]) {
            case "open":
                if (tokens.length >= 2) {
                    // Assume the file path might be all of the remaining tokens, including spaces
                    String path = homedir;
                    path += File.separator;
                    path += joinTokens(tokens, 1, true); 

                    System.out.println(path);

                    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                        String line;
                        lines.clear();
                        while ((line = br.readLine()) != null) {
                            lines.add(line);
                        }
                        filename = path;
                        selectLine(1);
                    } catch (Exception e) {
                        output = "Failed: " + e.getClass().getName() + e.getMessage();
                    }
                }
                else {
                    // TODO trying to open without a filename!!
                }
                break;
            case "close":
                clear();
                break;

            default:
                if (filename.length() == 0) {
                    // TODO trying to edit without a file open! Bad!
                }
                else {
                    // TODO run the editor thing
                    editorProcess(tokens);
                }

        }
    }

    private void selectLine( int line) {
        if (line > 0 && line <= lines.size()) {
            index = line - 1;
            output = lines.get(index);
        }
    }

    private void up(int amount) {
        int newIndex = index-amount;
        if (newIndex < 0) 
            newIndex = lines.size() + newIndex;
        selectLine(newIndex+1);
    }
    private void down(int amount) {
        selectLine((index+amount)%lines.size()+1);
    }

    public void tick() {

    }

    private String output = "";
    public String takeOutput() {
        String temp = output;
        output = "";
        return temp;
    }
    public void dumpOutput() {
        if (output.length() > 0)
            System.out.println(output);
        output = "";
    }

    private void editorProcess(String[] tokens) {
        String joint = joinTokens(tokens, 1, false);
        switch (tokens[0]) {
            // Allow up/down [n]
            case "up":
            case "down":
            case "goto":
                int num = 1;
                if (tokens.length == 2) {
                    num = Integer.parseInt(tokens[1]);
                }
                if (tokens[0].equals("up")) {
                    up(num);
                } else if (tokens[0].equals("down")) {
                    down(num);
                } else {
                    selectLine(num);
                }
                break;
            // Allow vim-style substitution in the limited format s foo/bar
            case "s":
                String[] two = joint.split("/");
                if (two.length != 2) {
                    // TODO v bad!
                    output = "You didn't provide 2 strings (one to find/one to replace with) ";
                }
                else {
                    lines.set(index, lines.get(index).replace(two[0], two[1]));
                }
                break;
            // Allow full-scale line replacement
            case "S":
                    lines.set(index, joint);
                break;
            // Allow i/insertion at start of line
            case "i":
                lines.set(index, joint + lines.get(index));
                break;
            // Allow a/append at end of line
            case "a":
                lines.set(index, lines.get(index) + joint);
                break;
            // Allow O to insert line above this one
            case "O":
                lines.add(index, joint);
                break;
            // Allow o to insert line after this one
            case "o":
                lines.add(index+1, joint);
                selectLine(index+2);
                break;
            
            // Allow line deletion
            case "d":
                lines.remove(index);
                break;
        }

        //Print the current line now
        selectLine(index+1);
        // After all editor commands, save

        try {
            File fout = new File(filename);
            FileOutputStream fos = new FileOutputStream(fout);
    
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
    
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                bw.newLine();
            }
    
            bw.close();
        } catch (Exception e) {
            output = "MAJOR ERROR! SAVE FAILED!";
        }
    }

    String homedir = System.getProperty("user.home");
    String filename = "";

    // Store a list (ArrayList) of lines in the file
    ArrayList<String> lines = new ArrayList<String>();
    // Keep a line number index to the open file
    int index = 0;

    // TODO command to loop through lines on a delay, printing them to chat, until user types stop



}