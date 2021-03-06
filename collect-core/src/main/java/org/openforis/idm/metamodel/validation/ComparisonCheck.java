/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class ComparisonCheck extends Check<Attribute<?,?>> {

	private static final long serialVersionUID = 1L;

	private enum Operation {
		LT("<"), LTE("<="), GT(">"), GTE(">="), EQ("=");

		private String xpathOperator;

		private Operation(final String xpathOperator) {
			this.xpathOperator = xpathOperator;
		}
	}

	private String expression;
	private String lessThanExpression;
	private String lessThanOrEqualsExpression;
	private String greaterThanExpression;
	private String greaterThanOrEqualsExpression;
	private String equalsExpression;

	public String getLessThanExpression() {
		return this.lessThanExpression;
	}

	public String getLessThanOrEqualsExpression() {
		return this.lessThanOrEqualsExpression;
	}

	public String getGreaterThanExpression() {
		return this.greaterThanExpression;
	}

	public String getGreaterThanOrEqualsExpression() {
		return this.greaterThanOrEqualsExpression;
	}

	public String getEqualsExpression() {
		return this.equalsExpression;
	}
			
	public String getExpression() {
		if ( expression == null ) {
			expression = buildExpression();
		}
		return expression;
	}
	
	public void setLessThanExpression(String lessThanExpression) {
		this.lessThanExpression = lessThanExpression;
		this.expression = null;
	}

	public void setLessThanOrEqualsExpression(String lessThanOrEqualsExpression) {
		this.lessThanOrEqualsExpression = lessThanOrEqualsExpression;
		this.expression = null;
	}

	public void setGreaterThanExpression(String greaterThanExpression) {
		this.greaterThanExpression = greaterThanExpression;
		this.expression = null;
	}

	public void setGreaterThanOrEqualsExpression(String greaterThanOrEqualsExpression) {
		this.greaterThanOrEqualsExpression = greaterThanOrEqualsExpression;
		this.expression = null;
	}

	public void setEqualsExpression(String equalsExpression) {
		this.equalsExpression = equalsExpression;
		this.expression = null;
	}

	private String buildExpression() {
		ExpressionBuilder expressionBuilder = new ExpressionBuilder();

		if (StringUtils.isNotBlank(greaterThanExpression)) {
			expressionBuilder.addOperation(Operation.GT, greaterThanExpression);
		}
		if (StringUtils.isNotBlank(greaterThanOrEqualsExpression)) {
			expressionBuilder.addOperation(Operation.GTE, greaterThanOrEqualsExpression);
		}
		if (StringUtils.isNotBlank(lessThanExpression)) {
			expressionBuilder.addOperation(Operation.LT, lessThanExpression);
		}
		if (StringUtils.isNotBlank(lessThanOrEqualsExpression)) {
			expressionBuilder.addOperation(Operation.LTE, lessThanOrEqualsExpression);
		}
		if (StringUtils.isNotBlank(equalsExpression)) {
			expressionBuilder.addOperation(Operation.EQ, equalsExpression);
		}
		
		return expressionBuilder.getExpression();
	}

	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> node) {
		Record record = node .getRecord();
		SurveyContext recordContext = record.getSurveyContext();
		ExpressionEvaluator expressionEvaluator = recordContext.getExpressionEvaluator();
		String expr = getExpression();
		try {
			boolean valid = expressionEvaluator.evaluateBoolean(node.getParent(), node, expr, true);
			return ValidationResultFlag.valueOf(valid, this.getFlag());
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating comparison check", e);
		}
	}

	private class ExpressionBuilder {

		private StringBuilder expression;
		private boolean firstOperation = true;

		ExpressionBuilder() {
			this.expression = new StringBuilder();
		}

		void addOperation(Operation o, String value) {
			if (firstOperation) {
				firstOperation = Boolean.FALSE;
			} else {
				expression.append(" ");
				expression.append("and");
				expression.append(" ");
			}
			expression.append("$this");
			expression.append(" ");
			expression.append(o.xpathOperator);
			expression.append(" ");
			expression.append(value);
		}

		String getExpression() {
			return expression.toString();
		}
	}

}
