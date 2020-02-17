package sugar.hook;

import java.util.List;

import sugar.SugarException;
import sugar.converter.Converter;
import sugar.csp.Clause;
import sugar.expression.Expression;

public interface ConverterHook {
    public Expression convertFunction(Converter converter, Expression x) throws SugarException;
    public Expression convertConstraint(Converter converter, Expression x, boolean negative, List<Clause> clauses) throws SugarException;
}
