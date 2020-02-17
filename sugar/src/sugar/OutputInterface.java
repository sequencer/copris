package sugar;

import java.io.PrintWriter;

import sugar.csp.CSP;

public interface OutputInterface {
    public void setCSP(CSP csp);
    public void setOut(PrintWriter out);
    public void setFormat(String format) throws SugarException;
    public void output() throws SugarException;
}
