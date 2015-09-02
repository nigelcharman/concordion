package org.concordion.api;

import org.concordion.api.listener.ExpressionEvaluatedEvent;
import org.concordion.api.listener.MissingRowEvent;
import org.concordion.api.listener.SurplusRowEvent;
import org.concordion.api.listener.VerifyRowsListener;
import org.concordion.internal.Row;
import org.concordion.internal.TableSupport;
import org.concordion.internal.util.Announcer;

import java.util.ArrayList;
import java.util.List;

public abstract class VerifyRowsStrategy {

    protected final CommandCall commandCall;
    protected final Evaluator evaluator;
    protected final ResultRecorder resultRecorder;
    protected final Announcer<VerifyRowsListener> listeners;
    protected final String loopVariableName;
    protected final TableSupport tableSupport;
    protected final Row[] expectedRows;
    protected final List<Object> actualRows;

    public VerifyRowsStrategy(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder,
                              Announcer<VerifyRowsListener> listeners, String loopVariableName, Iterable<Object> actualRows) {
        this.commandCall = commandCall;
        this.evaluator = evaluator;
        this.resultRecorder = resultRecorder;
        this.listeners = listeners;
        this.loopVariableName = loopVariableName;
        this.tableSupport = new TableSupport(commandCall);
        this.expectedRows = tableSupport.getDetailRows();
        this.actualRows = copy(actualRows);
    }

    public abstract void verify();

    protected void announceExpressionEvaluated(Element element) {
        listeners.announce().expressionEvaluated(new ExpressionEvaluatedEvent(element));
    }

    protected void announceMissingRow(Element element) {
        listeners.announce().missingRow(new MissingRowEvent(element));
    }

    protected void announceSurplusRow(Element element) {
        listeners.announce().surplusRow(new SurplusRowEvent(element));
    }

    protected List<Object> copy(Iterable<Object> iterable) {
        List<Object> copy = new ArrayList<Object>();
        for (Object o : iterable) {
            copy.add(o);
        }
        return copy;
    }
}