package org.oneliveweb.hibernate;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.data.BaseData;
import org.openedit.util.DateStorageUtil;



public class HibernateData extends BaseData {

	private static final Log log = LogFactory.getLog(HibernateData.class);

	
	
	protected Object fieldData;

	public Object getData() {
		return fieldData;
	}

	public void setData(Object inData) {
		fieldData = inData;
	}

	public String get(String inKey) {
		
		
		// for UI
		try {
			PropertyDescriptor pd;
			try {
				pd = new PropertyDescriptor(inKey, getData().getClass());
			} catch (Exception e) {
				return super.get(inKey);

			}
			Method getter = pd.getReadMethod();
			if (getter == null) {
				return super.get(inKey);

			}
			Object f = getter.invoke(fieldData);
			if (f != null) {
				return f.toString();
			}

		} catch (Exception e) {
			return null;
		}

		return super.get(inKey);
	}

	
	public Object getValue(String inKey)
	{

		// for UI
		try {
			PropertyDescriptor pd;
			try {
				pd = new PropertyDescriptor(inKey, getData().getClass());
			} catch (Exception e) {
				return super.getValue(inKey);

			}
			Method getter = pd.getReadMethod();
			if (getter == null) {
				return super.get(inKey);

			}
			Object f = getter.invoke(fieldData);
			if (f != null) {
				return f;
			}

		} catch (Exception e) {
			return null;
		}

		return super.getValue(inKey);
	}
	
	
	
	
	@Override
	public void setValue(String inKey, Object inValue) {
	
		PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(inKey, getData().getClass());
            Method setter = pd.getWriteMethod();
            if(setter == null) {
            	super.setValue(inKey, inValue);
            	return;
            }
            try {
            	Class<?> paramtype = setter.getParameterTypes()[0];
				if(paramtype.equals(Date.class)  && inValue instanceof String) {
					inValue = DateStorageUtil.getStorageUtil().parseFromStorage((String)inValue);
				}
            	
            	if(paramtype.equals(Integer.class) && inValue instanceof String){
            		inValue = Integer.valueOf((String)inValue);
            	}
				if(paramtype.equals(Long.class) && inValue instanceof String){
            		inValue = Long.valueOf((String)inValue);
            	}
				if(paramtype.equals(Long.class) && inValue instanceof Integer){
            		inValue = Long.valueOf((Integer)inValue);
            	}
            	
                setter.invoke(getData(),inValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            	log.info("Error setting " + inKey + " to " + inValue + " " + e.getMessage());
            }
        } catch (IntrospectionException e) {
        	log.info("Error setting " + inKey + " to " + inValue + " " + e.getMessage());
         //   e.printStackTrace();
        }
		
		
	}
	
	

}
