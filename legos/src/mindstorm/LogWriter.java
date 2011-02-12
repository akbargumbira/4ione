package mindstorm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ecky
 */
public class LogWriter {
    private final BufferedWriter _writer;

    public LogWriter(String filename) throws IOException {
        _writer = new BufferedWriter(new FileWriter(filename, true));
        log("LOG STARTED");
    }

    public void log(String log, boolean useTime) {
        if(useTime) log(log);
        else {
            try {
                _writer.write(log);
                _writer.newLine();
            } catch (IOException ex) {
                Logger.getLogger(LogWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void log(String log) {
        try {
            _writer.write(new Date().toString() + "> " + log);
            _writer.newLine();
        } catch (IOException ex) {
            Logger.getLogger(LogWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        log("LOG ENDED");
        try {
            _writer.newLine();
            _writer.close();
        } catch (IOException ex) {
            Logger.getLogger(LogWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
