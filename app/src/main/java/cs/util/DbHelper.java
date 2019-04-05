package cs.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper {
	private static DbHelper _instance;
	private SQLiteDatabase _dbInstance;
	private Context context;

	private DbHelper() {
	}

	
	public static DbHelper getInstance() {
		if (_instance == null) {
			_instance = new DbHelper();
		}
		return _instance;
	}

	public static void setContext(Context cxt) {
		if(getInstance().context == null){
			getInstance().context = cxt;
		}
	}

	public SQLiteDatabase getDatabase() {
		if (_dbInstance == null) {
			_dbInstance = (new dbOpenHelper(context, "mydb"))
					.getWritableDatabase();
		}
		return _dbInstance;
	}

	public static List<List<String>> query(String sql) {
		List<List<String>> result = new ArrayList<List<String>>();
		Cursor cur = DbHelper.getInstance().getDatabase()
				.rawQuery(sql, new String[] {});
		while (cur.moveToNext()) {
			List<String> row = new ArrayList<String>();
			int colCount = cur.getColumnCount();
			for(int i=0;i<colCount;i++){
				row.add(cur.getString(i));
			}
			result.add(row);
		}
		cur.close();
		return result;
	}
	
	public static void execNonquery(String sql){
		getInstance().getDatabase().execSQL(sql);
	}
	
	public static void saveRecrod(String tbName, ContentValues values) {
		getInstance().getDatabase().insert(tbName, null, values);
	}
	
	public static void updateRecrod(String tbName, String id, ContentValues values) {
		getInstance().getDatabase().update(tbName, values, "id=?", new String[] { id });
	}
	
	public static void deleteRecord(String tbName, String id) {
		getInstance().getDatabase()
				.delete(tbName, "id=?", new String[] { id });
	}
	
	public void close(){
		if(_dbInstance != null){
			_dbInstance.close();
			_dbInstance = null;
		}
	}
	
	class dbOpenHelper extends SQLiteOpenHelper {
		public dbOpenHelper(Context context, String name) {
			super(context, name, null, 1);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table config(id integer primary key autoincrement, name text, val text)");
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}
}