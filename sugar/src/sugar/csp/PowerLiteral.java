package sugar.csp;

import java.util.Set;

import sugar.SugarException;

/**
 * NOT IMPLEMENTED YET.
 * This class implements a literal for arithmetic power.
 * @see CSP
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 */
public class PowerLiteral extends Literal {

    @Override
    public Set<IntegerVariable> getVariables() {
        // TODO
        return null;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean isValid() throws SugarException {
        return false;
    }

    @Override
    public boolean isUnsatisfiable() throws SugarException {
        return false;
    }
    
    @Override
    public int propagate() {
        // TODO propagate
        return 0;
    }

    @Override
    public boolean isSatisfied() {
        // TODO isSatisfied
        return false;
    }

    @Override
    public Literal neg() throws SugarException {
        throw new SugarException("Negation of PowerLiteral " + this);
    }

    @Override
    public String toString() {
        // TODO toString
        String s = "(power)";
        return s;
    }

}
