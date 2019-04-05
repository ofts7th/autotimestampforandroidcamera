package cs;

public class string {
	public static Boolean IsNullOrEmpty(String str){
		if(str == null || str.equals("")){
			return true;
		}
		return false;
	}

	public static Boolean Equal(String a, String b){
		if(a == null && b == null){
			return true;
		}
		if(a != null && b != null){
			return a.equals(b);
		}
		return false;
	}


	public static String padLeft(String str, int len, String c) {
		while (str.length() < len) {
			str = c + str;
		}
		return str;
	}
}
