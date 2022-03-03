package dotnet54.tscore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Options {

	private String _name = "";
	private HashMap<String, Object> options;

	public Options() {
		this.options = new HashMap<>();
	}
	
	public Options(String name) {
		this.options = new HashMap<>();
		this._name = name;
	}

	public <T> T getField(String name){
		try {
			Class thisClass = getClass();
			Field field = thisClass.getDeclaredField(name);
			field.setAccessible(true);
			return (T) field.get(this);
		}catch (NoSuchFieldException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}

	public <T> void setField(String name, T value) {
		try {
			Class thisClass = getClass();
			Field field = thisClass.getDeclaredField(name);
			field.setAccessible(true);
			field.set(this, value);
		}catch (NoSuchFieldException | IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}

	public <T> T get(String key){
		if (options.containsKey(key)){
			return (T) options.get(key);
		}else{
			return getField(key);
		}
	}

	public <T> T get(String key, Class<T> cls) {
		if (options.containsKey(key)){
			Object obj = options.get(key);
			if (cls != null && cls.isInstance(obj)) {
				return cls.cast(options.get(key));
			}else {
				throw new ClassCastException();
			}
		}else{
			return getField(key);
		}
	}

	public String getString(String key) {
		if (options.containsKey(key)){
			return (String) options.get(key);
		}else{
			return getField(key);
		}
	}

	public boolean getBoolean(String key) {
		if (options.containsKey(key)){
			Object obj = options.get(key);
			if (obj instanceof String) {
				return Boolean.parseBoolean((String)obj);
			}else {
				return (boolean) options.get(key);
			}
		}else{
			return getField(key);
		}

	}

	public int getInt(String key) {
		if (options.containsKey(key)){
			Object obj = options.get(key);
			if (obj instanceof String) {
				return Integer.parseInt((String)obj);
			}else {
				return (int) options.get(key);
			}
		}else{
			return getField(key);
		}
	}

	public long getLong(String key) {
		if (options.containsKey(key)){
			Object obj = options.get(key);
			if (obj instanceof String) {
				return Long.parseLong((String)obj);
			}else {
				return (int) options.get(key);
			}
		}else{
			return getField(key);
		}
	}

	public double getDouble(String key) {
		if (options.containsKey(key)){
			Object obj = options.get(key);
			if (obj instanceof String) {
				return Double.parseDouble((String)obj);
			}else {
				return (double) options.get(key);
			}
		}else{
			return getField(key);
		}
	}

	public Options set(String key, Object obj) {
		options.put(key, obj);
		return this;
	}
	
	public <T> Options set(String key, T obj, Class<T> cls) {
		options.put(key, obj);
		return this;
	}

	public Options set(String key, String str) {
		options.put(key, str);
		return this;
	}

	public Options set(String key, boolean blnValue) {
		options.put(key, blnValue);
		return this;
	}
	
	public Options set(String key, int intValue) {
		options.put(key, intValue);
		return this;
	}

	public Options set(String key, long longValue) {
		options.put(key, longValue);
		return this;
	}
	
	public Options set(String key, double dblValue) {
		options.put(key, dblValue);
		return this;
	}

	public Options setString(String key, String value) {
		options.put(key, value);
		return this;
	}

	public Options setBoolean(String key, boolean value) {
		options.put(key, value);
		return this;
	}

	public Options setInt(String key, int value) {
		options.put(key, value);
		return this;
	}

	public Options setLong(String key, long value) {
		options.put(key, value);
		return this;
	}

	public Options setDouble(String key, double value) {
		options.put(key, value);
		return this;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		this._name = name;
	}
	
	public String toString() {
		return this._name + ":" + options.toString();
	}
	
	public int size() {
		return options.size();
	}
	
	public String[] keys() {
		return options.keySet().toArray(new String[options.size()]);
	}

	public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

		class DerivedOptions extends Options{
			public String desc;
			public double pie = 3.142;
			private int age = 20;

			public DerivedOptions(){

			}
		}

		Options opt1 = new Options("boss");
		Options opt2 = new Options("st");
		
		opt2.set("string-option", "test string");
		
		List<Double> list = new ArrayList<Double>();
		list.add(new Double(5.5));
		opt2.set("list-option", list);
		
		opt1.set("object-option", opt2);
		
		Options opt3 = (Options) opt1.get("object-option");
		
		System.out.println(opt1);
		System.out.println(opt2);
		System.out.println(opt3);
		System.out.println("---");

		//testing casting
		System.out.println((String) opt3.get("string-option"));
		System.out.println(opt3.get("list-option", ArrayList.class).get(0).getClass());	
		System.out.println(Arrays.toString(opt3.keys()));


		// setting and getting using reflection
		String nameField = opt1.getField("_name");
		System.out.println("Field name = " + nameField);
		opt1.setField("_name", "new name");
		nameField = opt1.getField("_name");
		System.out.println("Field name = " + nameField);

		DerivedOptions dOpt = new DerivedOptions();
		System.out.println("Derived Option name = " + dOpt.getName());
		double pie = dOpt.getField("pie");
		System.out.println("Derived Option pie = " + pie);
		dOpt.setField("age", 40);
		int age = dOpt.getField("age");
		System.out.println("Derived Option age = " + age);
		dOpt.age = 50;
		age = dOpt.getField("age");
		System.out.println("Derived Option age = " + age);
		dOpt.setField("age", 60);
		age = dOpt.getField("age");
		System.out.println("Derived Option age = " + age);
	}
}
