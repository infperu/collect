/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;

/**
 * @author M. Togna
 *
 */
public class IDMFunctionsTest extends AbstractExpressionTest {

	@Test
	public void testBlankWithMissingNode() throws InvalidExpressionException{
		assertTrue(evaluateBooleanExpression(cluster, null, "idm:blank(id)"));
	}
	
	@Test
	public void testBlankWithNullValue() throws InvalidExpressionException{
		EntityBuilder.addValue(cluster, "id", (Code) null);
		assertTrue(evaluateBooleanExpression(cluster, null, "idm:blank(id)"));
	}
	
	@Test
	public void testBlankWithBlankValue() throws InvalidExpressionException{
		EntityBuilder.addValue(cluster, "id",  new Code(""));
		assertTrue(evaluateBooleanExpression(cluster, null, "idm:blank(id)"));
	}
	
	@Test
	public void testBlankValidCode() throws InvalidExpressionException{
		EntityBuilder.addValue(cluster, "id",  new Code("001"));
		Assert.assertFalse(evaluateBooleanExpression(cluster, null, "idm:blank(id)"));
	}
	
	@Test
	public void testBlankValidNumber() throws InvalidExpressionException{
		EntityBuilder.addValue(cluster, "plot_direction",  3442.45);
		Assert.assertFalse(evaluateBooleanExpression(cluster, null, "idm:blank( plot_direction )"));
	}

	@Test
	public void testBlankInNestedExpression() throws InvalidExpressionException{
		EntityBuilder.addValue(cluster, "plot_direction",  3442.45);
		Assert.assertTrue(evaluateBooleanExpression(cluster, null, "not( idm:blank( plot_direction ) )"));
	}

	@Test
	public void testIndexAndPosition() throws InvalidExpressionException {
		Entity plot1 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot1, "no", new Code("1"));
		Entity plot2 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot2, "no", new Code("2"));
		Assert.assertEquals(Integer.valueOf(0), evaluateExpression(cluster, "idm:index(plot[1])"));
		Assert.assertEquals(Integer.valueOf(0), evaluateExpression(cluster, "idm:index(plot[no='1'])"));
		Assert.assertEquals(Integer.valueOf(1), evaluateExpression(cluster, "idm:index(plot[2])"));
		Assert.assertEquals(Integer.valueOf(1), evaluateExpression(cluster, "idm:index(plot[no='2'])"));
		Assert.assertEquals(Integer.valueOf(1), evaluateExpression(cluster, "idm:position(plot[1])"));
		Assert.assertEquals(Integer.valueOf(2), evaluateExpression(cluster, "idm:position(plot[2])"));
		Assert.assertEquals(Integer.valueOf(1), evaluateExpression(plot2, "idm:index()"));
	}
	
	@Test
	public void testCurrentDateFunction() throws InvalidExpressionException {
		String expr = ExpressionFactory.IDM_PREFIX + ":currentDate()";
		Object object = evaluateExpression(expr);

		Assert.assertTrue(object instanceof org.openforis.idm.model.Date);
		
		Date now = new Date();
		org.openforis.idm.model.Date currentDate = org.openforis.idm.model.Date.parse(now);
		Assert.assertTrue(currentDate.equals(object));
	}

	@Test
	public void testCurrentTimeFunction() throws InvalidExpressionException {
		String expr = ExpressionFactory.IDM_PREFIX + ":currentTime()";
		Object object = evaluateExpression(expr);

		Assert.assertTrue(object instanceof org.openforis.idm.model.Time);
		
		Date now = new Date();
		org.openforis.idm.model.Time currentTime = org.openforis.idm.model.Time.parse(now);
		Assert.assertTrue(currentTime.equals(object));
	}
	
}
