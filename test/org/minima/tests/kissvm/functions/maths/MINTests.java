package org.minima.tests.kissvm.functions.maths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;
import org.minima.kissvm.Contract;
import org.minima.kissvm.exceptions.ExecutionException;
import org.minima.kissvm.exceptions.MinimaParseException;
import org.minima.kissvm.expressions.ConstantExpression;
import org.minima.kissvm.functions.MinimaFunction;
import org.minima.kissvm.functions.number.MIN;
import org.minima.kissvm.values.BooleanValue;
import org.minima.kissvm.values.HexValue;
import org.minima.kissvm.values.NumberValue;
import org.minima.kissvm.values.StringValue;
import org.minima.kissvm.values.Value;
import org.minima.objects.Transaction;
import org.minima.objects.Witness;

//NumberValue MIN (NumberValue var1 … NumberValue varN)
public class MINTests {

    @Test
    public void testConstructors() {
        MIN fn = new MIN();
        MinimaFunction mf = fn.getNewFunction();

        assertEquals("MIN", mf.getName());
        assertEquals(0, mf.getParameterNum());

        try {
            mf = MinimaFunction.getFunction("MIN");
            assertEquals("MIN", mf.getName());
            assertEquals(0, mf.getParameterNum());
        } catch (MinimaParseException ex) {
            fail();
        }
    }

    @Test
    public void testValidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());
        ctr.setMaxInstructions(Integer.MAX_VALUE);
        
        MIN fn = new MIN();

        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            try {
                Value res = mf.runFunction(ctr);
                assertEquals(Value.VALUE_NUMBER, res.getValueType());
                assertEquals("0", ((NumberValue) res).toString());
            } catch (ExecutionException ex) {
                fail();
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                MinimaFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
                mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Integer.toString(Integer.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                MinimaFunction mf = fn.getNewFunction();
                mf.addParameter(new ConstantExpression(new NumberValue(Long.MIN_VALUE)));
                mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Long.toString(Long.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                MinimaFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Integer.toString(Integer.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                MinimaFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Long.MIN_VALUE)));
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Long.toString(Long.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                MinimaFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Integer.MIN_VALUE)));
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextInt())));
                }
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Integer.toString(Integer.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }
        {
            for (int i = 0; i < 10000; i++) {
                Random Rnd = new Random();

                MinimaFunction mf = fn.getNewFunction();
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                }
                mf.addParameter(new ConstantExpression(new NumberValue(Long.MIN_VALUE)));
                for (int j = 0; j < 12; j++) {
                    mf.addParameter(new ConstantExpression(new NumberValue(Rnd.nextLong())));
                }
                try {
                    Value res = mf.runFunction(ctr);
                    assertEquals(Value.VALUE_NUMBER, res.getValueType());
                    assertEquals(Long.toString(Long.MIN_VALUE), ((NumberValue) res).toString());
                } catch (ExecutionException ex) {
                    fail();
                }
            }
        }

    }

    @Test
    public void testInvalidParams() {
        Contract ctr = new Contract("", "", new Witness(), new Transaction(), new ArrayList<>());

        MIN fn = new MIN();

        // Invalid param count
        {
            MinimaFunction mf = fn.getNewFunction();
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        // Invalid param domain
        {
        }

        // Invalid param types
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }

        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new BooleanValue(true)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new HexValue("0x01234567")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new NumberValue(0)));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
        {
            MinimaFunction mf = fn.getNewFunction();
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            mf.addParameter(new ConstantExpression(new StringValue("Hello World")));
            assertThrows(ExecutionException.class, () -> {
                Value res = mf.runFunction(ctr);
            });
        }
    }
}
