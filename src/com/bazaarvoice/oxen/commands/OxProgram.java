package com.bazaarvoice.oxen.commands;

/**
 * Created by steve.ohara
 * Date: 9/19/12 12:25 PM
 */

import com.bazaarvoice.oxen.OxConstants;
import com.bazaarvoice.oxen.OxException;

import java.util.Vector;

public class OxProgram {
    private final Vector<OxCommand> _commands = new Vector<OxCommand>();
    private int _nextCommand;
    private final OxConstants _constants;

    public OxProgram(OxConstants c) {
        _constants = c;
    }

    public void setNextCommand(int next) {
        _nextCommand = next;
    }

    public void add(OxCommand cmd) {
        _commands.add(cmd);
    }

    public Vector<OxCommand> getCommands() {
        return _commands;
    }

    public void run() {
        // Evaluate the entire program at once
        _nextCommand = 0;

        while (_nextCommand < _commands.size()) {
            OxCommand cmd = _commands.get(_nextCommand);
            _nextCommand++; // default -- some _commands have to change it

            try {
                cmd.exec(this);
            } catch (OxException ex) {
                ex.setLine(cmd.getLineNumber());
                throw ex;
            }
        }
    }

    // Line up all the for/endfor blocks
    public void matchCommands() {
        int depth = 0;
        for (int i = 0; i < _commands.size(); i++) {
            OxCommand cmd = _commands.get(i);
            depth = cmd.connectCommands(_commands, i, depth, _constants);
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        int depth = 0;
        String indent = "   ";
        String prefix = "  >> ";

        for (OxCommand cmd : _commands) {
            buf.append(prefix);

            if (cmd instanceof OxCommands.MiddleBlockCmd ||
                    cmd instanceof OxCommands.EndBlockCmd) {
                depth--;
            }

            // Get some leading spaces to show depth
            for (int k = 0; k < depth; k++) {
                buf.append(indent);
            }

            if (cmd instanceof OxCommands.StartBlockCmd ||
                    cmd instanceof OxCommands.MiddleBlockCmd) {
                depth++;
            }

            buf.append(cmd.toString()).append(_constants.CRLF);
        }

        return buf.toString();
    }
}
